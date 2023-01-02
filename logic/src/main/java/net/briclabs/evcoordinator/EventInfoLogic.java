package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import net.briclabs.evcoordinator.generated.tables.records.EventInfoRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.tools.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.briclabs.evcoordinator.generated.tables.EventInfo.EVENT_INFO;

public class EventInfoLogic {

    private final DSLContext jooq;
    private static final List<String> FIELD_NAMES = EVENT_INFO.fieldStream().map(Field::getName).toList();

    public EventInfoLogic(DSLContext jooq) {
        this.jooq = jooq;
    }

    public Optional<EventInfoRecord> fetchById(Long id) {
        return jooq.selectFrom(EVENT_INFO).where(EVENT_INFO.ID.eq(id)).fetchOptional();
    }

    public List<EventInfoRecord> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {

        List<Condition> matchConditions = new ArrayList<>();

        // Strip out all map entries with keys not applicable to this table, and all map entries with blank values.
        searchCriteria.entrySet().stream()
                .filter(e -> !FIELD_NAMES.contains(e.getKey()) || StringUtils.isBlank(e.getValue().trim()))
                .map(Map.Entry::getKey)
                .forEach(searchCriteria::remove);

        for (Map.Entry<String, String> searchParam : searchCriteria.entrySet()) {
            String key = searchParam.getKey();
            String value = searchParam.getValue();

            if (key.equals(EVENT_INFO.ID.getName())) {
                matchConditions.add(EVENT_INFO.ID.eq(Long.valueOf(value)));
            }
            if (key.equals(EVENT_INFO.EVENT_STATUS.getName())) {
                matchConditions.add(EVENT_INFO.EVENT_STATUS.eq(value));
            }
            if (key.equals(EVENT_INFO.EVENT_NAME.getName())) {
                matchConditions.add(EVENT_INFO.EVENT_NAME.eq(value));
            }
            if (key.equals(EVENT_INFO.EVENT_TITLE.getName())) {
                matchConditions.add(EVENT_INFO.EVENT_TITLE.eq(value));
            }
            if (key.equals(EVENT_INFO.DATE_START.getName())) {
                matchConditions.add(EVENT_INFO.DATE_START.eq(LocalDate.parse(value)));
            }
            if (key.equals(EVENT_INFO.DATE_END.getName())) {
                matchConditions.add(EVENT_INFO.DATE_END.eq(LocalDate.parse(value)));
            }
        }

        return jooq.selectFrom(EVENT_INFO).where(matchConditions).orderBy(EVENT_INFO.ID).limit(offset, max).stream().toList();
    }

    public Optional<Long> insertNew(EventInfo eventInfo) {
        return jooq.insertInto(EVENT_INFO)
                .set(EVENT_INFO.EVENT_STATUS, eventInfo.getEventStatus())
                .set(EVENT_INFO.EVENT_NAME, eventInfo.getEventName())
                .set(EVENT_INFO.EVENT_TITLE, eventInfo.getEventTitle())
                .set(EVENT_INFO.DATE_START, eventInfo.getDateStart())
                .set(EVENT_INFO.DATE_END, eventInfo.getDateEnd())
                .set(EVENT_INFO.TIME_RECORDED, CommonLogic.getNow())
                .returning(EVENT_INFO.ID)
                .fetchOptional()
                .map(EventInfoRecord::getId);
    }

    public Long updateExisting(EventInfoRecord updatedEventInfoRecord, EventInfoRecord existingEvent) {
        if (!isUnchanged(updatedEventInfoRecord, existingEvent)) {
            jooq.update(EVENT_INFO)
                    .set(EVENT_INFO.EVENT_STATUS, updatedEventInfoRecord.getEventStatus())
                    .set(EVENT_INFO.EVENT_NAME, updatedEventInfoRecord.getEventName())
                    .set(EVENT_INFO.EVENT_TITLE, updatedEventInfoRecord.getEventTitle())
                    .set(EVENT_INFO.DATE_START, updatedEventInfoRecord.getDateStart())
                    .set(EVENT_INFO.DATE_END, updatedEventInfoRecord.getDateEnd())
                    .set(EVENT_INFO.TIME_RECORDED, CommonLogic.getNow())
                    .where(EVENT_INFO.ID.eq(existingEvent.getId()))
                    .execute();
        }
        return updatedEventInfoRecord.getId();
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
}
