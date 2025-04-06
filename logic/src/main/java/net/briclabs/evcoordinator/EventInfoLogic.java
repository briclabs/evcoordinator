package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.pojos.Guest;
import net.briclabs.evcoordinator.generated.tables.pojos.Payment;
import net.briclabs.evcoordinator.generated.tables.pojos.Registration;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO;
import static net.briclabs.evcoordinator.generated.tables.Registration.REGISTRATION;

public class EventInfoLogic<P extends EventInfo> extends Logic<EventInfoRecord, EventInfo, net.briclabs.evcoordinator.generated.tables.EventInfo> implements WriteLogic<P>, DeletableRecord {
    private final HistoryLogic<DataHistory> historyLogic;
    private final PaymentLogic<Payment> paymentLogic;
    private final GuestLogic<Guest> guestLogic;
    private final RegistrationLogic<Registration> registrationLogic;

    public EventInfoLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, EventInfo.class, EVENT_INFO, EVENT_INFO.ID);
        this.historyLogic = new HistoryLogic<>(objectMapper, new ParticipantLogic<>(objectMapper, jooq), jooq);
        this.paymentLogic = new PaymentLogic<>(objectMapper, jooq);
        this.guestLogic = new GuestLogic<>(objectMapper, jooq);
        this.registrationLogic = new RegistrationLogic<>(objectMapper, jooq);
    }

    @Override
    public boolean isAlreadyRecorded(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().EVENT_STATUS.getName(), pojo.getEventStatus()),
                entry(getTable().EVENT_NAME.getName(), pojo.getEventName()),
                entry(getTable().EVENT_TITLE.getName(), pojo.getEventTitle()),
                entry(getTable().DATE_START.getName(), pojo.getDateStart().format(DateTimeFormatter.ISO_DATE)),
                entry(getTable().DATE_END.getName(), pojo.getDateEnd().format(DateTimeFormatter.ISO_DATE)));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    /**
     * Fetches the latest event info entry. If no entries exist in the table, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional<EventInfo>} containing the latest event info entry if present, or empty if no entries exist.
     */
    public Optional<EventInfo> fetchLatest() {
        return jooq
                .selectFrom(getTable())
                .where(getTable().EVENT_STATUS.eq("CURRENT"))
                .orderBy(getIdColumn().desc())
                .limit(1)
                .fetchOptionalInto(getRecordType());
    }

    @Override
    public Optional<Long> insertNew(long actorId, P pojo) {
        Optional<Long> insertedId = jooq
                .insertInto(getTable())
                .set(getTable().EVENT_STATUS, pojo.getEventStatus())
                .set(getTable().EVENT_NAME, pojo.getEventName())
                .set(getTable().EVENT_TITLE, pojo.getEventTitle())
                .set(getTable().DATE_START, pojo.getDateStart())
                .set(getTable().DATE_END, pojo.getDateEnd())
                .returning(getIdColumn())
                .fetchOptional()
                .map(EventInfoRecord::getId);
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
        var originalRecord = fetchById(update.getId()).orElseThrow(() -> new RuntimeException("Event Info not found: " + update.getId()));
        int updatedRecords = jooq
                .update(getTable())
                .set(getTable().EVENT_STATUS, update.getEventStatus())
                .set(getTable().EVENT_NAME, update.getEventName())
                .set(getTable().EVENT_TITLE, update.getEventTitle())
                .set(getTable().DATE_START, update.getDateStart())
                .set(getTable().DATE_END, update.getDateEnd())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().EVENT_STATUS.notEqual(update.getEventStatus())
                                .or(getTable().EVENT_NAME.notEqual(update.getEventName()))
                                .or(getTable().EVENT_TITLE.notEqual(update.getEventTitle()))
                                .or(getTable().DATE_START.notEqual(update.getDateStart()))
                                .or(getTable().DATE_END.notEqual(update.getDateEnd()))
                ).execute();
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

    @Override
    public void delete(long actorId, long idToDelete) {
        var originalRecord = fetchById(idToDelete).orElseThrow(() -> new RuntimeException("Event Info not found: " + idToDelete));
        deleteCorrespondingPayments(actorId, idToDelete);
        deleteCorrespondingRegistrationsAndGuests(actorId, idToDelete);

        var deletedRecords = jooq.deleteFrom(getTable()).where(getTable().ID.eq(idToDelete)).execute();
        if (deletedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.UPDATED.name(),
                    getTable().getName(),
                    JSON.json("{}"),
                    convertToJson(originalRecord),
                    null
            ));
        }
    }

    private void deleteCorrespondingRegistrationsAndGuests(long actorId, long idToDelete) {
        var registrationIdsToDelete = jooq.select(REGISTRATION.ID).from(REGISTRATION).where(REGISTRATION.EVENT_INFO_ID.eq(idToDelete)).fetchInto(Long.class);
        deleteCorrespondingGuests(actorId, registrationIdsToDelete);
        registrationIdsToDelete.forEach(registrationIdToDelete -> registrationLogic.delete(actorId, registrationIdToDelete));
    }

    private void deleteCorrespondingGuests(long actorId, List<Long> registrationIdsToDelete) {
        guestLogic.getGuestIdsByRegistrationIds(registrationIdsToDelete).forEach(guestIdToDelete -> guestLogic.delete(actorId, guestIdToDelete));
    }

    private void deleteCorrespondingPayments(long actorId, long eventId) {
        paymentLogic.getPaymentIdsByEventId(eventId).forEach(paymentIdToDelete -> paymentLogic.delete(actorId, paymentIdToDelete));
    }
}
