package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;

import java.io.Serializable;

public abstract class WriteAndDeleteLogic<R extends TableRecordImpl<R>, P extends Serializable, T extends TableImpl<R>> extends WriteLogic<R, P, T> implements DeletableRecord {

    public WriteAndDeleteLogic(ObjectMapper objectMapper, DSLContext jooq, Class<P> recordType, T table, TableField<R, Long> idColumn
    ) {
        super(objectMapper, jooq, recordType, table, idColumn);
    }
}
