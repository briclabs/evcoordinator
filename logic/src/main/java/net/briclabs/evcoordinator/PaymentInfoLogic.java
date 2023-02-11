package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import net.briclabs.evcoordinator.generated.tables.records.PaymentInfoRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.PaymentInfo.PAYMENT_INFO;

public class PaymentInfoLogic<P extends PaymentInfo> extends Logic implements WriteLogic<P> {

    static List<Condition> parseCriteriaIntoConditions(Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, PAYMENT_INFO);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(PAYMENT_INFO.ID, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT_INFO.PAYMENT_TYPE, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT_INFO.INSTRUMENT_TYPE, key, value).ifPresent(matchConditions::add);
            addPossibleCondition(PAYMENT_INFO.AMOUNT, key, value).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    public PaymentInfoLogic(DSLContext jooq) {
        super(jooq);
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(PAYMENT_INFO.PAYMENT_TYPE.getName(), pojo.getPaymentType()),
                entry(PAYMENT_INFO.INSTRUMENT_TYPE.getName(), pojo.getInstrumentType()),
                entry(PAYMENT_INFO.AMOUNT.getName(), pojo.getAmount().toPlainString()));
        return fetchByCriteria(criteria, 0, 1).size() > 0;
    }

    @Override
    public Optional<PaymentInfo> fetchById(Long id) {
        return jooq
                .selectFrom(PAYMENT_INFO)
                .where(PAYMENT_INFO.ID.eq(id))
                .fetchOptionalInto(PaymentInfo.class);
    }

    @Override
    public List<PaymentInfo> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {
        return jooq
                .selectFrom(PAYMENT_INFO)
                .where(parseCriteriaIntoConditions(searchCriteria))
                .orderBy(PAYMENT_INFO.ID)
                .limit(offset, max)
                .fetchStreamInto(PaymentInfo.class)
                .toList();
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
