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
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().PAYMENT_ACTION_TYPE.getName(), pojo.getPaymentActionType()),
                entry(getTable().ACTOR_ID.getName(), Long.toString(pojo.getActorId())),
                entry(getTable().PAYMENT_ID.getName(), Long.toString(pojo.getPaymentId())),
                entry(getTable().RECIPIENT_ID.getName(), Long.toString(pojo.getRecipientId())),
                entry(getTable().EVENT_ID.getName(), Long.toString(pojo.getEventId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().PAYMENT_ACTION_TYPE, pojo.getPaymentActionType())
                .set(getTable().ACTOR_ID, pojo.getActorId())
                .set(getTable().PAYMENT_ID, pojo.getPaymentId())
                .set(getTable().RECIPIENT_ID, pojo.getRecipientId())
                .set(getTable().EVENT_ID, pojo.getEventId())
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
                .set(getTable().PAYMENT_ID, update.getPaymentId())
                .set(getTable().RECIPIENT_ID, update.getRecipientId())
                .set(getTable().EVENT_ID, update.getEventId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().PAYMENT_ACTION_TYPE.notEqual(update.getPaymentActionType())
                        .or(getTable().ACTOR_ID.notEqual(update.getActorId()))
                        .or(getTable().PAYMENT_ID.notEqual(update.getPaymentId()))
                        .or(getTable().RECIPIENT_ID.notEqual(update.getRecipientId()))
                        .or(getTable().EVENT_ID.notEqual(update.getEventId()))
                ).execute();
    }
}
