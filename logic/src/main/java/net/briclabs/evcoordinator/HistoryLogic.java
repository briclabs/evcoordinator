package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistoryWithLabels;
import net.briclabs.evcoordinator.generated.tables.records.DataHistoryRecord;
import net.briclabs.evcoordinator.generated.tables.records.DataHistoryWithLabelsRecord;
import org.jooq.DSLContext;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static net.briclabs.evcoordinator.generated.Tables.DATA_HISTORY_WITH_LABELS;
import static net.briclabs.evcoordinator.generated.tables.DataHistory.DATA_HISTORY;

public class HistoryLogic extends WriteLogic<DataHistoryRecord, DataHistory, net.briclabs.evcoordinator.generated.tables.DataHistory> {

    /**
     * Constructor for the HistoryLogic class, initializing components required for data history management.
     *
     * @param objectMapper the ObjectMapper instance used for JSON processing
     * @param jooq the DSLContext instance for database access and query execution
     */
    public HistoryLogic(ObjectMapper objectMapper, DSLContext jooq) {
        super(objectMapper, jooq, DataHistory.class, DATA_HISTORY, DATA_HISTORY.ID);
    }

    @Override
    public boolean isAlreadyRecorded(DataHistory pojo) {
        Map<String, String> criteria = Map.ofEntries(
                entry(getTable().ACTOR_ID.getName(), Long.toString(pojo.getActorId())),
                entry(getTable().ACTION_NAME.getName(), pojo.getActionName().getLiteral()),
                entry(getTable().TABLE_SOURCE.getName(), pojo.getTableSource().getLiteral()),
                entry(getTable().NEW_DATA.getName(), pojo.getNewData().toString()),
                entry(getTable().OLD_DATA.getName(), pojo.getOldData().toString()));
        return fetchByCriteria(true, criteria, getIdColumn().getName(), false,0, 1).count() > 0;
    }

    @Override
    public Optional<Long> insertNew(long actorId, DataHistory pojo) {
        if (actorId > 0) {
            return jooq
                    .insertInto(getTable())
                    .set(getTable().ACTOR_ID, actorId)
                    .set(getTable().ACTION_NAME, pojo.getActionName())
                    .set(getTable().TABLE_SOURCE, pojo.getTableSource())
                    .set(getTable().NEW_DATA, pojo.getNewData())
                    .set(getTable().OLD_DATA, pojo.getOldData())
                    .returning(getIdColumn())
                    .fetchOptional()
                    .map(DataHistoryRecord::getId);
        }
        return Optional.empty();
    }

    @Override
    public int updateExisting(long actorId, DataHistory update) {
        throw new UnsupportedOperationException("History records cannot be updated. Use the insertNew method to insert a new record.");
    }

    /**
     * Special logic class that works with a view instead of the raw table. The view provides useful label information for rows which, in the table, are simply FKs.
     */
    public static class DataHistoryWithLabelsLogic extends Logic<DataHistoryWithLabelsRecord, DataHistoryWithLabels, net.briclabs.evcoordinator.generated.tables.DataHistoryWithLabels> {

        public DataHistoryWithLabelsLogic(ObjectMapper objectMapper, DSLContext jooq) {
            super(objectMapper, jooq, DataHistoryWithLabels.class, DATA_HISTORY_WITH_LABELS, DATA_HISTORY_WITH_LABELS.ID);
        }
    }

    @Override
    public Map<String, String> validate(DataHistory pojo) {
        return Collections.emptyMap();
    }
}
