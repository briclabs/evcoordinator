package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.briclabs.evcoordinator.generated.tables.DataHistory.DATA_HISTORY;

public class HistoryLogic extends Logic {

    static List<Condition> parseCriteriaIntoConditions(Map<String, String> searchCriteria) {
        stripOutUnknownFields(searchCriteria, DATA_HISTORY);
        List<Condition> matchConditions = new ArrayList<>();
        searchCriteria.forEach((key, value) -> {
            buildPossibleCondition(DATA_HISTORY.ID, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(DATA_HISTORY.ACTION_NAME, key, value).ifPresent(matchConditions::add);
            buildPossibleCondition(DATA_HISTORY.ACTOR_ID, key, Long.parseLong(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(DATA_HISTORY.NEW_DATA, key, JSON.valueOf(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(DATA_HISTORY.OLD_DATA, key, JSON.valueOf(value)).ifPresent(matchConditions::add);
            buildPossibleCondition(DATA_HISTORY.TABLE_SOURCE, key, value).ifPresent(matchConditions::add);
        });
        return matchConditions;
    }

    public HistoryLogic(DSLContext jooq) {
        super(jooq);
    }

    public Optional<DataHistory> fetchById(Long id) {
        return jooq
                .selectFrom(DATA_HISTORY)
                .where(DATA_HISTORY.ID.eq(id))
                .fetchOptionalInto(DataHistory.class);
    }

    public List<DataHistory> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max) {
        return jooq
                .selectFrom(DATA_HISTORY)
                .where(parseCriteriaIntoConditions(searchCriteria))
                .orderBy(DATA_HISTORY.ID)
                .limit(offset, max)
                .fetchStreamInto(DataHistory.class)
                .toList();
    }

}
