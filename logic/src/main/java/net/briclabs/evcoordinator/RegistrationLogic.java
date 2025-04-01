package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.GuestRecord;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationWithLabelsRecord;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.Tables.GUEST;
import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION;
import static net.briclabs.evcoordinator.generated.Tables.REGISTRATION_WITH_LABELS;

public class RegistrationLogic<P extends Registration> extends Logic<RegistrationRecord, Registration, net.briclabs.evcoordinator.generated.tables.Registration> implements WriteLogic<P>, DeletableRecord {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationLogic.class);

    private final RegistrationWithLabelsLogic registrationWithLabelsLogic;

    public RegistrationLogic(DSLContext jooq) {
        super(jooq, Registration.class, REGISTRATION, REGISTRATION.ID);
        this.registrationWithLabelsLogic = new RegistrationWithLabelsLogic(jooq);
    }

    public RegistrationWithLabelsLogic getRegistrationWithLabelsLogic() {
        return registrationWithLabelsLogic;
    }

    @Override
    public boolean isAlreadyRecorded(P registrationPojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().EVENT_INFO_ID.getName(), Long.toString(registrationPojo.getEventInfoId())),
                entry(getTable().SIGNATURE.getName(), registrationPojo.getSignature()),
                entry(getTable().DONATION_PLEDGE.getName(), registrationPojo.getDonationPledge().toPlainString()),
                entry(getTable().PARTICIPANT_ID.getName(), Long.toString(registrationPojo.getParticipantId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P registrationPojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().EVENT_INFO_ID, registrationPojo.getEventInfoId())
                .set(getTable().SIGNATURE, registrationPojo.getSignature())
                .set(getTable().DONATION_PLEDGE, registrationPojo.getDonationPledge())
                .set(getTable().PARTICIPANT_ID, registrationPojo.getParticipantId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(RegistrationRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        if (update.getId() != null) {
            handleGuestUpdates(update.getId(), update.getParticipantId());
            return jooq
                    .update(getTable())
                    .set(getTable().EVENT_INFO_ID, update.getEventInfoId())
                    .set(getTable().SIGNATURE, update.getSignature())
                    .set(getTable().DONATION_PLEDGE, update.getDonationPledge())
                    .set(getTable().PARTICIPANT_ID, update.getParticipantId())
                    .where(getIdColumn().eq(update.getId()))
                    .and(
                            getTable().EVENT_INFO_ID.notEqual(update.getEventInfoId())
                                    .or(getTable().SIGNATURE.notEqual(update.getSignature()))
                                    .or(getTable().DONATION_PLEDGE.notEqual(update.getDonationPledge()))
                                    .or(getTable().PARTICIPANT_ID.notEqual(update.getParticipantId()))
                    )
                    .execute();
        }
        LOGGER.warn("Tried to update registration with a null id: {}", update);
        return 0;
    }

    @Override
    public void delete(Long id) {
        var guestIdsToDelete = jooq.select(Guest.GUEST.ID).from(Guest.GUEST).where(Guest.GUEST.REGISTRATION_ID.eq(id)).fetchInto(Long.class);
        jooq.deleteFrom(Guest.GUEST).where(Guest.GUEST.ID.in(guestIdsToDelete)).execute();

        jooq.deleteFrom(getTable()).where(getTable().ID.eq(id)).execute();
    }

    private void handleGuestUpdates(long registrationId, long newParticipantId) {
        fetchGuestsForThisRegistration(registrationId).stream()
                .filter(guest -> guest.getId() != null)
                .forEach(guest -> updateRelevantGuestsForThisEvent(newParticipantId, guest.getId()));
    }

    private Result<GuestRecord> fetchGuestsForThisRegistration(long registrationId) {
        return jooq
                .selectFrom(GUEST)
                .where(GUEST.REGISTRATION_ID.eq(registrationId))
                .fetch();
    }

    private void updateRelevantGuestsForThisEvent(long newParticipantId, long guestId) {
        jooq
            .update(GUEST)
            .set(GUEST.INVITEE_PROFILE_ID, newParticipantId)
            .where(GUEST.ID.eq(guestId))
            .execute();
    }

    public static class RegistrationWithLabelsLogic extends Logic<RegistrationWithLabelsRecord, RegistrationWithLabels, net.briclabs.evcoordinator.generated.tables.RegistrationWithLabels> {
        public RegistrationWithLabelsLogic(DSLContext jooq) {
            super(jooq, RegistrationWithLabels.class, REGISTRATION_WITH_LABELS, REGISTRATION_WITH_LABELS.ID);
        }
    }
}
