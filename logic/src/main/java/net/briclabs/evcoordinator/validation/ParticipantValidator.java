package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantRecord;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDate;

public class ParticipantValidator extends AbstractValidator<ParticipantRecord, Participant, net.briclabs.evcoordinator.generated.tables.Participant> {

    private final boolean isRegistrationPacketSection;

    private ParticipantValidator(Participant participant, boolean isRegistrationPacketSection) {
        super(net.briclabs.evcoordinator.generated.tables.Participant.PARTICIPANT, participant);
        this.isRegistrationPacketSection = isRegistrationPacketSection;
    }

    public static ParticipantValidator of(Participant participant, boolean isRegistrationPacketSection) {
        return new ParticipantValidator(participant, isRegistrationPacketSection);
    }

    @Override
    void validate() {
        if (!isRegistrationPacketSection) {
            if (pojo().getParticipantType() == null) {
                addMessage(table().PARTICIPANT_TYPE, MUST_BE_VALID_VALUE);
            }
        }
        if (pojo().getNameFirst().isBlank()) {
            addMessage(table().NAME_FIRST, MUST_NOT_BE_BLANK);
        }
        if (pojo().getNameLast().isBlank()) {
            addMessage(table().NAME_LAST, MUST_NOT_BE_BLANK);
        }
        if (pojo().getNameNick() != null && !pojo().getNameNick().isEmpty() && pojo().getNameFirst().isBlank()) {
            addMessage(table().NAME_NICK, MUST_BE_EMPTY_OR_NOT_BE_BLANK);
        }
        if (pojo().getSponsor().isBlank()) {
            addMessage(table().SPONSOR, MUST_NOT_BE_BLANK);
        }
        if (pojo().getDob() == null || !pojo().getDob().isBefore(LocalDate.now())) {
            addMessage(table().DOB, MUST_BE_PAST);
        }
        if (pojo().getAddrStreet_1().isBlank()) {
            addMessage(table().ADDR_STREET_1, MUST_NOT_BE_BLANK);
        }
        if (pojo().getAddrStreet_2() != null && !pojo().getAddrStreet_2().isEmpty() && pojo().getAddrStreet_2().isBlank()) {
            addMessage(table().ADDR_STREET_2, MUST_BE_EMPTY_OR_NOT_BE_BLANK);
        }
        if (pojo().getAddrCity().isBlank()) {
            addMessage(table().ADDR_CITY, MUST_NOT_BE_BLANK);
        }
        if (pojo().getAddrStateAbbr() == null) {
            addMessage(table().ADDR_STATE_ABBR, MUST_BE_VALID_STATE_ABBR);
        }
        if (!(pojo().getAddrZip().trim().length() == 5 || pojo().getAddrZip().trim().replace("-", "").length() == 9)) {
            addMessage(table().ADDR_ZIP, MUST_BE_VALID_ZIP);
        }
        if (!EmailValidator.getInstance().isValid(pojo().getAddrEmail())) {
            addMessage(table().ADDR_EMAIL, MUST_BE_VALID_EMAIL);
        }
        if (pojo().getPhoneDigits() == null || pojo().getPhoneDigits() < 1000000000L || pojo().getPhoneDigits() > 9999999999L) {
            addMessage(table().PHONE_DIGITS, MUST_BE_VALID_PHONE);
        }
        if (pojo().getEmergencyContactRelationshipType() == null) {
            addMessage(table().EMERGENCY_CONTACT_RELATIONSHIP_TYPE, MUST_BE_VALID_VALUE);
        }
        if (pojo().getNameEmergency().isBlank()) {
            addMessage(table().NAME_EMERGENCY, MUST_NOT_BE_BLANK);
        }
        if (pojo().getPhoneEmergency() == null || pojo().getPhoneEmergency() < 1000000000L || pojo().getPhoneEmergency() > 9999999999L) {
            addMessage(table().PHONE_EMERGENCY, MUST_BE_VALID_PHONE);
        }
    }
}
