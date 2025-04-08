package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.pojos.RegistrationWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.GuestRecord;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationRecord;
import net.briclabs.evcoordinator.generated.tables.records.RegistrationWithLabelsRecord;
import org.jooq.DSLContext;
import org.jooq.JSON;
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
    private final HistoryLogic<DataHistory> historyLogic;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationLogic.class);

    private final RegistrationWithLabelsLogic registrationWithLabelsLogic;
    private final GuestLogic<net.briclabs.evcoordinator.generated.tables.pojos.Guest> guestLogic;

    public RegistrationLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, Registration.class, REGISTRATION, REGISTRATION.ID);
        this.registrationWithLabelsLogic = new RegistrationWithLabelsLogic(objectMapper, jooq);
        this.guestLogic = new GuestLogic<>(objectMapper, jooq);
        this.historyLogic = new HistoryLogic<>(objectMapper, jooq);
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
    public Optional<Long> insertNew(long actorId, P pojo) {
        Optional<Long> insertedId = jooq
                .insertInto(getTable())
                .set(getTable().EVENT_INFO_ID, pojo.getEventInfoId())
                .set(getTable().SIGNATURE, pojo.getSignature())
                .set(getTable().DONATION_PLEDGE, pojo.getDonationPledge())
                .set(getTable().PARTICIPANT_ID, pojo.getParticipantId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(RegistrationRecord::getId);
        if (insertedId.isPresent()) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.INSERTED.name(),
                    getTable().getName(),
                    convertToJson(pojo),
                    JSON.json("{}"),
                    null
            ));
        }
        return insertedId;
    }

    @Override
    public int updateExisting(long actorId, P update) {
        if (update.getId() != null) {
            handleGuestUpdates(actorId, update.getId(), update.getParticipantId());
            var originalRecord = fetchById(update.getId()).orElseThrow(() -> new RuntimeException("Registration not found: " + update.getId()));
            int updatedRecords = jooq
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
            if (updatedRecords > 0) {
                historyLogic.insertNew(actorId, new DataHistory(
                        null,
                        actorId,
                        HistoryLogic.ActionType.UPDATED.name(),
                        getTable().getName(),
                        convertToJson(update),
                        convertToJson(originalRecord),
                        null
                ));
            }
            return updatedRecords;
        }
        LOGGER.warn("Tried to update registration with a null id: {}", update);
        return 0;
    }

    @Override
    public void delete(long actorId, long idToDelete) {
        var originalRecord = fetchById(idToDelete).orElseThrow(() -> new RuntimeException("Registration not found: " + idToDelete));
        deleteCorrespondingGuests(actorId, idToDelete);
        var deletedRecords = jooq.deleteFrom(getTable()).where(getTable().ID.eq(idToDelete)).execute();
        if (deletedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.DELETED.name(),
                    getTable().getName(),
                    JSON.json("{}"),
                    convertToJson(originalRecord),
                    null
            ));
        }
    }

    private void handleGuestUpdates(long actorId, long registrationId, long newParticipantId) {
        fetchGuestsForThisRegistration(registrationId).stream()
                .filter(guest -> guest.getId() != null)
                .forEach(guest -> updateRelevantGuestsForThisEvent(actorId, guest.getId(), newParticipantId));
    }

    private Result<GuestRecord> fetchGuestsForThisRegistration(long registrationId) {
        return jooq
                .selectFrom(GUEST)
                .where(GUEST.REGISTRATION_ID.eq(registrationId))
                .fetch();
    }

    private void updateRelevantGuestsForThisEvent(long actorId, long guestId, long newParticipantId) {
        var existingGuest = guestLogic.fetchById(guestId).orElseThrow(() -> new RuntimeException("Guest not found: " + guestId));
        var updatedGuest = new Guest(existingGuest.getId(), newParticipantId, existingGuest.getRegistrationId(), existingGuest.getRawGuestName(), existingGuest.getGuestProfileId(), existingGuest.getRelationship(), existingGuest.getTimeRecorded());
        guestLogic.updateExisting(actorId, updatedGuest);
    }

    private void deleteCorrespondingGuests(long actorId, long registrationIdToDelete) {
        jooq.select(GUEST.ID).from(GUEST).where(GUEST.REGISTRATION_ID.eq(registrationIdToDelete)).fetchInto(Long.class).forEach(guestIdToDelete -> guestLogic.delete(actorId, guestIdToDelete));
    }

    /**
     * Special logic class that works with a view instead of the raw table. The view provides useful label information for rows which, in the table, are simply FKs.
     */
    public static class RegistrationWithLabelsLogic extends Logic<RegistrationWithLabelsRecord, RegistrationWithLabels, net.briclabs.evcoordinator.generated.tables.RegistrationWithLabels> {

        public RegistrationWithLabelsLogic(ObjectMapper objectMapper, DSLContext jooq) {
            super(objectMapper, jooq, RegistrationWithLabels.class, REGISTRATION_WITH_LABELS, REGISTRATION_WITH_LABELS.ID);
        }
    }
}
