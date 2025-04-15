package net.briclabs.evcoordinator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.Logic;
import net.briclabs.evcoordinator.WriteLogic;
import net.briclabs.evcoordinator.model.CreateResponse;
import net.briclabs.evcoordinator.model.UpdateResponse;
import org.jooq.DSLContext;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public abstract class WriteController<
        RR extends TableRecordImpl<RR>,
        RP extends Serializable,
        RT extends TableImpl<RR>,
        RL extends Logic<RR, RP, RT>,
        WR extends TableRecordImpl<WR>,
        WP extends Serializable,
        WT extends TableImpl<WR>,
        WL extends WriteLogic<WR, WP, WT>>
    extends ReadController<RR, RP, RT, RL> {

    protected final WL writeLogic;

    public WriteController(ObjectMapper objectMapper, DSLContext dslContext, RL readLogic, WL writeLogic) {
        super(objectMapper, dslContext, readLogic);
        this.writeLogic = writeLogic;
    }

    /**
     * Creates an instance of this object.
     * @param pojo the POJO received to be created.
     * @return a {@link CreateResponse} containing the PKID of the created object and any messages to pass along to the consumer.
     */
    protected ResponseEntity<CreateResponse> create(WP pojo) {
        var errors = writeLogic.validate(pojo);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CreateResponse(null, errors));
        }
        if (writeLogic.isAlreadyRecorded(pojo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new CreateResponse(null, Map.of("create", "This record already exists.")));
        }
        return writeLogic.insertNew(getActorId(), pojo)
                .map(insertedId -> ResponseEntity.status(HttpStatus.OK).body(new CreateResponse(insertedId, Collections.emptyMap())))
                .orElseGet(() -> ResponseEntity.internalServerError().body(new CreateResponse(null, Map.of("create", "Failed to create record."))));
    }

    /**
     * Updates an instance of this object.
     * @param pojo the updated POJO received to be written to the database.
     * @return an {@link UpdateResponse} containing the number of records updated and any messages to pass along to the consumer.
     */
    protected ResponseEntity<UpdateResponse> update(WP pojo) {
        var errors = writeLogic.validate(pojo);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UpdateResponse(null, errors));
        }
        try {
            var countOfRecordsUpdated = writeLogic.updateExisting(getActorId(), pojo);
            return ResponseEntity.ok(new UpdateResponse(countOfRecordsUpdated, countOfRecordsUpdated > 0 ? Collections.emptyMap() : Map.of("update", "No records were updated.")));
        } catch (WriteLogic.LogicException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UpdateResponse(null, Map.ofEntries(e.getPublicMessage())));
        }
    }
}
