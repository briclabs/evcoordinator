package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO;

public class EventInfoLogic extends Logic {

    static List<Condition> parseCriteriaIntoConditions(Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, EVENT_INFO);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            buildPossibleCondition(EVENT_INFO.ID, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(EVENT_INFO.EVENT_STATUS, key, value).ifPresent(matchConditions::add);
            buildPossibleCondition(EVENT_INFO.EVENT_NAME, key, value).ifPresent(matchConditions::add);
            buildPossibleCondition(EVENT_INFO.EVENT_TITLE, key, value).ifPresent(matchConditions::add);
            buildPossibleCondition(EVENT_INFO.DATE_START, key, LocalDate.parse(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(EVENT_INFO.DATE_END, key, LocalDate.parse(value)).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    public EventInfoLogic(DSLContext jooq) {
        super(jooq);
    }

    public boolean validateIsTrulyNew(EventInfo pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(EVENT_INFO.EVENT_STATUS.getName(), pojo.getEventStatus()),
                entry(EVENT_INFO.EVENT_NAME.getName(), pojo.getEventName()),
                entry(EVENT_INFO.EVENT_TITLE.getName(), pojo.getEventTitle()),
                entry(EVENT_INFO.DATE_START.getName(), pojo.getDateStart().format(DateTimeFormatter.ISO_DATE)),
                entry(EVENT_INFO.DATE_END.getName(), pojo.getDateEnd().format(DateTimeFormatter.ISO_DATE)));
        return fetchByCriteria(criteria, 0, 1).size() > 0;
    }

    public Optional<EventInfo> fetchById(Long id) {
        return jooq
                .selectFrom(EVENT_INFO)
                .where(EVENT_INFO.ID.eq(id))
                .fetchOptionalInto(EventInfo.class);
    }

    public List<EventInfo> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {
        return jooq
                .selectFrom(EVENT_INFO)
                .where(parseCriteriaIntoConditions(searchCriteria))
                .orderBy(EVENT_INFO.ID)
                .limit(offset, max)
                .fetchStreamInto(EventInfo.class)
                .toList();
    }

    public Optional<Long> insertNew(EventInfo pojo) {
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

    public int updateExisting(EventInfo update) {
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
