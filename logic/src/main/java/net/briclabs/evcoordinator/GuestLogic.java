package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.GuestWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.GuestRecord;
import net.briclabs.evcoordinator.generated.tables.records.GuestWithLabelsRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Guest.GUEST;
import static net.briclabs.evcoordinator.generated.tables.GuestWithLabels.GUEST_WITH_LABELS;

public class GuestLogic<P extends Guest> extends Logic<GuestRecord, Guest, net.briclabs.evcoordinator.generated.tables.Guest> implements WriteLogic<P>, DeletableRecord {

    private final GuestLogic.GuestWithLabelsLogic guestWithLabelsLogic;

    public GuestLogic(DSLContext jooq) {
        super(jooq, Guest.class, GUEST, GUEST.ID);
        this.guestWithLabelsLogic = new GuestLogic.GuestWithLabelsLogic(jooq);
    }

    public GuestLogic.GuestWithLabelsLogic getGuestWithLabelsLogic() {
        return guestWithLabelsLogic;
    }

    @Override
    public boolean isAlreadyRecorded(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().REGISTRATION_ID.getName(), Long.toString(pojo.getRegistrationId())),
                entry(getTable().INVITEE_PROFILE_ID.getName(), Long.toString(pojo.getInviteeProfileId())),
                entry(getTable().GUEST_PROFILE_ID.getName(), Optional.ofNullable(pojo.getGuestProfileId()).map(value -> Long.toString(value)).orElse("")),
                entry(getTable().RAW_GUEST_NAME.getName(), pojo.getRawGuestName()),
                entry(getTable().RELATIONSHIP.getName(), pojo.getRelationship()));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().REGISTRATION_ID, pojo.getRegistrationId())
                .set(getTable().INVITEE_PROFILE_ID, pojo.getInviteeProfileId())
                .set(getTable().GUEST_PROFILE_ID, pojo.getGuestProfileId())
                .set(getTable().RAW_GUEST_NAME, pojo.getRawGuestName())
                .set(getTable().RELATIONSHIP, pojo.getRelationship())
                .returning(getIdColumn())
                .fetchOptional()
                .map(GuestRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().REGISTRATION_ID, update.getRegistrationId())
                .set(getTable().INVITEE_PROFILE_ID, update.getInviteeProfileId())
                .set(getTable().GUEST_PROFILE_ID, update.getGuestProfileId())
                .set(getTable().RAW_GUEST_NAME, update.getRelationship())
                .set(getTable().RELATIONSHIP, update.getRelationship())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().REGISTRATION_ID.notEqual(update.getRegistrationId())
                    .or(getTable().INVITEE_PROFILE_ID.notEqual(update.getInviteeProfileId()))
                    .or(getTable().GUEST_PROFILE_ID.notEqual(update.getGuestProfileId()))
                    .or(getTable().RAW_GUEST_NAME.notEqual(update.getRawGuestName()))
                    .or(getTable().RELATIONSHIP.notEqual(update.getRelationship()))
                ).execute();
    }

    @Override
    public void delete(Long id) {
        jooq.deleteFrom(getTable()).where(getTable().ID.eq(id)).execute();
    }

    public static class GuestWithLabelsLogic extends Logic<GuestWithLabelsRecord, GuestWithLabels, net.briclabs.evcoordinator.generated.tables.GuestWithLabels> {
        public GuestWithLabelsLogic(DSLContext jooq) {
            super(jooq, GuestWithLabels.class, GUEST_WITH_LABELS, GUEST_WITH_LABELS.ID);
        }
    }
}