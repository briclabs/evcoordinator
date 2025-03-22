package net.briclabs.evcoordinator;


import net.briclabs.evcoordinator.generated.tables.pojos.Payment;
import net.briclabs.evcoordinator.generated.tables.records.PaymentRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Payment.PAYMENT;

public class PaymentLogic<P extends Payment> extends Logic<PaymentRecord, Payment, net.briclabs.evcoordinator.generated.tables.Payment> implements WriteLogic<P> {
    public PaymentLogic(DSLContext jooq) {
        super(jooq, Payment.class, PAYMENT, PAYMENT.ID);
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
    public Optional<Long> insertNew(P pojo) {
        return jooq
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
    }

    @Override
    public int updateExisting(P update) {
        return jooq
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
    }
}
