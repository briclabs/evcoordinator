package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import net.briclabs.evcoordinator.validation.EventInfoValidator;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO;
import static net.briclabs.evcoordinator.generated.tables.Registration.REGISTRATION;

public class EventInfoLogic extends WriteAndDeleteLogic<EventInfoRecord, EventInfo, net.briclabs.evcoordinator.generated.tables.EventInfo> {
    private final HistoryLogic historyLogic;
    private final TransactionLogic transactionLogic;
    private final GuestLogic guestLogic;
    private final RegistrationLogic registrationLogic;

    /**
     * Represents the possible statuses of an event.
     */
    public enum EVENT_STATUS {
        CURRENT,
        PAST,
        CANCELLED;

        /**
         * Converts a string representation of an event status to the corresponding {@code EVENT_STATUS} enum value.
         * The comparison is case-insensitive. If the input string does not match any known status, {@code null} is returned.
         *
         * @param status the string representation of the event status to convert.
         * @return the corresponding {@code EVENT_STATUS} enum value, or {@code null} if the input string does not match any status.
         */
        public static Optional<EVENT_STATUS> fromString(String status) {
            for (EVENT_STATUS eventStatus : values()) {
                if (eventStatus.name().equalsIgnoreCase(status)) {
                    return Optional.of(eventStatus);
                }
            }
            return Optional.empty();
        }
    }

    public EventInfoLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, EventInfo.class, EVENT_INFO, EVENT_INFO.ID);
        this.historyLogic = new HistoryLogic(objectMapper, jooq);
        this.transactionLogic = new TransactionLogic(objectMapper, jooq);
        this.guestLogic = new GuestLogic(objectMapper, jooq);
        this.registrationLogic = new RegistrationLogic(objectMapper, jooq);
    }

    @Override
    public boolean isAlreadyRecorded(EventInfo pojo) {
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
    public Optional<Long> insertNew(long actorId, EventInfo pojo) {
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
                    getTable().getName().toUpperCase(),
                    convertToJson(pojo),
                    JSON.json("{}"),
                    null
            ));
        }
        return insertedId;
    }

    @Override
    public int updateExisting(long actorId, EventInfo update) throws EventInfoException {
        if (update.getId() == null) {
            throw new EventInfoException(
                    new AbstractMap.SimpleImmutableEntry<>(getIdColumn().getName(), "ID to update was missing."),
                    "ID %d to update was missing.".formatted(update.getId()));
        }
        var originalRecord = fetchById(update.getId()).orElseThrow(() -> new EventInfoException(
                new AbstractMap.SimpleImmutableEntry<>(getTable().getName(), "Record to update was not found."),
                "Record %d to update was not found.".formatted(update.getId())));
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
                    getTable().getName().toUpperCase(),
                    convertToJson(update),
                    convertToJson(originalRecord),
                    null
            ));
        }
        return updatedRecords;
    }

    @Override
    public void delete(long actorId, long idToDelete) throws EventInfoException, TransactionLogic.TransactionException, RegistrationLogic.RegistrationException, GuestLogic.GuestException {
        var originalRecord = fetchById(idToDelete).orElseThrow(() -> new EventInfoException(
                new AbstractMap.SimpleImmutableEntry<>(getTable().getName(), "The event to be deleted was not found."),
                "The event with ID %d to be deleted was not found.".formatted(idToDelete)));
        deleteCorrespondingTransactions(actorId, idToDelete);
        deleteCorrespondingRegistrationsAndGuests(actorId, idToDelete);

        var deletedRecords = jooq.deleteFrom(getTable()).where(getTable().ID.eq(idToDelete)).execute();
        if (deletedRecords > 0) {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    HistoryLogic.ActionType.UPDATED.name(),
                    getTable().getName().toUpperCase(),
                    JSON.json("{}"),
                    convertToJson(originalRecord),
                    null
            ));
        }
    }

    @Override
    public Map<String, String> validate(EventInfo pojo) {
        return EventInfoValidator.of(pojo).getMessages();
    }

    private void deleteCorrespondingRegistrationsAndGuests(long actorId, long idToDelete) throws RegistrationLogic.RegistrationException, GuestLogic.GuestException {
        var registrationIdsToDelete = jooq.select(REGISTRATION.ID).from(REGISTRATION).where(REGISTRATION.EVENT_INFO_ID.eq(idToDelete)).fetchInto(Long.class);
        deleteCorrespondingGuests(actorId, registrationIdsToDelete);
        for (Long registrationIdToDelete : registrationIdsToDelete) {
            registrationLogic.delete(actorId, registrationIdToDelete);
        }
    }

    private void deleteCorrespondingGuests(long actorId, List<Long> registrationIdsToDelete) throws GuestLogic.GuestException {
        for (Long guestIdToDelete : guestLogic.getGuestIdsByRegistrationIds(registrationIdsToDelete)) {
            guestLogic.delete(actorId, guestIdToDelete);
        }
    }

    private void deleteCorrespondingTransactions(long actorId, long eventId) throws TransactionLogic.TransactionException {
        for (Long transactionIdToDelete : transactionLogic.getTransactionIdsByEventId(eventId)) {
            transactionLogic.delete(actorId, transactionIdToDelete);
        }
    }

    public static class EventInfoException extends LogicException {
        public EventInfoException(Map.Entry<String, String> publicMessage, String troubleshootingMessage) {
            super(publicMessage, troubleshootingMessage);
        }
    }
}
