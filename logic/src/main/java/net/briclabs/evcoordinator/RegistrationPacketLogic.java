package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import net.briclabs.evcoordinator.model.RegistrationPacket;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION;
import static net.briclabs.evcoordinator.generated.tables.RegistrationParticipantAssociation.REGISTRATION_PARTICIPANT_ASSOCIATION;

public class RegistrationPacketLogic<P extends RegistrationPacket> extends Logic<RegistrationRecord, Registration, net.briclabs.evcoordinator.generated.tables.Registration> implements WriteLogic<P> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationPacketLogic.class);


    private final ParticipantLogic<Participant> participantLogic;
    private final RegistrationLogic<Registration> registrationLogic;
    private final ParticipantAssociationLogic<ParticipantAssociation> participantAssociationLogic;
    private final RegistrationParticipantAssociationLogic<RegistrationParticipantAssociation> registrationParticipantAssociationLogic;

    public RegistrationPacketLogic(DSLContext jooq) {
        super(jooq, Registration.class, REGISTRATION, REGISTRATION.ID);
        this.participantLogic = new ParticipantLogic<>(jooq);
        this.registrationLogic = new RegistrationLogic<>(jooq);
        this.participantAssociationLogic = new ParticipantAssociationLogic<>(jooq);
        this.registrationParticipantAssociationLogic = new RegistrationParticipantAssociationLogic<>(jooq);
    }

    /**
     * Fetches a registration packet based on the provided registration ID.
     * The method retrieves a registration record by its ID and, if a record exists,
     * builds a {@code RegistrationPacket} containing the associated participant details,
     * participant associations, and event registration information.
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
     * participant associations, and event registration information.
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
    public Optional<RegistrationPacket> fetchRegistrationPacketParticipantId(Long id) {
        return registrationLogic.fetchByCriteria(true, Map.of(REGISTRATION.PARTICIPANT_ID.getName(), id.toString()), null, false, 0, 1)
                .list().stream().findFirst().flatMap(this::buildRegistrationPacket);
    }

    /**
     * Because this is a compound action, newness is checked in the {@link #insertNew(RegistrationPacket)} method as each object must first be built before checking.
     * Therefore, this method does not serve any purpose.
     *
     * @param registrationRequest The Registration request to be validated as truly new.
     * @return true if the participant, associations, and event are all validated as new; otherwise, false.
     */
    @Override
    public boolean isAlreadyRecorded(P registrationRequest) {
        throw new UnsupportedOperationException("This method is not supported. The insertNew() method will perform this function over the course of its process.");
    }

    /**
     * This operation includes inserting the participant, their associations, and event details if all provided
     * data is valid and correctly processed.
     *
     * @param registrationRequest the registration to create.
     * @return An {@code Optional<Long>} containing the ID of the newly created event if all insertions were successful,
     *         or an empty {@code Optional} if any operation failed during the process.
     */
    @Override
    public Optional<Long> insertNew(P registrationRequest) {
        if (registrationRequest.participant() == null || registrationRequest.associations() == null || registrationRequest.registration() == null) {
            LOGGER.error("Attempted to insert a registration packet with null values. Aborting.");
            return Optional.empty();
        }

        Optional<Long> participantId = participantLogic.isAlreadyRecorded(registrationRequest.participant()) ? Optional.empty() : participantLogic.insertNew(registrationRequest.participant());
        if (participantId.isEmpty()) {
            LOGGER.error("Failed to insert a participant.");
            return Optional.empty();
        }

        var registrationToCreate = new Registration(null, participantId.get(), registrationRequest.registration().getDonationPledge(), registrationRequest.registration().getSignature(), registrationRequest.registration().getEventInfoId(), null);
        Optional<Long> registrationId = registrationLogic.isAlreadyRecorded(registrationToCreate) ? Optional.empty() : registrationLogic.insertNew(registrationToCreate);
        if (registrationId.isEmpty()) {
            LOGGER.error("Failed to insert a registration.");
            return Optional.empty();
        }

        for (var association : registrationRequest.associations()) {
            var associationToCreate = new ParticipantAssociation(null, participantId.get(), association.getRawAssociateName(), null, association.getAssociation(), null);
            Optional<Long> associationId = participantAssociationLogic.isAlreadyRecorded(associationToCreate) ? Optional.empty() : participantAssociationLogic.insertNew(associationToCreate);
            if (associationId.isEmpty()) {
                LOGGER.error("Failed to insert a participant association.");
                return Optional.empty();
            }

            if (registrationParticipantAssociationLogic.insertNew(new RegistrationParticipantAssociation(null, associationId.get(), registrationId.get())).isEmpty()) {
                LOGGER.error("Failed to insert a registration participant association.");
                return Optional.empty();
            }
            LOGGER.info("Successfully inserted participant association ID {} for registration ID {}.", associationId.get(), registrationId.get());
        }

        LOGGER.info("Successfully inserted participant ID {} and registration ID {}.", participantId.get(), registrationId.get());
        return registrationId;
    }

    @Override
    public int updateExisting(P update) {
        if (update.participant() == null || update.participant().getId() == null
        || update.associations() == null || Arrays.stream(update.associations()).anyMatch(association -> association.getId() == null)
        || update.registration() == null || update.registration().getId() == null) {
            return 0;
        }
        int updatedParticipants = participantLogic.updateExisting(update.participant());
        int updatedRegistrations = registrationLogic.updateExisting(update.registration());
        int updatedAssociations = Arrays.stream(update.associations()).map(participantAssociationLogic::updateExisting).reduce(0, Integer::sum);
        return updatedParticipants + updatedRegistrations + updatedAssociations;
    }

    private Optional<RegistrationPacket> buildRegistrationPacket(Registration fetchedRegistration) {
        return Optional.ofNullable(fetchedRegistration.getId())
                .map(Object::toString)
                .flatMap(registrationId -> participantLogic.fetchById(fetchedRegistration.getParticipantId())
                .map(fetchedParticipant -> new RegistrationPacket(fetchedParticipant, fetchParticipantAssociationsByRegistrationId(registrationId), fetchedRegistration)));
    }

    private ParticipantAssociation[] fetchParticipantAssociationsByRegistrationId(String registrationId) {
        return registrationParticipantAssociationLogic.fetchByCriteria(true, Map.of(REGISTRATION_PARTICIPANT_ASSOCIATION.REGISTRATION_ID.getName(), registrationId), null, false, 0, 1).list().stream()
                .map(fetchedAssociation -> participantAssociationLogic.fetchById(fetchedAssociation.getParticipantAssociationId()))
                .flatMap(Optional::stream)
                .toArray(ParticipantAssociation[]::new);
    }
}
