package net.briclabs.evcoordinator;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.TransactionWithLabels;
import net.briclabs.evcoordinator.generated.tables.pojos.Transaction_;
import net.briclabs.evcoordinator.generated.tables.records.TransactionWithLabelsRecord;
import net.briclabs.evcoordinator.generated.tables.records.Transaction_Record;
import net.briclabs.evcoordinator.validation.TransactionValidator;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.impl.DSL;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.TransactionWithLabels.TRANSACTION_WITH_LABELS;
import static net.briclabs.evcoordinator.generated.tables.Transaction_.TRANSACTION_;

public class TransactionLogic extends WriteAndDeleteLogic<Transaction_Record, Transaction_, net.briclabs.evcoordinator.generated.tables.Transaction_> {
    private final HistoryLogic historyLogic;

    public TransactionLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, Transaction_.class, TRANSACTION_, TRANSACTION_.ID);
        this.historyLogic = new HistoryLogic(objectMapper, jooq);
    }

    @Override
    public boolean isAlreadyRecorded(Transaction_ pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().ACTOR_ID.getName(), Long.toString(pojo.getActorId())),
                entry(getTable().RECIPIENT_ID.getName(), Long.toString(pojo.getRecipientId())),
                entry(getTable().TRANSACTION_TYPE.getName(), pojo.getTransactionType()),
                entry(getTable().INSTRUMENT_TYPE.getName(), pojo.getInstrumentType()),
                entry(getTable().AMOUNT.getName(), pojo.getAmount().toPlainString()),
                entry(getTable().MEMO.getName(), StringUtils.defaultString(pojo.getMemo(), "")),
                entry(getTable().EVENT_INFO_ID.getName(), Long.toString(pojo.getEventInfoId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(long actorId, Transaction_ pojo) {
        Optional<Long> insertedId = jooq
                .insertInto(getTable())
                .set(getTable().ACTOR_ID, pojo.getActorId())
                .set(getTable().RECIPIENT_ID, pojo.getRecipientId())
                .set(getTable().TRANSACTION_TYPE, pojo.getTransactionType())
                .set(getTable().INSTRUMENT_TYPE, pojo.getInstrumentType())
                .set(getTable().AMOUNT, pojo.getAmount())
                .set(getTable().MEMO, pojo.getMemo())
                .set(getTable().EVENT_INFO_ID, pojo.getEventInfoId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(Transaction_Record::getId);
        if (insertedId.isPresent()) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.INSERTED.name(),
                    getTable().getName().toUpperCase(),
                    convertToJson(pojo),
                    JSON.json("{}"),
                    null
            ));
        }
        return insertedId;
    }

    @Override
    public int updateExisting(long actorId, Transaction_ update) throws TransactionException {
        if (update.getId() == null) {
            throw new TransactionException(
                    new AbstractMap.SimpleImmutableEntry<>(GENERAL_MESSAGE_KEY, "ID to update was missing. Please review your input and try again."),
                    "ID %d to update was missing.".formatted(update.getId()));
        }
        var originalRecord = fetchById(update.getId()).orElseThrow(() -> new TransactionException(
                new AbstractMap.SimpleImmutableEntry<>(GENERAL_MESSAGE_KEY, "Record to update was not found. Please review your input and try again."),
                "Record %d to update was not found.".formatted(update.getId())));
        int updatedRecords = jooq
                .update(getTable())
                .set(getTable().ACTOR_ID, update.getActorId())
                .set(getTable().RECIPIENT_ID, update.getRecipientId())
                .set(getTable().TRANSACTION_TYPE, update.getTransactionType())
                .set(getTable().INSTRUMENT_TYPE, update.getInstrumentType())
                .set(getTable().AMOUNT, update.getAmount())
                .set(getTable().MEMO, update.getMemo())
                .set(getTable().EVENT_INFO_ID, update.getEventInfoId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().ACTOR_ID.notEqual(update.getActorId())
                                .or(getTable().RECIPIENT_ID.notEqual(update.getRecipientId()))
                                .or(getTable().TRANSACTION_TYPE.notEqual(update.getTransactionType()))
                                .or(getTable().INSTRUMENT_TYPE.notEqual(update.getInstrumentType()))
                                .or(getTable().AMOUNT.notEqual(update.getAmount()))
                                .or(DSL.coalesce(getTable().MEMO, "").notEqual(update.getMemo()))
                                .or(getTable().EVENT_INFO_ID.notEqual(update.getEventInfoId()))
                ).execute();
        if (updatedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.UPDATED.name(),
                    getTable().getName().toUpperCase(),
                    convertToJson(update),
                    convertToJson(originalRecord),
                    null
            ));
        }
        return updatedRecords;
    }

    @Override
    public void delete(long actorId, long idToDelete) throws TransactionException {
        var originalRecord = fetchById(idToDelete).orElseThrow(() -> new TransactionException(
                new AbstractMap.SimpleImmutableEntry<>(GENERAL_MESSAGE_KEY, "Transaction to delete was not found. Please review your input and try again."),
                "Transaction with ID %d to be deleted was not found.".formatted(idToDelete)));
        var deletedRecords = jooq.deleteFrom(getTable()).where(getTable().ID.eq(idToDelete)).execute();
        if (deletedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.DELETED.name(),
                    getTable().getName().toUpperCase(),
                    JSON.json("{}"),
                    convertToJson(originalRecord),
                    null
            ));
        }
    }

    @Override
    public Map<String, String> validate(Transaction_ pojo) {
        return TransactionValidator.of(pojo).getMessages();
    }

    /**
     * Retrieves the list of transaction IDs associated with the given event ID.
     *
     * @param eventId the ID of the event for which transaction IDs need to be retrieved.
     * @return a list of transaction IDs linked to the specified event ID.
     */
    public List<Long> getTransactionIdsByEventId(long eventId) {
        return jooq.select(getIdColumn()).from(getTable()).where(getTable().EVENT_INFO_ID.eq(eventId)).fetchInto(Long.class);
    }

    /**
     * Special logic class that works with a view instead of the raw table. The view provides useful label information for rows which, in the table, are simply FKs.
     */
    public static class TransactionWithLabelsLogic extends Logic<TransactionWithLabelsRecord, TransactionWithLabels, net.briclabs.evcoordinator.generated.tables.TransactionWithLabels> {
        public TransactionWithLabelsLogic(ObjectMapper objectMapper, DSLContext jooq) {
            super(objectMapper, jooq, TransactionWithLabels.class, TRANSACTION_WITH_LABELS, TRANSACTION_WITH_LABELS.ID);
        }
    }

    public static class TransactionException extends LogicException {
        public TransactionException(Map.Entry<String, String> publicMessage, String troubleshootingMessage) {
            super(publicMessage, troubleshootingMessage);
        }
    }
}
