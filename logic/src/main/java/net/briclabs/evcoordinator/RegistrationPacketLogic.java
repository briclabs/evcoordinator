package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationPacketWithLabel;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationPacketWithLabelRecord;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION_PACKET_WITH_LABEL;

public class RegistrationPacketLogic<P extends RegistrationPacket> extends Logic<RegistrationPacketWithLabelRecord, RegistrationPacketWithLabel, net.briclabs.evcoordinator.generated.tables.RegistrationPacketWithLabel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationPacketLogic.class);

    private final ParticipantLogic<Participant> participantLogic;
    private final RegistrationLogic<Registration> registrationLogic;
    private final GuestLogic<Guest> guestLogic;

    public RegistrationPacketLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, RegistrationPacketWithLabel.class, REGISTRATION_PACKET_WITH_LABEL, REGISTRATION_PACKET_WITH_LABEL.ID);
        this.participantLogic = new ParticipantLogic<>(objectMapper, jooq);
        this.registrationLogic = new RegistrationLogic<>(objectMapper, jooq);
        this.guestLogic = new GuestLogic<>(objectMapper, jooq);
    }

    /**
     * This operation includes inserting the participant, their guests, and event details if all provided
     * data is valid and correctly processed. If the participant registering already exists, uses the existing
     * participant information; otherwise, inserts a new participant per the data contained in the registration
     * packet.
     *
     * @param registrationRequest the registration to create.
     * @param actorId the participant ID of the participant submitting the registration packet.
     * @return An {@code Optional<Long>} containing the ID of the newly created event if all insertions were successful,
     *         or an empty {@code Optional} if any operation failed during the process.
     */
    public Optional<Long> register(long actorId, P registrationRequest) {
        if (isPacketIncomplete(registrationRequest)) {
            return Optional.empty();
        }

        var participantId = fetchOrInsertParticipatingAttendee(actorId, registrationRequest);
        if (participantId.isEmpty()) {
            LOGGER.error("Participant either did not already exist or failed to insert. Aborting registration process.");
            return Optional.empty();
        }

        return insertNewRegistration(actorId, registrationRequest.registration(), registrationRequest.guests(), participantId.get());
    }

    private boolean isPacketIncomplete(P registrationRequest) {
        if (registrationRequest.participant() == null || registrationRequest.guests() == null || registrationRequest.registration() == null) {
            LOGGER.error("Attempted to insert a registration packet with null values. Aborting.");
            return true;
        }
        return false;
    }

    private Optional<Long> fetchOrInsertParticipatingAttendee(long actorId, P registrationRequest) {
        var preexistingAttendee = participantLogic.fetchAttendeeByNameAndEmail(registrationRequest.participant().getNameFirst(), registrationRequest.participant().getNameLast(), registrationRequest.participant().getAddrEmail());
        return preexistingAttendee.count() == 0 || preexistingAttendee.list().get(0).getId() == null
                ? participantLogic.insertNew(actorId, registrationRequest.participant())
                : Optional.ofNullable(preexistingAttendee.list().get(0).getId());
    }

    private Optional<Long> insertNewRegistration(long actorId, Registration registration, Guest[] guests, long presentParticipantId) {
        var registrationToCreate = new Registration(null, presentParticipantId, registration.getDonationPledge(), registration.getSignature(), registration.getEventInfoId(), null);
        Optional<Long> registrationId = registrationLogic.isAlreadyRecorded(registrationToCreate) ? Optional.empty() : registrationLogic.insertNew(actorId, registrationToCreate);
        if (registrationId.isEmpty()) {
            LOGGER.error("Failed to insert a registration.");
            return Optional.empty();
        }

        for (var guest : guests) {
            var guestToCreate = new Guest(null, presentParticipantId, registrationId.get(), guest.getRawGuestName(), null, guest.getRelationship(), null);
            Optional<Long> guestId = guestLogic.isAlreadyRecorded(guestToCreate) ? Optional.empty() : guestLogic.insertNew(actorId, guestToCreate);
            if (guestId.isEmpty()) {
                LOGGER.error("Failed to insert a guest.");
                return Optional.empty();
            }
            LOGGER.info("Successfully inserted guest ID {} for registration ID {}.", guestId.get(), registrationId.get());
        }

        LOGGER.info("Successfully inserted participant ID {} and registration ID {}.", presentParticipantId, registrationId.get());
        return registrationId;
    }
}
