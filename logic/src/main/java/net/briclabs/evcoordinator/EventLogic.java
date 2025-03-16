package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Event;
import net.briclabs.evcoordinator.generated.tables.records.EventRecord;
import org.jooq.DSLContext;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Event.EVENT;

public class EventLogic<P extends Event> extends Logic<EventRecord, Event, net.briclabs.evcoordinator.generated.tables.Event> implements WriteLogic<P> {
    public EventLogic(DSLContext jooq) {
        super(jooq, Event.class, EVENT, EVENT.ID);
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getIdColumn().getName(), Long.toString(pojo.getEventId())),
                entry(getTable().ACTION_TYPE.getName(), pojo.getActionType()),
                entry(getTable().PARTICIPANT_ID.getName(), Long.toString(pojo.getParticipantId())));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(getTable())
                .set(getTable().EVENT_ID, pojo.getEventId())
                .set(getTable().ACTION_TYPE, pojo.getActionType())
                .set(getTable().PARTICIPANT_ID, pojo.getParticipantId())
                .returning(getIdColumn())
                .fetchOptional()
                .map(EventRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(getTable())
                .set(getTable().EVENT_ID, update.getEventId())
                .set(getTable().ACTION_TYPE, update.getActionType())
                .set(getTable().PARTICIPANT_ID, update.getParticipantId())
                .where(getIdColumn().eq(update.getId()))
                .and(
                        getTable().EVENT_ID.notEqual(update.getEventId())
                    .or(getTable().ACTION_TYPE.notEqual(update.getActionType()))
                    .or(getTable().PARTICIPANT_ID.notEqual(update.getParticipantId()))
                )
                .execute();
    }
}
