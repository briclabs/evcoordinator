package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.jooq.DSLContext;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO;
import static net.briclabs.evcoordinator.generated.tables.Guest.GUEST;
import static net.briclabs.evcoordinator.generated.tables.Payment.PAYMENT;
import static net.briclabs.evcoordinator.generated.tables.Registration.REGISTRATION;

public class EventInfoLogic<P extends EventInfo> extends Logic<EventInfoRecord, EventInfo, net.briclabs.evcoordinator.generated.tables.EventInfo> implements WriteLogic<P>, DeletableRecord {
    public EventInfoLogic(DSLContext jooq) {
        super(jooq, EventInfo.class, EVENT_INFO, EVENT_INFO.ID);
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
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().EVENT_STATUS, pojo.getEventStatus())
                .set(getTable().EVENT_NAME, pojo.getEventName())
                .set(getTable().EVENT_TITLE, pojo.getEventTitle())
                .set(getTable().DATE_START, pojo.getDateStart())
                .set(getTable().DATE_END, pojo.getDateEnd())
                .returning(getIdColumn())
                .fetchOptional()
                .map(EventInfoRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
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
    }

    @Override
    public void delete(Long id) {
        var paymentsToDelete = jooq.select(PAYMENT.ID).from(PAYMENT).where(PAYMENT.EVENT_INFO_ID.eq(id)).fetchInto(Long.class);
        jooq.deleteFrom(PAYMENT).where(PAYMENT.ID.in(paymentsToDelete)).execute();

        var registrationIdsToDelete = jooq.select(REGISTRATION.ID).from(REGISTRATION).where(REGISTRATION.EVENT_INFO_ID.eq(id)).fetchInto(Long.class);
        var guestIdsToDelete = jooq.select(GUEST.ID).from(GUEST).where(GUEST.REGISTRATION_ID.in(registrationIdsToDelete)).fetchInto(Long.class);

        jooq.deleteFrom(GUEST).where(GUEST.ID.in(guestIdsToDelete)).execute();
        jooq.deleteFrom(REGISTRATION).where(REGISTRATION.ID.in(registrationIdsToDelete)).execute();

        jooq.deleteFrom(getTable()).where(getTable().ID.eq(id)).execute();
    }
}
