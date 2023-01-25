package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.Event;
import net.briclabs.evcoordinator.generated.tables.records.EventRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventLogic extends Logic {

    public EventLogic(DSLContext jooq) {
        super(jooq);
    }

    private static final net.briclabs.evcoordinator.generated.tables.Event TABLE = net.briclabs.evcoordinator.generated.tables.Event.EVENT;

    public Optional<EventRecord> fetchById(Long id) {
        return jooq.selectFrom(TABLE).where(TABLE.ID.eq(id)).fetchOptional();
    }

    public List<EventRecord> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {

        List<Condition> matchConditions = new ArrayList<>();

        // Strip out all map entries with keys not applicable to this table, and all map entries with blank values.
        searchCriteria.entrySet().stream()
                .filter(e -> StringUtils.isBlank(e.getValue().trim()) || !TABLE.fieldStream().map(Field::getName).toList().contains(e.getKey()))
                .map(Map.Entry::getKey)
                .forEach(searchCriteria::remove);

        for (Map.Entry<String, String> searchParam : searchCriteria.entrySet()) {
            String key = searchParam.getKey();
            String value = searchParam.getValue();

            conditionallyAddMatchConditions(TABLE.ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(TABLE.EVENT_ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(TABLE.PARTICIPANT_ID, key, Long.parseLong(value), matchConditions);
            conditionallyAddMatchConditions(TABLE.ACTION_TYPE, key, value, matchConditions);
        }

        return jooq.selectFrom(TABLE).where(matchConditions).orderBy(TABLE.ID).limit(offset, max).stream().toList();
    }

    public Optional<Long> insertNew(Event pojo) {
        return jooq.insertInto(TABLE)
                .set(TABLE.EVENT_ID, pojo.getEventId())
                .set(TABLE.ACTION_TYPE, pojo.getActionType())
                .set(TABLE.PARTICIPANT_ID, pojo.getParticipantId())
                .set(TABLE.TIME_RECORDED, Logic.getNow())
                .returning(TABLE.ID)
                .fetchOptional()
                .map(EventRecord::getId);
    }

    public Long updateExisting(EventRecord update, EventRecord existing) {
        if (!isUnchanged(update, existing)) {
            jooq.update(TABLE)
                    .set(TABLE.EVENT_ID, update.getEventId())
                    .set(TABLE.ACTION_TYPE, update.getActionType())
                    .set(TABLE.PARTICIPANT_ID, update.getParticipantId())
                    .set(TABLE.TIME_RECORDED, Logic.getNow())
                    .where(TABLE.ID.eq(existing.getId()))
                    .execute();
        }
        return update.getId();
    }

    public Optional<Long> isUpdateRedundant(Long id, Event update) {
        List<EventRecord> matchingEvents = fetchByCriteria(getCriteriaFromPojo(update), 0, 1);

        if (!matchingEvents.isEmpty() && matchingEvents.get(0).get(TABLE.ID).equals(id)) {
            // No change needed; just return the ID.
            return Optional.of(id);
        }

        if (!matchingEvents.isEmpty()) {
            // An update would create a record with the same mutable field values.
            return Optional.of(matchingEvents.get(0).get(TABLE.ID));
        }
        return Optional.empty();
    }

    public boolean isUnchanged(EventRecord update, EventRecord existing) {
        return new EqualsBuilder()
                .append(update.getEventId(), existing.getEventId())
                .append(update.getActionType(), existing.getActionType())
                .append(update.getParticipantId(), existing.getParticipantId())
                .build();
    }

    public boolean isEntryAlreadyExists(Event pojo) {
        return fetchByCriteria(getCriteriaFromPojo(pojo), 0, 1).size() > 0;
    }

    public Map<String, String> getCriteriaFromPojo(Event pojo) {
        return Map.of(
                TABLE.EVENT_ID.getName(), Long.toString(pojo.getEventId()),
                TABLE.ACTION_TYPE.getName(), pojo.getActionType(),
                TABLE.PARTICIPANT_ID.getName(), Long.toString(pojo.getParticipantId()));
    }
}
