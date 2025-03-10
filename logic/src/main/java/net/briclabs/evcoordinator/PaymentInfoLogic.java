package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import net.briclabs.evcoordinator.generated.tables.records.PaymentInfoRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.PaymentInfo.PAYMENT_INFO;

public class PaymentInfoLogic<P extends PaymentInfo> extends Logic implements WriteLogic<P> {

    private static final Map<String, Field<?>> FIELDS = Map.ofEntries(
            Map.entry("ID", PAYMENT_INFO.ID),
            Map.entry("AMOUNT", PAYMENT_INFO.AMOUNT),
            Map.entry("PAYMENT_TYPE", PAYMENT_INFO.PAYMENT_TYPE),
            Map.entry("INSTRUMENT_TYPE", PAYMENT_INFO.INSTRUMENT_TYPE),
            Map.entry("TIME_RECORDED", PAYMENT_INFO.TIME_RECORDED)
    );

    public PaymentInfoLogic(DSLContext jooq) {
        super(jooq);
    }

    static List<Condition> parseCriteriaIntoConditions(boolean exactCriteria, Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, PAYMENT_INFO);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(PAYMENT_INFO.PAYMENT_TYPE, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT_INFO.INSTRUMENT_TYPE, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT_INFO.AMOUNT, key, value, exactCriteria).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(PAYMENT_INFO.PAYMENT_TYPE.getName(), pojo.getPaymentType()),
                entry(PAYMENT_INFO.INSTRUMENT_TYPE.getName(), pojo.getInstrumentType()),
                entry(PAYMENT_INFO.AMOUNT.getName(), pojo.getAmount().toPlainString()));
        return fetchByCriteria(true, criteria, PAYMENT_INFO.ID.getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<PaymentInfo> fetchById(Long id) {
        return jooq
                .selectFrom(PAYMENT_INFO)
                .where(PAYMENT_INFO.ID.eq(id))
                .fetchOptionalInto(PaymentInfo.class);
    }

    @Override
    public Field<?> resolveField(String columnName, Field<?> defaultField) {
        return FIELDS.getOrDefault(columnName, defaultField);
    }

    @Override
    public ListWithCount<PaymentInfo> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, int offset, int max) {
        List<Condition> conditions = parseCriteriaIntoConditions(exactCriteria, searchCriteria);

        List<PaymentInfo> results = jooq
                .selectFrom(PAYMENT_INFO)
                .where(buildWhereClause(exactCriteria, conditions))
                .orderBy(sortAscending
                        ? resolveField(sortColumn, PAYMENT_INFO.ID).asc()
                        : resolveField(sortColumn, PAYMENT_INFO.ID).desc())
                .limit(offset, max)
                .fetchStreamInto(PaymentInfo.class)
                .toList();
        int count = jooq
                .selectCount()
                .from(PAYMENT_INFO)
                .where(conditions)
                .fetchOptional(0, Integer.class)
                .orElse(0);
        return new ListWithCount<>(results, count);
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(PAYMENT_INFO)
                .set(PAYMENT_INFO.PAYMENT_TYPE, pojo.getPaymentType())
                .set(PAYMENT_INFO.INSTRUMENT_TYPE, pojo.getInstrumentType())
                .set(PAYMENT_INFO.AMOUNT, pojo.getAmount())
                .returning(PAYMENT_INFO.ID)
                .fetchOptional()
                .map(PaymentInfoRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(PAYMENT_INFO)
                .set(PAYMENT_INFO.PAYMENT_TYPE, update.getPaymentType())
                .set(PAYMENT_INFO.INSTRUMENT_TYPE, update.getInstrumentType())
                .set(PAYMENT_INFO.AMOUNT, update.getAmount())
                .where(PAYMENT_INFO.ID.equal(update.getId()))
                .and(
                            PAYMENT_INFO.PAYMENT_TYPE.notEqual(update.getPaymentType())
                        .or(PAYMENT_INFO.INSTRUMENT_TYPE.notEqual(update.getInstrumentType()))
                        .or(PAYMENT_INFO.AMOUNT.notEqual(update.getAmount()))
                ).execute();
    }
}
