package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.generated.enums.DataHistoryType;
import net.briclabs.evcoordinator.generated.enums.TableRef;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistory;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class WriteAndDeleteLogic<R extends TableRecordImpl<R>, P extends Serializable, T extends TableImpl<R>> extends WriteLogic<R, P, T> implements DeletableRecord {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteAndDeleteLogic.class);

    public WriteAndDeleteLogic(ObjectMapper objectMapper, DSLContext jooq, Class<P> recordType, T table, TableField<R, Long> idColumn
    ) {
        super(objectMapper, jooq, recordType, table, idColumn);
    }

    /**
     * Records a history entry for a deletion action performed on a database table.
     * This method logs the details of the deletion if successful and updates the history table
     * with the deletion details, including the actor who performed the deletion and the deleted record data.
     *
     * @param historyLogic the history logic instance to use to perform the history record insertion.
     * @param actorId The unique identifier of the user or system performing the deletion.
     * @param deletedRecord A JSON object containing the details of the record that was deleted.
     */
    void recordHistoryForDeletion(HistoryLogic historyLogic, long actorId, JSON deletedRecord) {
        var tableReference = TableRef.lookupLiteral(getTable().getName().toUpperCase());
        if (tableReference == null) {
            LOGGER.error("Unable to find table reference for table '{}'. Actor '{}' attempted to delete a record '{}'.",
                    getTable().getName().toUpperCase(), actorId, deletedRecord
            );
        } else {
            historyLogic.insertNew(actorId, new DataHistory(
                    null,
                    actorId,
                    DataHistoryType.DELETED,
                    tableReference,
                    JSON.json("{}"),
                    deletedRecord,
                    null
            ));
        }
    }
}
