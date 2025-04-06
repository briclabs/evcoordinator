package net.briclabs.evcoordinator;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.Payment;
import net.briclabs.evcoordinator.generated.tables.pojos.PaymentWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.PaymentRecord;
import net.briclabs.evcoordinator.generated.tables.records.PaymentWithLabelsRecord;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Payment.PAYMENT;
import static net.briclabs.evcoordinator.generated.tables.PaymentWithLabels.PAYMENT_WITH_LABELS;

public class PaymentLogic<P extends Payment> extends Logic<PaymentRecord, Payment, net.briclabs.evcoordinator.generated.tables.Payment> implements WriteLogic<P>, DeletableRecord {
    private final HistoryLogic<DataHistory> historyLogic;

    private final PaymentWithLabelsLogic paymentWithLabelsLogic;

    public PaymentLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, Payment.class, PAYMENT, PAYMENT.ID);
        this.paymentWithLabelsLogic = new PaymentWithLabelsLogic(objectMapper, jooq);
        this.historyLogic = new HistoryLogic<>(objectMapper, new ParticipantLogic<>(objectMapper, jooq), jooq);
    }

    public PaymentWithLabelsLogic getPaymentWithLabelsLogic() {
        return paymentWithLabelsLogic;
    }

    @Override
    public boolean isAlreadyRecorded(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().PAYMENT_ACTION_TYPE.getName(), pojo.getPaymentActionType()),
                entry(getTable().ACTOR_ID.getName(), Long.toString(pojo.getActorId())),
                entry(getTable().RECIPIENT_ID.getName(), Long.toString(pojo.getRecipientId())),
                entry(getTable().PAYMENT_TYPE.getName(), pojo.getPaymentType()),
                entry(getTable().INSTRUMENT_TYPE.getName(), pojo.getInstrumentType()),
                entry(getTable().AMOUNT.getName(), pojo.getAmount().toPlainString()),
                entry(getTable().EVENT_INFO_ID.getName(), Long.toString(pojo.getEventInfoId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(long actorId, P pojo) {
        Optional<Long> insertedId = jooq
                .insertInto(getTable())
                .set(getTable().PAYMENT_ACTION_TYPE, pojo.getPaymentActionType())
                .set(getTable().ACTOR_ID, pojo.getActorId())
                .set(getTable().RECIPIENT_ID, pojo.getRecipientId())
                .set(getTable().PAYMENT_TYPE, pojo.getPaymentType())
                .set(getTable().INSTRUMENT_TYPE, pojo.getInstrumentType())
                .set(getTable().AMOUNT, pojo.getAmount())
                .set(getTable().EVENT_INFO_ID, pojo.getEventInfoId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(PaymentRecord::getId);
        if (insertedId.isPresent()) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.INSERTED.name(),
                    getTable().getName(),
                    convertToJson(pojo),
                    JSON.json("{}"),
                    null
            ));
        }
        return insertedId;
    }

    @Override
    public int updateExisting(long actorId, P update) {
        var originalRecord = fetchById(update.getId()).orElseThrow(() -> new RuntimeException("Payment not found: " + update.getId()));
        int updatedRecords = jooq
                .update(getTable())
                .set(getTable().PAYMENT_ACTION_TYPE, update.getPaymentActionType())
                .set(getTable().ACTOR_ID, update.getActorId())
                .set(getTable().RECIPIENT_ID, update.getRecipientId())
                .set(getTable().PAYMENT_TYPE, update.getPaymentType())
                .set(getTable().INSTRUMENT_TYPE, update.getInstrumentType())
                .set(getTable().AMOUNT, update.getAmount())
                .set(getTable().EVENT_INFO_ID, update.getEventInfoId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().PAYMENT_ACTION_TYPE.notEqual(update.getPaymentActionType())
                                .or(getTable().ACTOR_ID.notEqual(update.getActorId()))
                                .or(getTable().RECIPIENT_ID.notEqual(update.getRecipientId()))
                                .or(getTable().PAYMENT_TYPE.notEqual(update.getPaymentType()))
                                .or(getTable().INSTRUMENT_TYPE.notEqual(update.getInstrumentType()))
                                .or(getTable().AMOUNT.notEqual(update.getAmount()))
                                .or(getTable().EVENT_INFO_ID.notEqual(update.getEventInfoId()))
                ).execute();
        if (updatedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.UPDATED.name(),
                    getTable().getName(),
                    convertToJson(update),
                    convertToJson(originalRecord),
                    null
            ));
        }
        return updatedRecords;
    }

    @Override
    public void delete(long actorId, long idToDelete) {
        var originalRecord = fetchById(idToDelete).orElseThrow(() -> new RuntimeException("Payment not found: " + idToDelete));
        var deletedRecords = jooq.deleteFrom(getTable()).where(getTable().ID.eq(idToDelete)).execute();
        if (deletedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.DELETED.name(),
                    getTable().getName(),
                    JSON.json("{}"),
                    convertToJson(originalRecord),
                    null
            ));
        }
    }

    /**
     * Retrieves the list of payment IDs associated with the given event ID.
     *
     * @param eventId the ID of the event for which payment IDs need to be retrieved.
     * @return a list of payment IDs linked to the specified event ID.
     */
    public List<Long> getPaymentIdsByEventId(long eventId) {
        return jooq.select(getIdColumn()).from(getTable()).where(getTable().EVENT_INFO_ID.eq(eventId)).fetchInto(Long.class);
    }

    /**
     * Special logic class that works with a view instead of the raw table. The view provides useful label information for rows which, in the table, are simply FKs.
     */
    public static class PaymentWithLabelsLogic extends Logic<PaymentWithLabelsRecord, PaymentWithLabels, net.briclabs.evcoordinator.generated.tables.PaymentWithLabels> {
        public PaymentWithLabelsLogic(ObjectMapper objectMapper, DSLContext jooq) {
            super(objectMapper, jooq, PaymentWithLabels.class, PAYMENT_WITH_LABELS, PAYMENT_WITH_LABELS.ID);
        }
    }
}
