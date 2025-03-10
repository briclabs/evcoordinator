package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO;

public class EventInfoLogic<P extends EventInfo> extends Logic implements WriteLogic<P> {

    private static final Map<String, Field<?>> FIELDS = Map.ofEntries(
            Map.entry("ID", EVENT_INFO.ID),
            Map.entry("EVENT_NAME", EVENT_INFO.EVENT_NAME),
            Map.entry("EVENT_TITLE", EVENT_INFO.EVENT_TITLE),
            Map.entry("DATE_START", EVENT_INFO.DATE_START),
            Map.entry("DATE_END", EVENT_INFO.DATE_END),
            Map.entry("EVENT_STATUS", EVENT_INFO.EVENT_STATUS),
            Map.entry("TIME_RECORDED", EVENT_INFO.TIME_RECORDED)

    );

    public EventInfoLogic(DSLContext jooq) {
        super(jooq);
    }

    static List<Condition> parseCriteriaIntoConditions(boolean exactCriteria, Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, EVENT_INFO);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(EVENT_INFO.EVENT_STATUS, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(EVENT_INFO.EVENT_NAME, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(EVENT_INFO.EVENT_TITLE, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(EVENT_INFO.DATE_START, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(EVENT_INFO.DATE_END, key, value, exactCriteria).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    @Override
    public boolean validateIsTrulyNew(P pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(EVENT_INFO.EVENT_STATUS.getName(), pojo.getEventStatus()),
                entry(EVENT_INFO.EVENT_NAME.getName(), pojo.getEventName()),
                entry(EVENT_INFO.EVENT_TITLE.getName(), pojo.getEventTitle()),
                entry(EVENT_INFO.DATE_START.getName(), pojo.getDateStart().format(DateTimeFormatter.ISO_DATE)),
                entry(EVENT_INFO.DATE_END.getName(), pojo.getDateEnd().format(DateTimeFormatter.ISO_DATE)));
        return fetchByCriteria(true, criteria, EVENT_INFO.ID.getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<EventInfo> fetchById(Long id) {
        return jooq
                .selectFrom(EVENT_INFO)
                .where(EVENT_INFO.ID.eq(id))
                .fetchOptionalInto(EventInfo.class);
    }

    @Override
    public Field<?> resolveField(String columnName, Field<?> defaultField) {
        return FIELDS.getOrDefault(columnName, defaultField);
    }

    @Override
    public ListWithCount<EventInfo> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, int offset, int max) {
        List<Condition> conditions = parseCriteriaIntoConditions(exactCriteria, searchCriteria);

        List<EventInfo> results = jooq
                .selectFrom(EVENT_INFO)
                .where(buildWhereClause(exactCriteria, conditions))
                .orderBy(sortAscending
                        ? resolveField(sortColumn, EVENT_INFO.ID).asc()
                        : resolveField(sortColumn, EVENT_INFO.ID).desc())
                .limit(offset, max)
                .fetchStreamInto(EventInfo.class)
                .toList();
        int count = jooq
                .selectCount()
                .from(EVENT_INFO)
                .where(conditions)
                .fetchOptional(0, Integer.class)
                .orElse(0);
        return new ListWithCount<>(results, count);
    }

    @Override
    public Optional<Long> insertNew(P pojo) {
        return jooq
                .insertInto(EVENT_INFO)
                .set(EVENT_INFO.EVENT_STATUS, pojo.getEventStatus())
                .set(EVENT_INFO.EVENT_NAME, pojo.getEventName())
                .set(EVENT_INFO.EVENT_TITLE, pojo.getEventTitle())
                .set(EVENT_INFO.DATE_START, pojo.getDateStart())
                .set(EVENT_INFO.DATE_END, pojo.getDateEnd())
                .returning(EVENT_INFO.ID)
                .fetchOptional()
                .map(EventInfoRecord::getId);
    }

    @Override
    public int updateExisting(P update) {
        return jooq
                .update(EVENT_INFO)
                .set(EVENT_INFO.EVENT_STATUS, update.getEventStatus())
                .set(EVENT_INFO.EVENT_NAME, update.getEventName())
                .set(EVENT_INFO.EVENT_TITLE, update.getEventTitle())
                .set(EVENT_INFO.DATE_START, update.getDateStart())
                .set(EVENT_INFO.DATE_END, update.getDateEnd())
                .where(EVENT_INFO.ID.eq(update.getId()))
                .and(
                    EVENT_INFO.EVENT_STATUS.notEqual(update.getEventStatus())
                    .or(EVENT_INFO.EVENT_NAME.notEqual(update.getEventName()))
                    .or(EVENT_INFO.EVENT_TITLE.notEqual(update.getEventTitle()))
                    .or(EVENT_INFO.DATE_START.notEqual(update.getDateStart()))
                    .or(EVENT_INFO.DATE_END.notEqual(update.getDateEnd()))
                ).execute();
    }
}
