package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.records.GuestRecord;

public class GuestValidator extends AbstractValidator<GuestRecord, Guest, net.briclabs.evcoordinator.generated.tables.Guest> {

    private boolean isRegistrationPacketSection;

    private GuestValidator(Guest guest, boolean isRegistrationPacketSection) {
        super(net.briclabs.evcoordinator.generated.tables.Guest.GUEST, guest);
        this.isRegistrationPacketSection = isRegistrationPacketSection;
    }

    public static GuestValidator of(Guest guest, boolean isRegistrationPacketSection) {
        return new GuestValidator(guest, isRegistrationPacketSection);
    }

    @Override
    void validate() {
        if (!isRegistrationPacketSection) {
            if (pojo().getRegistrationId() == null || pojo().getRegistrationId() < 0L) {
                addMessage(table().REGISTRATION_ID, MUST_BE_VALID_VALUE);
            }
            Long guestProfileId = pojo().getGuestProfileId();
            if (guestProfileId != null && guestProfileId < 0) {
                addMessage(table().GUEST_PROFILE_ID, MUST_BE_VALID_VALUE);
            }
        }
        if (pojo().getRawGuestName().isBlank()) {
            addMessage(table().RAW_GUEST_NAME, MUST_NOT_BE_BLANK);
        }
        if (pojo().getRelationship().isBlank()) {
            addMessage(table().RELATIONSHIP, MUST_BE_VALID_VALUE);
        }
    }
}
