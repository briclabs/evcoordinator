package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION;

public class RegistrationPacketLogic<P extends RegistrationPacket> extends Logic<RegistrationRecord, Registration, net.briclabs.evcoordinator.generated.tables.Registration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationPacketLogic.class);

    private final ParticipantLogic<Participant> participantLogic;
    private final RegistrationLogic<Registration> registrationLogic;
    private final GuestLogic<Guest> guestLogic;

    public RegistrationPacketLogic(DSLContext jooq) {
        super(jooq, Registration.class, REGISTRATION, REGISTRATION.ID);
        this.participantLogic = new ParticipantLogic<>(jooq);
        this.registrationLogic = new RegistrationLogic<>(jooq);
        this.guestLogic = new GuestLogic<>(jooq);
    }

    /**
     * This operation includes inserting the participant, their guests, and event details if all provided
     * data is valid and correctly processed. If the participant registering already exists, uses the existing
     * participant information; otherwise, inserts a new participant per the data contained in the registration
     * packet.
     *
     * @param registrationRequest the registration to create.
     * @return An {@code Optional<Long>} containing the ID of the newly created event if all insertions were successful,
     *         or an empty {@code Optional} if any operation failed during the process.
     */
    public Optional<Long> register(P registrationRequest) {
        if (isPacketIncomplete(registrationRequest)) {
            return Optional.empty();
        }

        var participantId = fetchOrInsertParticipatingAttendee(registrationRequest);
        if (participantId.isEmpty()) {
            LOGGER.error("Participant either did not already exist or failed to insert. Aborting registration process.");
            return Optional.empty();
        }

        return insertNewRegistration(registrationRequest.registration(), registrationRequest.guests(), participantId.get());
    }

    private boolean isPacketIncomplete(P registrationRequest) {
        if (registrationRequest.participant() == null || registrationRequest.guests() == null || registrationRequest.registration() == null) {
            LOGGER.error("Attempted to insert a registration packet with null values. Aborting.");
            return true;
        }
        return false;
    }

    private Optional<Long> fetchOrInsertParticipatingAttendee(P registrationRequest) {
        var preexistingAttendee = participantLogic.fetchAttendeeByNameAndEmail(registrationRequest.participant().getNameFirst(), registrationRequest.participant().getNameLast(), registrationRequest.participant().getAddrEmail());
        return preexistingAttendee.count() == 0 || preexistingAttendee.list().get(0).getId() == null
                ? participantLogic.insertNew(registrationRequest.participant())
                : Optional.ofNullable(preexistingAttendee.list().get(0).getId());
    }

    private Optional<Long> insertNewRegistration(Registration registration, Guest[] guests, Long presentParticipantId) {
        var registrationToCreate = new Registration(null, presentParticipantId, registration.getDonationPledge(), registration.getSignature(), registration.getEventInfoId(), null);
        Optional<Long> registrationId = registrationLogic.isAlreadyRecorded(registrationToCreate) ? Optional.empty() : registrationLogic.insertNew(registrationToCreate);
        if (registrationId.isEmpty()) {
            LOGGER.error("Failed to insert a registration.");
            return Optional.empty();
        }

        for (var guest : guests) {
            var guestToCreate = new Guest(null, presentParticipantId, registrationId.get(), guest.getRawGuestName(), null, guest.getRelationship(), null);
            Optional<Long> guestId = guestLogic.isAlreadyRecorded(guestToCreate) ? Optional.empty() : guestLogic.insertNew(guestToCreate);
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
