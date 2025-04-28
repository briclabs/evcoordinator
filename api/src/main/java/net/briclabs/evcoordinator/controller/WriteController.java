package net.briclabs.evcoordinator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.Logic;
import net.briclabs.evcoordinator.WriteLogic;
import net.briclabs.evcoordinator.model.CreateResponse;
import net.briclabs.evcoordinator.model.UpdateResponse;
import org.jooq.DSLContext;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteController.class);

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CreateResponse(-1L, errors));
        }
        if (writeLogic.isAlreadyRecorded(pojo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new CreateResponse(-1L, Map.of(Logic.GENERAL_MESSAGE_KEY, "This record already exists.")));
        }
        Optional<Long> insertedId;
        try {
            insertedId = writeLogic.insertNew(getActorId(), pojo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new CreateResponse(-1L, Map.of(Logic.GENERAL_MESSAGE_KEY, "An internal server error occurred. Please try again. If the problem persists, please contact the administrator.")));
        }
        if (insertedId.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new CreateResponse(insertedId.get(), Collections.emptyMap()));
        }
        LOGGER.error("Failed to create record.", new Exception());
        return ResponseEntity.internalServerError().body(new CreateResponse(-1L, Map.of(Logic.GENERAL_MESSAGE_KEY, "Failed to create record. Please review your input and try again.")));
    }

    /**
     * Updates an instance of this object.
     * @param pojo the updated POJO received to be written to the database.
     * @return an {@link UpdateResponse} containing the number of records updated and any messages to pass along to the consumer.
     */
    protected ResponseEntity<UpdateResponse> update(WP pojo) {
        var errors = writeLogic.validate(pojo);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UpdateResponse(0, errors));
        }
        int countOfRecordsUpdated;
        try {
            countOfRecordsUpdated = writeLogic.updateExisting(getActorId(), pojo);
        } catch (WriteLogic.LogicException e) {
            LOGGER.error("Failed to update record.", e);
            return ResponseEntity.internalServerError().body(new UpdateResponse(0, Map.ofEntries(e.getPublicMessage())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new UpdateResponse(0, Map.of(Logic.GENERAL_MESSAGE_KEY, "An internal server error occurred. Please try again. If the problem persists, please contact the administrator.")));
        }
        if (countOfRecordsUpdated == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new UpdateResponse(0, Map.of(Logic.GENERAL_MESSAGE_KEY, "No records were updated. Please review your input and try again.")));
        }
        return ResponseEntity.ok(new UpdateResponse(countOfRecordsUpdated, Collections.emptyMap()));
    }
}
