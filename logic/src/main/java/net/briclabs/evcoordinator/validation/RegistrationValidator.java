package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;

import java.math.BigDecimal;

public class RegistrationValidator extends AbstractValidator<RegistrationRecord, Registration, net.briclabs.evcoordinator.generated.tables.Registration> {

    private boolean isRegistrationPacketSection;

    private RegistrationValidator(Registration registration, boolean isRegistrationPacketSection) {
        super(net.briclabs.evcoordinator.generated.tables.Registration.REGISTRATION, registration);
        this.isRegistrationPacketSection = isRegistrationPacketSection;
    }

    public static RegistrationValidator of(Registration registration, boolean isRegistrationPacketSection) {
        return new RegistrationValidator(registration, isRegistrationPacketSection);
    }

    @Override
    void validate() {
        if (!isRegistrationPacketSection) {
            if (pojo().getParticipantId() == null || pojo().getParticipantId() < 0L) {
                addMessage(table().PARTICIPANT_ID, MUST_BE_VALID_VALUE);
            }
        }
        if (pojo().getDonationPledge() == null || pojo().getDonationPledge().compareTo(BigDecimal.ZERO) < 0) {
            addMessage(table().DONATION_PLEDGE, MUST_BE_POSITIVE_NUMBER);
        }
        if (pojo().getSignature().isBlank()) {
            addMessage(table().SIGNATURE, MUST_NOT_BE_BLANK);
        }
        if (pojo().getEventInfoId() == null || pojo().getEventInfoId() < 0L) {
            addMessage(table().EVENT_INFO_ID, MUST_BE_VALID_VALUE);
        }
    }
}
