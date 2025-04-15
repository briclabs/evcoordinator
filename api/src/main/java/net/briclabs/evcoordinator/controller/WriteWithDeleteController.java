package net.briclabs.evcoordinator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.Logic;
import net.briclabs.evcoordinator.WriteAndDeleteLogic;
import net.briclabs.evcoordinator.WriteLogic;
import net.briclabs.evcoordinator.model.DeleteResponse;
import org.jooq.DSLContext;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public abstract class WriteWithDeleteController<
        RR extends TableRecordImpl<RR>,
        RP extends Serializable,
        RT extends TableImpl<RR>,
        RL extends Logic<RR, RP, RT>,
        WDR extends TableRecordImpl<WDR>,
        WDP extends Serializable,
        WDT extends TableImpl<WDR>,
        WDL extends WriteAndDeleteLogic<WDR, WDP, WDT>>
        extends WriteController<RR, RP, RT, RL, WDR, WDP, WDT, WDL> {

    public WriteWithDeleteController(ObjectMapper objectMapper, DSLContext dslContext, RL readLogic, WDL writeAndDeleteLogic) {
        super(objectMapper, dslContext, readLogic, writeAndDeleteLogic);
    }

    /**
     * Deletes the record corresponding to the specified ID.
     * @param id the ID of the record to be deleted.
     * @return a {@link DeleteResponse} containing any messages to pass along to the consumer.
     */
    protected ResponseEntity<DeleteResponse> delete(Long id) {
        try {
            writeLogic.delete(getActorId(), id);
        } catch (WriteLogic.LogicException e) {
            return ResponseEntity.internalServerError().body(new DeleteResponse(Map.ofEntries(e.getPublicMessage())));
        }
        return ResponseEntity.ok().body(new DeleteResponse(Collections.emptyMap()));
    }
}
