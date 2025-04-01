package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static net.briclabs.evcoordinator.generated.Tables.GUEST;
import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION;

public class RegistrationPacketLogic<P extends RegistrationPacket> extends Logic<RegistrationRecord, Registration, net.briclabs.evcoordinator.generated.tables.Registration> implements WriteLogic<P> {

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
     * Fetches a registration packet based on the provided registration ID.
     * The method retrieves a registration record by its ID and, if a record exists,
     * builds a {@code RegistrationPacket} containing the associated participant details,
     * participant guests, and event registration information.
     *
     * @param id the unique identifier of the registration to be fetched.
     * @return an {@code Optional<RegistrationPacket>} containing the constructed registration packet,
     *         or an empty {@code Optional} if no registration with the specified ID is found.
     */
    public Optional<RegistrationPacket> fetchRegistrationPacketByRegistrationId(Long id) {
        return registrationLogic.fetchById(id).flatMap(this::buildRegistrationPacket);
    }

    /**
     * Fetches a registration packet associated with the specified event information ID.
     * This method retrieves a registration record by the given event information ID and, if a record exists,
     * builds a {@code RegistrationPacket} containing the associated participant details,
     * participant guests, and event registration information.
     *
     * @param id the unique identifier of the event information to fetch the registration packet for.
     * @return an {@code Optional<RegistrationPacket>} containing the constructed registration packet,
     *         or an empty {@code Optional} if no relevant data is found.
     */
    public Optional<RegistrationPacket> fetchRegistrationPacketByEventInfoId(Long id) {
        return registrationLogic.fetchByCriteria(true, Map.of(REGISTRATION.EVENT_INFO_ID.getName(), id.toString()), null, false, 0, 1)
                .list().stream().findFirst().flatMap(this::buildRegistrationPacket);
    }

    /**
     * Fetches a registration packet associated with the specified participant ID.
     * This method retrieves a registration record by the given participant ID and, if a record exists,
     * builds a {@code RegistrationPacket} containing the associated participant details,
     * participant relationships, and event registration information.
     *
     * @param id the unique participant ID to search for.
     * @return an {@code Optional<RegistrationPacket>} containing the constructed registration packet,
     *         or an empty {@code Optional} if no matching record is found.
     */
    public Optional<RegistrationPacket> fetchRegistrationPacketByParticipantId(Long id) {
        return registrationLogic.fetchByCriteria(true, Map.of(REGISTRATION.PARTICIPANT_ID.getName(), id.toString()), null, false, 0, 1)
                .list().stream().findFirst().flatMap(this::buildRegistrationPacket);
    }

    /**
     * Because this is a compound action, newness is checked in the {@link #insertNew(RegistrationPacket)} method as each object must first be built before checking.
     * Therefore, this method does not serve any purpose.
     *
     * @param registrationRequest The Registration request to be validated as truly new.
     * @return true if the participant, guests, and event are all validated as new; otherwise, false.
     */
    @Override
    public boolean isAlreadyRecorded(P registrationRequest) {
        throw new UnsupportedOperationException("This method is not supported. The insertNew() method will perform this function over the course of its process.");
    }

    /**
     * This operation includes inserting the participant, their guests, and event details if all provided
     * data is valid and correctly processed.
     *
     * @param registrationRequest the registration to create.
     * @return An {@code Optional<Long>} containing the ID of the newly created event if all insertions were successful,
     *         or an empty {@code Optional} if any operation failed during the process.
     */
    @Override
    public Optional<Long> insertNew(P registrationRequest) {
        if (isPacketIncomplete(registrationRequest)) {
            return Optional.empty();
        }

        Optional<Long> participantId = participantLogic.isAlreadyRecorded(registrationRequest.participant()) ? Optional.empty() : participantLogic.insertNew(registrationRequest.participant());
        if (participantId.isEmpty()) {
            LOGGER.error("Failed to insert a participant.");
            return Optional.empty();
        }

        return insertNewRegistration(registrationRequest.registration(), registrationRequest.guests(), participantId.get());
    }

    /**
     * Inserts a new registration packet by utilizing an existing participant profile if available.
     * This method retrieves a preexisting participant based on the provided registration request
     * details and creates a complete registration packet with associated data. The registration
     * packet is then passed to another method to handle insertion.
     *
     * @param registrationRequest The registration request containing participant details,
     *        guests, and registration information. This data is used to locate an
     *        existing participant and create a complete registration packet.
     * @return An {@code Optional<Long>} containing the ID of the newly created registration,
     *         or an empty {@code Optional} if the operation fails at any stage.
     */
    public Optional<Long> insertNewRegistrationWithPreexistingProfile(P registrationRequest) {
        if (isPacketIncomplete(registrationRequest)) {
            return Optional.empty();
        }

        var preexistingParticipant = participantLogic.fetchPreexistingAttendeeByNameAndEmail(registrationRequest.participant().getNameFirst(), registrationRequest.participant().getNameLast(), registrationRequest.participant().getAddrEmail());
        if (preexistingParticipant.count() == 0 ) {
            LOGGER.error("No preexisting participant found with the provided name and email. Aborting.");
            return Optional.empty();
        }

        return insertNewRegistration(registrationRequest.registration(), registrationRequest.guests(), preexistingParticipant.list().get(0).getId());
    }

    private boolean isPacketIncomplete(P registrationRequest) {
        if (registrationRequest.participant() == null || registrationRequest.guests() == null || registrationRequest.registration() == null) {
            LOGGER.error("Attempted to insert a registration packet with null values. Aborting.");
            return true;
        }
        return false;
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

    @Override
    public int updateExisting(P update) {
        if (update.participant() == null || update.participant().getId() == null
        || update.guests() == null || Arrays.stream(update.guests()).anyMatch(guest -> guest.getId() == null)
        || update.registration() == null || update.registration().getId() == null) {
            return 0;
        }
        int updatedParticipants = participantLogic.updateExisting(update.participant());
        int updatedRegistrations = registrationLogic.updateExisting(update.registration());
        int updatedGuests = Arrays.stream(update.guests()).map(guestLogic::updateExisting).reduce(0, Integer::sum);
        return updatedParticipants + updatedRegistrations + updatedGuests;
    }

    private Optional<RegistrationPacket> buildRegistrationPacket(Registration fetchedRegistration) {
        return Optional.ofNullable(fetchedRegistration.getId())
                .map(Object::toString)
                .flatMap(registrationId -> participantLogic.fetchById(fetchedRegistration.getParticipantId())
                .map(fetchedParticipant -> new RegistrationPacket(fetchedParticipant, fetchGuestsByRegistrationId(registrationId), fetchedRegistration)));
    }

    private Guest[] fetchGuestsByRegistrationId(String registrationId) {
        return guestLogic.fetchByCriteria(true, Map.of(GUEST.REGISTRATION_ID.getName(), registrationId), null, false, 0, 0).list().toArray(Guest[]::new);
    }
}
