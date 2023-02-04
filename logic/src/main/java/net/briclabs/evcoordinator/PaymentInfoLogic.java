package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import net.briclabs.evcoordinator.generated.tables.records.PaymentInfoRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.PaymentInfo.PAYMENT_INFO;

public class PaymentInfoLogic extends Logic {

    static List<Condition> parseCriteriaIntoConditions(Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, PAYMENT_INFO);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            buildPossibleCondition(PAYMENT_INFO.ID, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(PAYMENT_INFO.PAYMENT_TYPE, key, value).ifPresent(matchConditions::add);
            buildPossibleCondition(PAYMENT_INFO.INSTRUMENT_TYPE, key, value).ifPresent(matchConditions::add);
            buildPossibleCondition(PAYMENT_INFO.AMOUNT, key, new BigDecimal(value)).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    public PaymentInfoLogic(DSLContext jooq) {
        super(jooq);
    }

    public boolean validateIsTrulyNew(PaymentInfo pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(PAYMENT_INFO.PAYMENT_TYPE.getName(), pojo.getPaymentType()),
                entry(PAYMENT_INFO.INSTRUMENT_TYPE.getName(), pojo.getInstrumentType()),
                entry(PAYMENT_INFO.AMOUNT.getName(), pojo.getAmount().toPlainString()));
        return fetchByCriteria(criteria, 0, 1).size() > 0;
    }

    public Optional<PaymentInfo> fetchById(Long id) {
        return jooq
                .selectFrom(PAYMENT_INFO)
                .where(PAYMENT_INFO.ID.eq(id))
                .fetchOptionalInto(PaymentInfo.class);
    }

    public List<PaymentInfo> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {
        return jooq
                .selectFrom(PAYMENT_INFO)
                .where(parseCriteriaIntoConditions(searchCriteria))
                .orderBy(PAYMENT_INFO.ID)
                .limit(offset, max)
                .fetchStreamInto(PaymentInfo.class)
                .toList();
    }

    public Optional<Long> insertNew(PaymentInfo pojo) {
        return jooq
                .insertInto(PAYMENT_INFO)
                .set(PAYMENT_INFO.PAYMENT_TYPE, pojo.getPaymentType())
                .set(PAYMENT_INFO.INSTRUMENT_TYPE, pojo.getInstrumentType())
                .set(PAYMENT_INFO.AMOUNT, pojo.getAmount())
                .returning(PAYMENT_INFO.ID)
                .fetchOptional()
                .map(PaymentInfoRecord::getId);
    }

    public int updateExisting(PaymentInfo update) {
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
