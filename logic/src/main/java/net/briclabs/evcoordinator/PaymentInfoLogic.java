package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import net.briclabs.evcoordinator.generated.tables.records.PaymentInfoRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.PaymentInfo.PAYMENT_INFO;

public class PaymentInfoLogic<P extends PaymentInfo> extends Logic<PaymentInfoRecord, PaymentInfo, net.briclabs.evcoordinator.generated.tables.PaymentInfo> implements WriteLogic<P> {
    public PaymentInfoLogic(DSLContext jooq) {
        super(jooq, PaymentInfo.class, PAYMENT_INFO, PAYMENT_INFO.ID);
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().PAYMENT_TYPE.getName(), pojo.getPaymentType()),
                entry(getTable().INSTRUMENT_TYPE.getName(), pojo.getInstrumentType()),
                entry(getTable().AMOUNT.getName(), pojo.getAmount().toPlainString()));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().PAYMENT_TYPE, pojo.getPaymentType())
                .set(getTable().INSTRUMENT_TYPE, pojo.getInstrumentType())
                .set(getTable().AMOUNT, pojo.getAmount())
                .returning(getIdColumn())
                .fetchOptional()
                .map(PaymentInfoRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().PAYMENT_TYPE, update.getPaymentType())
                .set(getTable().INSTRUMENT_TYPE, update.getInstrumentType())
                .set(getTable().AMOUNT, update.getAmount())
                .where(getIdColumn().equal(update.getId()))
                .and(
                        getTable().PAYMENT_TYPE.notEqual(update.getPaymentType())
                        .or(getTable().INSTRUMENT_TYPE.notEqual(update.getInstrumentType()))
                        .or(getTable().AMOUNT.notEqual(update.getAmount()))
                ).execute();
    }
}
