package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Event;
import net.briclabs.evcoordinator.generated.tables.records.EventRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Event.EVENT;

public class EventLogic extends Logic {

    static List<Condition> parseCriteriaIntoConditions(Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, EVENT);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            buildPossibleCondition(EVENT.ID, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(EVENT.EVENT_ID, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(EVENT.PARTICIPANT_ID, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(EVENT.ACTION_TYPE, key, value).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    public EventLogic(DSLContext jooq) {
        super(jooq);
    }

    public boolean validateIsTrulyNew(Event pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(EVENT.EVENT_ID.getName(), Long.toString(pojo.getEventId())),
                entry(EVENT.ACTION_TYPE.getName(), pojo.getActionType()),
                entry(EVENT.PARTICIPANT_ID.getName(), Long.toString(pojo.getParticipantId())));
        return fetchByCriteria(criteria, 0, 1).size() > 0;
    }

    public Optional<Event> fetchById(Long id) {
        return jooq
                .select()
                .from(EVENT)
                .where(EVENT.ID.eq(id))
                .fetchOptionalInto(Event.class);
    }

    public List<Event> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {
        return jooq
                .selectFrom(EVENT)
                .where(parseCriteriaIntoConditions(searchCriteria))
                .orderBy(EVENT.ID)
                .limit(offset, max)
                .fetchStreamInto(Event.class)
                .toList();
    }

    public Optional<Long> insertNew(Event pojo) {
        return jooq
                .insertInto(EVENT)
                .set(EVENT.EVENT_ID, pojo.getEventId())
                .set(EVENT.ACTION_TYPE, pojo.getActionType())
                .set(EVENT.PARTICIPANT_ID, pojo.getParticipantId())
                .returning(EVENT.ID)
                .fetchOptional()
                .map(EventRecord::getId);
    }

    public int updateExisting(Event update) {
        return jooq
                .update(EVENT)
                .set(EVENT.EVENT_ID, update.getEventId())
                .set(EVENT.ACTION_TYPE, update.getActionType())
                .set(EVENT.PARTICIPANT_ID, update.getParticipantId())
                .where(EVENT.ID.eq(update.getId()))
                .and(
                        EVENT.EVENT_ID.notEqual(update.getEventId())
                    .or(EVENT.ACTION_TYPE.notEqual(update.getActionType()))
                    .or(EVENT.PARTICIPANT_ID.notEqual(update.getParticipantId()))
                )
                .execute();
    }
}
