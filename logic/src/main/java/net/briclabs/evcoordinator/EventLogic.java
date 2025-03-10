package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Event;
import net.briclabs.evcoordinator.generated.tables.records.EventRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.Event.EVENT;

public class EventLogic<P extends Event> extends Logic implements WriteLogic<P> {

    private static final Map<String, Field<?>> FIELDS = Map.ofEntries(
            Map.entry("ID", EVENT.ID),
            Map.entry("PARTICIPANT_ID", EVENT.PARTICIPANT_ID),
            Map.entry("ACTION_TYPE", EVENT.ACTION_TYPE),
            Map.entry("EVENT_ID", EVENT.EVENT_ID),
            Map.entry("TIME_RECORDED", EVENT.TIME_RECORDED)
    );

    public EventLogic(DSLContext jooq) {
        super(jooq);
    }

    static List<Condition> parseCriteriaIntoConditions(boolean exactCriteria, Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, EVENT);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(EVENT.EVENT_ID, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(EVENT.PARTICIPANT_ID, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(EVENT.ACTION_TYPE, key, value, exactCriteria).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(EVENT.EVENT_ID.getName(), Long.toString(pojo.getEventId())),
                entry(EVENT.ACTION_TYPE.getName(), pojo.getActionType()),
                entry(EVENT.PARTICIPANT_ID.getName(), Long.toString(pojo.getParticipantId())));
        return fetchByCriteria(true, criteria, EVENT.ID.getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Event> fetchById(Long id) {
        return jooq
                .select()
                .from(EVENT)
                .where(EVENT.ID.eq(id))
                .fetchOptionalInto(Event.class);
    }

    @Override
    public Field<?> resolveField(String columnName, Field<?> defaultField) {
        return FIELDS.getOrDefault(columnName, defaultField);
    }

    @Override
    public ListWithCount<Event> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, int offset, int max) {
        List<Condition> conditions = parseCriteriaIntoConditions(exactCriteria, searchCriteria);

        List<Event> results = jooq
                .selectFrom(EVENT)
                .where(buildWhereClause(exactCriteria, conditions))
                .orderBy(sortAscending
                        ? resolveField(sortColumn, EVENT.ID).asc()
                        : resolveField(sortColumn, EVENT.ID).desc())
                .limit(offset, max)
                .fetchStreamInto(Event.class)
                .toList();
        int count = jooq
                .selectCount()
                .from(EVENT)
                .where(conditions)
                .fetchOptional(0, Integer.class)
                .orElse(0);
        return new ListWithCount<>(results, count);
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(EVENT)
                .set(EVENT.EVENT_ID, pojo.getEventId())
                .set(EVENT.ACTION_TYPE, pojo.getActionType())
                .set(EVENT.PARTICIPANT_ID, pojo.getParticipantId())
                .returning(EVENT.ID)
                .fetchOptional()
                .map(EventRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
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
