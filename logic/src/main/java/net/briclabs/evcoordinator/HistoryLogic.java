package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import net.briclabs.evcoordinator.generated.tables.records.DataHistoryRecord;
import org.jooq.DSLContext;

import static net.briclabs.evcoordinator.generated.tables.DataHistory.DATA_HISTORY;

public class HistoryLogic extends Logic<DataHistoryRecord, DataHistory, net.briclabs.evcoordinator.generated.tables.DataHistory> {
    public HistoryLogic(DSLContext jooq) {
        super(jooq, DataHistory.class, DATA_HISTORY, DATA_HISTORY.ID);
    }
}
