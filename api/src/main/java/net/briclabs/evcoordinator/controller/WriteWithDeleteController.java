package net.briclabs.evcoordinator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.Logic;
import net.briclabs.evcoordinator.WriteAndDeleteLogic;
import net.briclabs.evcoordinator.WriteLogic;
import net.briclabs.evcoordinator.model.DeleteResponse;
import org.jooq.DSLContext;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteWithDeleteController.class);


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
            LOGGER.error("Failed to delete record with ID {}.", id, e);
            return ResponseEntity.internalServerError().body(new DeleteResponse(Map.ofEntries(e.getPublicMessage())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new DeleteResponse(Map.of(Logic.GENERAL_MESSAGE_KEY, "An internal server error occurred. Please try again. If the problem persists, please contact the administrator.")));
        }
        return ResponseEntity.ok().body(new DeleteResponse(Collections.emptyMap()));
    }
}
