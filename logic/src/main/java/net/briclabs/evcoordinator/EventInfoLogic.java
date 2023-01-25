package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.Tables;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.tools.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO;

public class EventInfoLogic extends Logic {

    public EventInfoLogic(DSLContext jooq) {
        super(jooq);
    }

    public Optional<EventInfoRecord> fetchById(Long id) {
        return jooq.selectFrom(EVENT_INFO).where(EVENT_INFO.ID.eq(id)).fetchOptional();
    }

    public List<EventInfoRecord> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {

        List<Condition> matchConditions = new ArrayList<>();

        // Strip out all map entries with keys not applicable to this table, and all map entries with blank values.
        searchCriteria.entrySet().stream()
                .filter(e -> StringUtils.isBlank(e.getValue().trim()) || !EVENT_INFO.fieldStream().map(Field::getName).toList().contains(e.getKey()))
                .map(Map.Entry::getKey)
                .forEach(searchCriteria::remove);

        for (Map.Entry<String, String> searchParam : searchCriteria.entrySet()) {
            String key = searchParam.getKey();
            String value = searchParam.getValue();

            conditionallyAddMatchConditions(EVENT_INFO.ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(EVENT_INFO.EVENT_STATUS, key, value, matchConditions);
            conditionallyAddMatchConditions(EVENT_INFO.EVENT_NAME, key, value, matchConditions);
            conditionallyAddMatchConditions(EVENT_INFO.EVENT_TITLE, key, value, matchConditions);
            conditionallyAddMatchConditions(EVENT_INFO.DATE_START, key, LocalDate.parse(value), matchConditions);
            conditionallyAddMatchConditions(EVENT_INFO.DATE_END, key, LocalDate.parse(value), matchConditions);
        }

        return jooq.selectFrom(EVENT_INFO).where(matchConditions).orderBy(EVENT_INFO.ID).limit(offset, max).stream().toList();
    }

    public Optional<Long> insertNew(EventInfo pojo) {
        return jooq.insertInto(EVENT_INFO)
                .set(EVENT_INFO.EVENT_STATUS, pojo.getEventStatus())
                .set(EVENT_INFO.EVENT_NAME, pojo.getEventName())
                .set(EVENT_INFO.EVENT_TITLE, pojo.getEventTitle())
                .set(EVENT_INFO.DATE_START, pojo.getDateStart())
                .set(EVENT_INFO.DATE_END, pojo.getDateEnd())
                .set(EVENT_INFO.TIME_RECORDED, Logic.getNow())
                .returning(EVENT_INFO.ID)
                .fetchOptional()
                .map(EventInfoRecord::getId);
    }

    public Long updateExisting(EventInfoRecord update, EventInfoRecord existing) {
        if (!isUnchanged(update, existing)) {
            jooq.update(EVENT_INFO)
                    .set(EVENT_INFO.EVENT_STATUS, update.getEventStatus())
                    .set(EVENT_INFO.EVENT_NAME, update.getEventName())
                    .set(EVENT_INFO.EVENT_TITLE, update.getEventTitle())
                    .set(EVENT_INFO.DATE_START, update.getDateStart())
                    .set(EVENT_INFO.DATE_END, update.getDateEnd())
                    .set(EVENT_INFO.TIME_RECORDED, Logic.getNow())
                    .where(EVENT_INFO.ID.eq(existing.getId()))
                    .execute();
        }
        return update.getId();
    }

    public Optional<Long> isUpdateRedundant(Long id, EventInfo update) {
        List<EventInfoRecord> matchingEvents = fetchByCriteria(getCriteriaFromPojo(update), 0, 1);

        if (!matchingEvents.isEmpty() && matchingEvents.get(0).get(Tables.EVENT_INFO.ID).equals(id)) {
            // No change needed; just return the ID.
            return Optional.of(id);
        }

        if (!matchingEvents.isEmpty()) {
            // An update would create a record with the same mutable field values.
            return Optional.of(matchingEvents.get(0).get(Tables.EVENT_INFO.ID));
        }
        return Optional.empty();
    }

    public boolean isUnchanged(EventInfoRecord update, EventInfoRecord existing) {
        return new EqualsBuilder()
                .append(update.getEventStatus(), existing.getEventStatus())
                .append(update.getEventName(), existing.getEventName())
                .append(update.getEventTitle(), existing.getEventTitle())
                .append(update.getDateStart(), existing.getDateStart())
                .append(update.getDateEnd(), existing.getDateEnd())
                .build();
    }

    public boolean isEntryAlreadyExists(EventInfo pojo) {
        return fetchByCriteria(getCriteriaFromPojo(pojo), 0, 1).size() > 0;
    }

    public Map<String, String> getCriteriaFromPojo(EventInfo pojo) {
        return Map.of(
                EVENT_INFO.EVENT_STATUS.getName(), pojo.getEventStatus(),
                EVENT_INFO.EVENT_NAME.getName(), pojo.getEventName(),
                EVENT_INFO.EVENT_TITLE.getName(), pojo.getEventTitle(),
                EVENT_INFO.DATE_START.getName(), pojo.getDateStart().format(DateTimeFormatter.ISO_DATE),
                EVENT_INFO.DATE_END.getName(), pojo.getDateEnd().format(DateTimeFormatter.ISO_DATE));
    }
}
