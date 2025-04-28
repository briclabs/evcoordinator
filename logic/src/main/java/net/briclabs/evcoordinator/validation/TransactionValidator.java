package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.generated.tables.pojos.Transaction_;
import net.briclabs.evcoordinator.generated.tables.records.Transaction_Record;

import java.math.BigDecimal;

public class TransactionValidator extends AbstractValidator<Transaction_Record, Transaction_, net.briclabs.evcoordinator.generated.tables.Transaction_> {

    private TransactionValidator(Transaction_ transaction) {
        super(net.briclabs.evcoordinator.generated.tables.Transaction_.TRANSACTION_, transaction);
    }

    public static TransactionValidator of(Transaction_ transaction) {
        return new TransactionValidator(transaction);
    }

    @Override
    void validate() {
        if (pojo().getEventInfoId() == null || pojo().getEventInfoId() < 0L) {
            addMessage(table().EVENT_INFO_ID, MUST_BE_VALID_VALUE);
        }
        if (pojo().getActorId() == null || pojo().getActorId() < 0L) {
            addMessage(table().ACTOR_ID, MUST_BE_VALID_VALUE);
        }
        if (pojo().getRecipientId() == null || pojo().getRecipientId() < 0L) {
            addMessage(table().RECIPIENT_ID, MUST_BE_VALID_VALUE);
        }
        if (pojo().getAmount() == null || pojo().getAmount().compareTo(BigDecimal.ZERO) < 0) {
            addMessage(table().AMOUNT, MUST_BE_POSITIVE_NUMBER);
        }
        if (pojo().getTransactionType().isBlank()) {
            addMessage(table().TRANSACTION_TYPE, MUST_BE_VALID_VALUE);
        }
        if (pojo().getInstrumentType().isBlank()) {
            addMessage(table().INSTRUMENT_TYPE, MUST_BE_VALID_VALUE);
        }
        if (pojo().getMemo() != null && !pojo().getMemo().isEmpty() && pojo().getMemo().isBlank()) {
            addMessage(table().MEMO, MUST_BE_EMPTY_OR_NOT_BE_BLANK);
        }
    }
}
