package net.briclabs.evcoordinator.validation;

import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.records.GuestRecord;

public class GuestValidator extends AbstractValidator<GuestRecord, Guest, net.briclabs.evcoordinator.generated.tables.Guest> {

    private GuestValidator(Guest guest) {
        super(net.briclabs.evcoordinator.generated.tables.Guest.GUEST, guest);
    }

    public static GuestValidator of(Guest guest) {
        return new GuestValidator(guest);
    }

    void validate() {
        if (pojo().getInviteeProfileId() < 0L) {
            addMessage(table().INVITEE_PROFILE_ID, MUST_BE_POSITIVE_NUMBER);
        }
        if (pojo().getRegistrationId() < 0L) {
            addMessage(table().REGISTRATION_ID, MUST_BE_POSITIVE_NUMBER);
        }
        if (pojo().getRawGuestName().isBlank()) {
            addMessage(table().RAW_GUEST_NAME, MUST_NOT_BE_BLANK);
        }
        Long guestProfileId = pojo().getGuestProfileId();
        if (guestProfileId != null && guestProfileId < 0) {
            addMessage(table().GUEST_PROFILE_ID, MUST_BE_POSITIVE_NUMBER);
        }
        if (pojo().getRelationship().isBlank()) {
            addMessage(table().RELATIONSHIP, MUST_NOT_BE_BLANK);
        }
    }
}
