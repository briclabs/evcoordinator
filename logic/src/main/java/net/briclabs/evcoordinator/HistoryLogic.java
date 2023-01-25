package net.briclabs.evcoordinator;

import net.briclabs.evcoordinator.generated.tables.DataHistory;
import net.briclabs.evcoordinator.generated.tables.records.DataHistoryRecord;
import org.jooq.DSLContext;

import java.util.Optional;

public class HistoryLogic extends Logic {

    public HistoryLogic(DSLContext jooq) {
        super(jooq);
    }

    private static final DataHistory TABLE = DataHistory.DATA_HISTORY;

    public Optional<DataHistoryRecord> fetchById(Long id) {
        return jooq.selectFrom(TABLE).where(TABLE.ID.eq(id)).fetchOptional();
    }

    
}
