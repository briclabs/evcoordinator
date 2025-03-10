package net.briclabs.evcoordinator;


import net.briclabs.evcoordinator.generated.tables.pojos.Payment;
import net.briclabs.evcoordinator.generated.tables.records.PaymentRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Payment.PAYMENT;

public class PaymentLogic<P extends Payment> extends Logic implements WriteLogic<P> {

    private static final Map<String, Field<?>> FIELDS = Map.ofEntries(
            Map.entry("ID", PAYMENT.ID),
            Map.entry("EVENT_ID", PAYMENT.EVENT_ID),
            Map.entry("ACTOR_ID", PAYMENT.ACTOR_ID),
            Map.entry("PAYMENT_ACTION_TYPE", PAYMENT.PAYMENT_ACTION_TYPE),
            Map.entry("RECIPIENT_ID", PAYMENT.RECIPIENT_ID),
            Map.entry("PAYMENT_ID", PAYMENT.PAYMENT_ID),
            Map.entry("TIME_RECORDED", PAYMENT.TIME_RECORDED)
    );

    public PaymentLogic(DSLContext jooq) {
        super(jooq);
    }

    static List<Condition> parseCriteriaIntoConditions(boolean exactCriteria, Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, PAYMENT);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(PAYMENT.PAYMENT_ACTION_TYPE, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT.ACTOR_ID, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT.PAYMENT_ID, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT.RECIPIENT_ID, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT.EVENT_ID, key, value, exactCriteria).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(PAYMENT.PAYMENT_ACTION_TYPE.getName(), pojo.getPaymentActionType()),
                entry(PAYMENT.ACTOR_ID.getName(), Long.toString(pojo.getActorId())),
                entry(PAYMENT.PAYMENT_ID.getName(), Long.toString(pojo.getPaymentId())),
                entry(PAYMENT.RECIPIENT_ID.getName(), Long.toString(pojo.getRecipientId())),
                entry(PAYMENT.EVENT_ID.getName(), Long.toString(pojo.getEventId())));
        return fetchByCriteria(true, criteria, PAYMENT.ID.getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Payment> fetchById(Long id) {
        return jooq
                .selectFrom(PAYMENT)
                .where(PAYMENT.ID.eq(id))
                .fetchOptionalInto(Payment.class);
    }

    @Override
    public Field<?> resolveField(String columnName, Field<?> defaultField) {
        return FIELDS.getOrDefault(columnName, defaultField);
    }

    @Override
    public ListWithCount<Payment> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, int offset, int max) {
        List<Condition> conditions = parseCriteriaIntoConditions(exactCriteria, searchCriteria);

        List<Payment> results = jooq
                .selectFrom(PAYMENT)
                .where(buildWhereClause(exactCriteria, conditions))
                .orderBy(sortAscending
                        ? resolveField(sortColumn, PAYMENT.ID).asc()
                        : resolveField(sortColumn, PAYMENT.ID).desc())
                .limit(offset, max)
                .fetchStreamInto(Payment.class)
                .toList();
        int count = jooq
                .selectCount()
                .from(PAYMENT)
                .where(conditions)
                .fetchOptional(0, Integer.class)
                .orElse(0);
        return new ListWithCount<>(results, count);
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(PAYMENT)
                .set(PAYMENT.PAYMENT_ACTION_TYPE, pojo.getPaymentActionType())
                .set(PAYMENT.ACTOR_ID, pojo.getActorId())
                .set(PAYMENT.PAYMENT_ID, pojo.getPaymentId())
                .set(PAYMENT.RECIPIENT_ID, pojo.getRecipientId())
                .set(PAYMENT.EVENT_ID, pojo.getEventId())
                .returning(PAYMENT.ID)
                .fetchOptional()
                .map(PaymentRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(PAYMENT)
                .set(PAYMENT.PAYMENT_ACTION_TYPE, update.getPaymentActionType())
                .set(PAYMENT.ACTOR_ID, update.getActorId())
                .set(PAYMENT.PAYMENT_ID, update.getPaymentId())
                .set(PAYMENT.RECIPIENT_ID, update.getRecipientId())
                .set(PAYMENT.EVENT_ID, update.getEventId())
                .where(PAYMENT.ID.eq(update.getId()))
                .and(
                        PAYMENT.PAYMENT_ACTION_TYPE.notEqual(update.getPaymentActionType())
                        .or(PAYMENT.ACTOR_ID.notEqual(update.getActorId()))
                        .or(PAYMENT.PAYMENT_ID.notEqual(update.getPaymentId()))
                        .or(PAYMENT.RECIPIENT_ID.notEqual(update.getRecipientId()))
                        .or(PAYMENT.EVENT_ID.notEqual(update.getEventId()))
                ).execute();
    }
}
