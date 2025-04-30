package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationWithLabelsRecord;
import net.briclabs.evcoordinator.validation.RegistrationValidator;
import org.jooq.DSLContext;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.Tables.GUEST;
import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION;
import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION_WITH_LABELS;

public class RegistrationLogic extends WriteAndDeleteLogic<RegistrationRecord, Registration, net.briclabs.evcoordinator.generated.tables.Registration> {
    private final HistoryLogic historyLogic;

    private final GuestLogic guestLogic;

    public RegistrationLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, Registration.class, REGISTRATION, REGISTRATION.ID);
        this.guestLogic = new GuestLogic(objectMapper, jooq);
        this.historyLogic = new HistoryLogic(objectMapper, jooq);
    }

    @Override
    public boolean isAlreadyRecorded(Registration registrationPojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().EVENT_INFO_ID.getName(), Long.toString(registrationPojo.getEventInfoId())),
                entry(getTable().SIGNATURE.getName(), registrationPojo.getSignature()),
                entry(getTable().DONATION_PLEDGE.getName(), registrationPojo.getDonationPledge().toPlainString()),
                entry(getTable().PARTICIPANT_ID.getName(), Long.toString(registrationPojo.getParticipantId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(long actorId, Registration pojo) {
        Optional<Long> insertedId = jooq
                .insertInto(getTable())
                .set(getTable().EVENT_INFO_ID, pojo.getEventInfoId())
                .set(getTable().SIGNATURE, pojo.getSignature())
                .set(getTable().DONATION_PLEDGE, pojo.getDonationPledge())
                .set(getTable().PARTICIPANT_ID, pojo.getParticipantId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(RegistrationRecord::getId);
        if (insertedId.isPresent()) {
            recordHistoryForInsert(historyLogic, actorId, convertToJson(pojo));
        }
        return insertedId;
    }

    @Override
    public int updateExisting(long actorId, Registration update) throws RegistrationException {
        if (update.getId() == null) {
            throw new RegistrationException(
                    new AbstractMap.SimpleImmutableEntry<>(GENERAL_MESSAGE_KEY, "ID to update was missing. Please review your input and try again."),
                    "ID %d to update was missing.".formatted(update.getId()));
        }
        var originalRecord = fetchById(update.getId()).orElseThrow(
                () -> new RegistrationException(
                        new AbstractMap.SimpleImmutableEntry<>(GENERAL_MESSAGE_KEY, "Record to update was not found. Please review your input and try again."),
                        "Record %d to update was not found.".formatted(update.getId())));
        int updatedRecords = jooq
                .update(getTable())
                .set(getTable().EVENT_INFO_ID, update.getEventInfoId())
                .set(getTable().SIGNATURE, update.getSignature())
                .set(getTable().DONATION_PLEDGE, update.getDonationPledge())
                .set(getTable().PARTICIPANT_ID, update.getParticipantId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().EVENT_INFO_ID.notEqual(update.getEventInfoId())
                                .or(getTable().SIGNATURE.notEqual(update.getSignature()))
                                .or(getTable().DONATION_PLEDGE.notEqual(update.getDonationPledge()))
                                .or(getTable().PARTICIPANT_ID.notEqual(update.getParticipantId()))
                )
                .execute();
        if (updatedRecords > 0) {
            recordHistoryForUpdate(historyLogic, actorId, convertToJson(originalRecord), convertToJson(update));
        }
        return updatedRecords;
    }

    @Override
    public void delete(long actorId, long idToDelete) throws RegistrationException, GuestLogic.GuestException {
        var originalRecord = fetchById(idToDelete).orElseThrow(() -> new RegistrationException(
                new AbstractMap.SimpleImmutableEntry<>(GENERAL_MESSAGE_KEY, "Registration to delete was not found. Please review your input and try again."),
                "Registration with ID %d was not found.".formatted(idToDelete)));
        deleteCorrespondingGuests(actorId, idToDelete);
        var deletedRecords = jooq.deleteFrom(getTable()).where(getTable().ID.eq(idToDelete)).execute();
        if (deletedRecords > 0) {
            recordHistoryForDeletion(historyLogic, actorId, convertToJson(originalRecord));
        }
    }

    @Override
    public Map<String, String> validate(Registration pojo) {
        return RegistrationValidator.of(pojo, false).getMessages();
    }

    private void deleteCorrespondingGuests(long actorId, long registrationIdToDelete) throws GuestLogic.GuestException {
        for (Long guestIdToDelete : jooq.select(GUEST.ID).from(GUEST).where(GUEST.REGISTRATION_ID.eq(registrationIdToDelete)).fetchInto(Long.class)) {
            guestLogic.delete(actorId, guestIdToDelete);
        }
    }

    /**
     * Special logic class that works with a view instead of the raw table. The view provides useful label information for rows which, in the table, are simply FKs.
     */
    public static class RegistrationWithLabelsLogic extends Logic<RegistrationWithLabelsRecord, RegistrationWithLabels, net.briclabs.evcoordinator.generated.tables.RegistrationWithLabels> {

        public RegistrationWithLabelsLogic(ObjectMapper objectMapper, DSLContext jooq) {
            super(objectMapper, jooq, RegistrationWithLabels.class, REGISTRATION_WITH_LABELS, REGISTRATION_WITH_LABELS.ID);
        }
    }

    public static class RegistrationException extends LogicException {
        public RegistrationException(Map.Entry<String, String> publicMessage, String troubleshootingMessage) {
            super(publicMessage, troubleshootingMessage);
        }
    }
}
