package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.Tables;
import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import net.briclabs.evcoordinator.generated.tables.records.PaymentInfoRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.tools.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.PaymentInfo.PAYMENT_INFO;

public class PaymentInfoLogic extends Logic {

    public PaymentInfoLogic(DSLContext jooq) {
        super(jooq);
    }

    public Optional<PaymentInfoRecord> fetchById(Long id) {
        return jooq.selectFrom(PAYMENT_INFO).where(PAYMENT_INFO.ID.eq(id)).fetchOptional();
    }

    public List<PaymentInfoRecord> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {

        List<Condition> matchConditions = new ArrayList<>();

        // Strip out all map entries with keys not applicable to this table, and all map entries with blank values.
        searchCriteria.entrySet().stream()
                .filter(e -> StringUtils.isBlank(e.getValue().trim()) || !PAYMENT_INFO.fieldStream().map(Field::getName).toList().contains(e.getKey()))
                .map(Map.Entry::getKey)
                .forEach(searchCriteria::remove);

        for (Map.Entry<String, String> searchParam : searchCriteria.entrySet()) {
            String key = searchParam.getKey();
            String value = searchParam.getValue();

            conditionallyAddMatchConditions(PAYMENT_INFO.ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(PAYMENT_INFO.PAYMENT_TYPE, key, value, matchConditions);
            conditionallyAddMatchConditions(PAYMENT_INFO.INSTRUMENT_TYPE, key, value, matchConditions);
            conditionallyAddMatchConditions(PAYMENT_INFO.AMOUNT, key, new BigDecimal(value), matchConditions);
        }

        return jooq.selectFrom(PAYMENT_INFO).where(matchConditions).orderBy(PAYMENT_INFO.ID).limit(offset, max).stream().toList();
    }

    public Optional<Long> insertNew(PaymentInfo pojo) {
        return jooq.insertInto(PAYMENT_INFO)
                .set(PAYMENT_INFO.PAYMENT_TYPE, pojo.getPaymentType())
                .set(PAYMENT_INFO.INSTRUMENT_TYPE, pojo.getInstrumentType())
                .set(PAYMENT_INFO.AMOUNT, pojo.getAmount())
                .set(PAYMENT_INFO.TIME_RECORDED, Logic.getNow())
                .returning(PAYMENT_INFO.ID)
                .fetchOptional()
                .map(PaymentInfoRecord::getId);
    }

    public Long updateExisting(PaymentInfoRecord update, PaymentInfoRecord existing) {
        if (!isUnchanged(update, existing)) {
            jooq.update(PAYMENT_INFO)
                    .set(PAYMENT_INFO.PAYMENT_TYPE, update.getPaymentType())
                    .set(PAYMENT_INFO.INSTRUMENT_TYPE, update.getInstrumentType())
                    .set(PAYMENT_INFO.AMOUNT, update.getAmount())
                    .set(PAYMENT_INFO.TIME_RECORDED, Logic.getNow())
                    .where(PAYMENT_INFO.ID.eq(existing.getId()))
                    .execute();
        }
        return update.getId();
    }

    public Optional<Long> isUpdateRedundant(Long id, PaymentInfo update) {
        List<PaymentInfoRecord> matchingEvents = fetchByCriteria(getCriteriaFromPojo(update), 0, 1);

        if (!matchingEvents.isEmpty() && matchingEvents.get(0).get(Tables.PAYMENT_INFO.ID).equals(id)) {
            // No change needed; just return the ID.
            return Optional.of(id);
        }

        if (!matchingEvents.isEmpty()) {
            // An update would create a record with the same mutable field values.
            return Optional.of(matchingEvents.get(0).get(Tables.PAYMENT_INFO.ID));
        }
        return Optional.empty();
    }

    public boolean isUnchanged(PaymentInfoRecord update, PaymentInfoRecord existing) {
        return new EqualsBuilder()
                .append(update.getPaymentType(), existing.getPaymentType())
                .append(update.getPaymentType(), existing.getPaymentType())
                .append(update.getInstrumentType(), existing.getInstrumentType())
                .append(update.getAmount(), existing.getAmount())
                .build();
    }

    public boolean isEntryAlreadyExists(PaymentInfo pojo) {
        return fetchByCriteria(getCriteriaFromPojo(pojo), 0, 1).size() > 0;
    }

    public Map<String, String> getCriteriaFromPojo(PaymentInfo pojo) {
        return Map.ofEntries(
                entry(PAYMENT_INFO.PAYMENT_TYPE.getName(), pojo.getPaymentType()),
                entry(PAYMENT_INFO.INSTRUMENT_TYPE.getName(), pojo.getInstrumentType()),
                entry(PAYMENT_INFO.AMOUNT.getName(), pojo.getAmount().toPlainString()));
    }
}
