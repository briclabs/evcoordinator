package net.briclabs.evcoordinator;


import net.briclabs.evcoordinator.generated.Tables;
import net.briclabs.evcoordinator.generated.tables.pojos.Payment;
import net.briclabs.evcoordinator.generated.tables.records.PaymentRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Payment.PAYMENT;

public class PaymentLogic extends Logic {

    public PaymentLogic(DSLContext jooq) {
        super(jooq);
    }

    public Optional<PaymentRecord> fetchById(Long id) {
        return jooq.selectFrom(PAYMENT).where(PAYMENT.ID.eq(id)).fetchOptional();
    }

    public List<PaymentRecord> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {

        List<Condition> matchConditions = new ArrayList<>();

        // Strip out all map entries with keys not applicable to this table, and all map entries with blank values.
        searchCriteria.entrySet().stream()
                .filter(e -> StringUtils.isBlank(e.getValue().trim()) || !PAYMENT.fieldStream().map(Field::getName).toList().contains(e.getKey()))
                .map(Map.Entry::getKey)
                .forEach(searchCriteria::remove);

        for (Map.Entry<String, String> searchParam : searchCriteria.entrySet()) {
            String key = searchParam.getKey();
            String value = searchParam.getValue();

            conditionallyAddMatchConditions(PAYMENT.ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PAYMENT.PAYMENT_ACTION_TYPE, key, value, matchConditions);
            conditionallyAddMatchConditions(PAYMENT.ACTOR_ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PAYMENT.PAYMENT_ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PAYMENT.RECIPIENT_ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PAYMENT.EVENT_ID, key, Long.parseLong(value), matchConditions);
        }

        return jooq.selectFrom(PAYMENT).where(matchConditions).orderBy(PAYMENT.ID).limit(offset, max).stream().toList();
    }

    public Optional<Long> insertNew(Payment pojo) {
        return jooq.insertInto(PAYMENT)
                .set(PAYMENT.PAYMENT_ACTION_TYPE, pojo.getPaymentActionType())
                .set(PAYMENT.ACTOR_ID, pojo.getActorId())
                .set(PAYMENT.PAYMENT_ID, pojo.getPaymentId())
                .set(PAYMENT.RECIPIENT_ID, pojo.getRecipientId())
                .set(PAYMENT.EVENT_ID, pojo.getEventId())
                .set(PAYMENT.TIME_RECORDED, Logic.getNow())
                .returning(PAYMENT.ID)
                .fetchOptional()
                .map(PaymentRecord::getId);
    }

    public Long updateExisting(PaymentRecord update, PaymentRecord existing) {
        if (!isUnchanged(update, existing)) {
            jooq.update(PAYMENT)
                    .set(PAYMENT.PAYMENT_ACTION_TYPE, update.getPaymentActionType())
                    .set(PAYMENT.ACTOR_ID, update.getActorId())
                    .set(PAYMENT.PAYMENT_ID, update.getPaymentId())
                    .set(PAYMENT.RECIPIENT_ID, update.getRecipientId())
                    .set(PAYMENT.EVENT_ID, update.getEventId())
                    .set(PAYMENT.TIME_RECORDED, Logic.getNow())
                    .where(PAYMENT.ID.eq(existing.getId()))
                    .execute();
        }
        return update.getId();
    }

    public Optional<Long> isUpdateRedundant(Long id, Payment update) {
        List<PaymentRecord> matchingEvents = fetchByCriteria(getCriteriaFromPojo(update), 0, 1);

        if (!matchingEvents.isEmpty() && matchingEvents.get(0).get(Tables.PAYMENT.ID).equals(id)) {
            // No change needed; just return the ID.
            return Optional.of(id);
        }

        if (!matchingEvents.isEmpty()) {
            // An update would create a record with the same mutable field values.
            return Optional.of(matchingEvents.get(0).get(Tables.PAYMENT.ID));
        }
        return Optional.empty();
    }

    public boolean isUnchanged(PaymentRecord update, PaymentRecord existing) {
        return new EqualsBuilder()
                .append(update.getPaymentActionType(), existing.getPaymentActionType())
                .append(update.getActorId(), existing.getActorId())
                .append(update.getPaymentId(), existing.getPaymentId())
                .append(update.getRecipientId(), existing.getRecipientId())
                .append(update.getEventId(), existing.getEventId())
                .build();
    }

    public boolean isEntryAlreadyExists(Payment pojo) {
        return fetchByCriteria(getCriteriaFromPojo(pojo), 0, 1).size() > 0;
    }

    public Map<String, String> getCriteriaFromPojo(Payment pojo) {
        return Map.ofEntries(
                entry(PAYMENT.PAYMENT_ACTION_TYPE.getName(), pojo.getPaymentActionType()),
                entry(PAYMENT.ACTOR_ID.getName(), Long.toString(pojo.getActorId())),
                entry(PAYMENT.PAYMENT_ID.getName(), Long.toString(pojo.getPaymentId())),
                entry(PAYMENT.RECIPIENT_ID.getName(), Long.toString(pojo.getRecipientId())),
                entry(PAYMENT.EVENT_ID.getName(), Long.toString(pojo.getEventId())));
    }
}
