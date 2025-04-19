package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationPacketWithLabel;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationPacketWithLabelRecord;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import net.briclabs.evcoordinator.validation.RegistrationPacketValidator;
import org.jooq.DSLContext;

import java.util.AbstractMap;
import java.util.Map;

import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION_PACKET_WITH_LABEL;

public class RegistrationPacketLogic extends Logic<RegistrationPacketWithLabelRecord, RegistrationPacketWithLabel, net.briclabs.evcoordinator.generated.tables.RegistrationPacketWithLabel> implements Validatable<RegistrationPacket> {

    /**
     * Represents the distinct sections of a registration packet.
     * <p>
     * This enum is used to define and manage the various logical sections involved
     * in the registration process. Each section corresponds to a specific part of
     * the data being processed during registration, including participants, guest
     * information, the registration, and the overall registration packet details.
     */
    public enum PACKET_SECTION {
        OVERALL,
        PARTICIPANT,
        REGISTRATION,
        GUESTS
    }

    private final ParticipantLogic participantLogic;
    private final RegistrationLogic registrationLogic;
    private final GuestLogic guestLogic;

    public RegistrationPacketLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, RegistrationPacketWithLabel.class, REGISTRATION_PACKET_WITH_LABEL, REGISTRATION_PACKET_WITH_LABEL.ID);
        this.participantLogic = new ParticipantLogic(objectMapper, jooq);
        this.registrationLogic = new RegistrationLogic(objectMapper, jooq);
        this.guestLogic = new GuestLogic(objectMapper, jooq);
    }

    /**
     * This operation includes inserting the participant, their guests, and event details if all provided
     * data is valid and correctly processed. If the participant registering already exists, uses the existing
     * participant information; otherwise, inserts a new participant per the data contained in the registration
     * packet.
     *
     * @param registrationRequest the registration to create.
     * @param actorId the participant ID of the participant submitting the registration packet.
     * @return A {@code Long} containing the ID of the newly created event.
     * @throws RegistrationPacketException in the event there's a problem handling processing of a part of the Registration Packet.
     */
    public Long register(long actorId, RegistrationPacket registrationRequest) throws RegistrationPacketException{
        var participantId = fetchOrInsertParticipatingAttendee(actorId, registrationRequest);
        return processRegistration(actorId, registrationRequest.registration(), registrationRequest.guests(), participantId);
    }

    @Override
    public Map<String, String> validate(RegistrationPacket pojo) {
        return RegistrationPacketValidator.of(pojo).getMessages();
    }

    private Long fetchOrInsertParticipatingAttendee(long actorId, RegistrationPacket registrationRequest) throws RegistrationPacketException {
        var preexistingAttendee = participantLogic.fetchAttendeeByNameAndEmail(registrationRequest.participant().getNameFirst(), registrationRequest.participant().getNameLast(), registrationRequest.participant().getAddrEmail());
        if (preexistingAttendee.count() != 0 && preexistingAttendee.list().get(0).getId() != null) {
            return preexistingAttendee.list().get(0).getId();
        }
        return participantLogic.insertNew(actorId, registrationRequest.participant()).orElseThrow(
                () -> new RegistrationPacketException(
                        new AbstractMap.SimpleImmutableEntry<>(PACKET_SECTION.PARTICIPANT.toString(),
                                "Failed to insert participant."), "Failed to insert participant %s.".formatted(
                        registrationRequest.participant().getNameFirst() + " " + registrationRequest.participant().getNameLast() + " (" + registrationRequest.participant().getAddrEmail() + ")")));
    }

    private Long processRegistration(long actorId, Registration registration, Guest[] guests, long presentParticipantId) throws RegistrationPacketException {
        var registrationId = insertRegistration(actorId, registration, presentParticipantId);
        insertGuests(actorId, guests, registrationId);

        return registrationId;
    }

    private Long insertRegistration(long actorId, Registration registration, long participantId) throws RegistrationPacketException {
        var registrationToCreate = new Registration(null, participantId, registration.getDonationPledge(), registration.getSignature(), registration.getEventInfoId(), null);
        if (registrationLogic.isAlreadyRecorded(registrationToCreate)) {
            throw new RegistrationPacketException(
                    new AbstractMap.SimpleImmutableEntry<>(PACKET_SECTION.REGISTRATION.toString(), "Registration already exists."),
                    "Registration for participant ID %d and event info %d already exists.".formatted(participantId, registrationToCreate.getEventInfoId()));
        } else {
            return registrationLogic.insertNew(actorId, registrationToCreate).orElseThrow(
                    () -> new RegistrationPacketException(
                            new AbstractMap.SimpleImmutableEntry<>(PACKET_SECTION.REGISTRATION.toString(), "Failed to insert a registration."),
                            "Failed to insert event info %d registration for participant ID %d.".formatted(registration.getEventInfoId(), participantId)));
        }
    }

    /**
     * Inserts guests associated with a registration process and records them <b>if they are not already present</b>.
     * Throws an exception if any guest could not be inserted. Silently ignores preexisting guests for this registration.
     *
     * @param actorId               The ID of the actor performing the operation.
     * @param guests                An array of {@code Guest} objects representing the guests to be inserted.
     * @param registrationId        The ID of the registration under which the guests are being added.
     * @throws RegistrationPacketException if a guest insertion fails during the process.
     */
    private void insertGuests(long actorId, Guest[] guests, Long registrationId) throws RegistrationPacketException {
        for (var guest : guests) {
            var guestToCreate = new Guest(null, registrationId, guest.getRawGuestName(), null, guest.getRelationship(), null);
            if (!guestLogic.isAlreadyRecorded(guestToCreate) && guestLogic.insertNew(actorId, guestToCreate).isEmpty()) {
                throw new RegistrationPacketException(
                        new AbstractMap.SimpleImmutableEntry<>(PACKET_SECTION.GUESTS.toString(), "Failed to insert a guest."),
                        "Failed to insert guest %s for registration ID %d.".formatted(guest.getRawGuestName(), registrationId));
            }
        }
    }

    public static class RegistrationPacketException extends WriteLogic.LogicException {
        public RegistrationPacketException(Map.Entry<String, String> publicMessage, String troubleshootingMessage) {
            super(publicMessage, troubleshootingMessage);
        }
    }
}
