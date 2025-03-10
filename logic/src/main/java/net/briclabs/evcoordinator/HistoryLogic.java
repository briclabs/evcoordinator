package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.briclabs.evcoordinator.generated.tables.DataHistory.DATA_HISTORY;

public class HistoryLogic extends Logic {

    private static final Map<String, Field<?>> FIELDS = Map.ofEntries(
            Map.entry("ID", DATA_HISTORY.ID),
            Map.entry("ACTOR_ID", DATA_HISTORY.ACTOR_ID),
            Map.entry("ACTION_NAME", DATA_HISTORY.ACTION_NAME),
            Map.entry("TABLE_SOURCE", DATA_HISTORY.TABLE_SOURCE),
            Map.entry("NEW_DATA", DATA_HISTORY.NEW_DATA),
            Map.entry("OLD_DATA", DATA_HISTORY.OLD_DATA),
            Map.entry("TIME_RECORDED", DATA_HISTORY.TIME_RECORDED)
    );

    public HistoryLogic(DSLContext jooq) {
        super(jooq);
    }

    static List<Condition> parseCriteriaIntoConditions(boolean exactCriteria, Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, DATA_HISTORY);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            addPossibleCondition(DATA_HISTORY.ACTION_NAME, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(DATA_HISTORY.ACTOR_ID, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(DATA_HISTORY.NEW_DATA, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(DATA_HISTORY.OLD_DATA, key, value, exactCriteria).ifPresent(matchConditions::add);
            addPossibleCondition(DATA_HISTORY.TABLE_SOURCE, key, value, exactCriteria).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    @Override
    public Optional<DataHistory> fetchById(Long id) {
        return jooq
                .selectFrom(DATA_HISTORY)
                .where(DATA_HISTORY.ID.eq(id))
                .fetchOptionalInto(DataHistory.class);
    }

    @Override
    public Field<?> resolveField(String columnName, Field<?> defaultField) {
        return FIELDS.getOrDefault(columnName, defaultField);
    }

    @Override
    public ListWithCount<DataHistory> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, int offset, int max) {
        List<Condition> conditions = parseCriteriaIntoConditions(exactCriteria, searchCriteria);

        List<DataHistory> results = jooq
                .selectFrom(DATA_HISTORY)
                .where(buildWhereClause(exactCriteria, conditions))
                .orderBy(sortAscending
                        ? resolveField(sortColumn, DATA_HISTORY.ID).asc()
                        : resolveField(sortColumn, DATA_HISTORY.ID).desc())
                .limit(offset, max)
                .fetchStreamInto(DataHistory.class)
                .toList();
        int count = jooq
                .selectCount()
                .from(DATA_HISTORY)
                .where(conditions)
                .fetchOptional(0, Integer.class)
                .orElse(0);
        return new ListWithCount<>(results, count);
    }

}
