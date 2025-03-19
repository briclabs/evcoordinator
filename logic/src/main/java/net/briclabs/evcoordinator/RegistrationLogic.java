package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Event;
import net.briclabs.evcoordinator.generated.tables.pojos.EventParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import org.jooq.DSLContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public class RegistrationLogic {
    public static final String EVENT_ACTION_REGISTERED = "REGISTERED";
    private final ParticipantLogic<Participant> participantLogic;
    private final ParticipantAssociationLogic<ParticipantAssociation> participantAssociationLogic;
    private final EventParticipantAssociationLogic<EventParticipantAssociation> eventParticipantAssociationLogic;
    private final EventLogic<net.briclabs.evcoordinator.generated.tables.pojos.Event> eventLogic;

    public RegistrationLogic(DSLContext jooq) {
        this.participantLogic = new ParticipantLogic<>(jooq);
        this.participantAssociationLogic = new ParticipantAssociationLogic<>(jooq);
        this.eventParticipantAssociationLogic = new EventParticipantAssociationLogic<>(jooq);
        this.eventLogic = new EventLogic<>(jooq);
    }

    /**
     * Validates whether a participant, their associations, and event-related information are truly new entries.
     *
     * @param participant The participant to be validated.
     * @param associations An array of participant associations to validate.
     * @param eventInfoId The identifier of the associated event information.
     * @param donationPledge The amount pledged for the donation.
     * @param signature The signature associated with the participant or event.
     * @return true if the participant, associations, and event are all validated as new; otherwise, false.
     */
    public boolean validateIsTrulyNew(
            Participant participant,
            ParticipantAssociation[] associations,
            Long eventInfoId,
            BigDecimal donationPledge,
            String signature
    ) {
        if (participant.getId() == null) {
            return false;
        }

        var participantIsNew = participantLogic.validateIsTrulyNew(participant);
        boolean associationsAreNew = Arrays.stream(associations).map(participantAssociationLogic::validateIsTrulyNew).reduce(true, (a, b) -> a && b);
        var eventIsNew = eventLogic.validateIsTrulyNew(new Event(null, participant.getId(), donationPledge, signature, EVENT_ACTION_REGISTERED, eventInfoId, null));
        return participantIsNew && associationsAreNew && eventIsNew;
    }

    /**
     * This operation includes inserting the participant, their associations, and event details if all provided
     * data is valid and correctly processed.
     *
     * @param participant The participant to be inserted into the system.
     * @param associations An array of participant associations to be associated with the participant.
     * @param eventInfoId The identifier representing the associated event information.
     * @param donationPledge The pledged donation amount for the event.
     * @param signature A signature used to record or verify the transaction or registration.
     * @return An {@code Optional<Long>} containing the ID of the newly created event if all insertions were successful,
     *         or an empty {@code Optional} if any operation failed during the process.
     */
    public Optional<Long> insertNew(
        Participant participant,
        ParticipantAssociation[] associations,
        Long eventInfoId,
        BigDecimal donationPledge,
        String signature
    ) {
        if (participant == null || associations == null || eventInfoId == null || donationPledge == null || signature == null) {
            return Optional.empty();
        }

        var participantId = participantLogic.insertNew(participant);
        if (participantId.isEmpty()) {
            return Optional.empty();
        }

        var eventId = eventLogic.insertNew(new Event(null, participantId.get(), donationPledge, signature, EVENT_ACTION_REGISTERED, eventInfoId, null));
        if (eventId.isEmpty()) {
            return Optional.empty();
        }

        for (var association : associations) {
            var completedAssociation = new ParticipantAssociation(null, participantId.get(), association.getRawAssociateName(), null, association.getAssociation(), null);
            var associationId = participantAssociationLogic.insertNew(completedAssociation);
            if (associationId.isEmpty()) {
                return Optional.empty();
            }

            var eventAssociationId = eventParticipantAssociationLogic.insertNew(new EventParticipantAssociation(null, associationId.get(), eventId.get()));
            if (eventAssociationId.isEmpty()) {
                return Optional.empty();
            }
        }

        return eventId;
    }
}
