package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.jooq.DSLContext;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO;

public class EventInfoLogic<P extends EventInfo> extends Logic<EventInfoRecord, EventInfo, net.briclabs.evcoordinator.generated.tables.EventInfo> implements WriteLogic<P> {
    public EventInfoLogic(DSLContext jooq) {
        super(jooq, EventInfo.class, EVENT_INFO, EVENT_INFO.ID);
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().EVENT_STATUS.getName(), pojo.getEventStatus()),
                entry(getTable().EVENT_NAME.getName(), pojo.getEventName()),
                entry(getTable().EVENT_TITLE.getName(), pojo.getEventTitle()),
                entry(getTable().DATE_START.getName(), pojo.getDateStart().format(DateTimeFormatter.ISO_DATE)),
                entry(getTable().DATE_END.getName(), pojo.getDateEnd().format(DateTimeFormatter.ISO_DATE)));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
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
}
