package net.briclabs.evcoordinator.controller.participant;

import net.briclabs.evcoordinator.ParticipantAssociationLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociation;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantAssociationRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping(ApiController.V1 + "/participant/assoc")
public class ParticipantAssociationController extends ApiController<ParticipantAssociationLogic> {

    public static final Class<ParticipantAssociation> POJO = ParticipantAssociation.class;

    @Autowired
    public ParticipantAssociationController(DSLContext dslContext) {
        super(dslContext, new ParticipantAssociationLogic(dslContext));
    }

    @GetMapping(value = "/{id}")
    public ParticipantAssociation findById(@PathVariable("id") Long id)
    {
        return logic.fetchById(id).map(r -> r.into(POJO)).orElse(null);
    }

    @GetMapping(value = "/{offset}/{max}")
    public List<ParticipantAssociation> findByCriteria(
            @PathVariable("offset") int offset,
            @PathVariable("max") int max,
            @RequestParam Map<String, String> criteria) {
        return logic.fetchByCriteria(criteria, offset, max).stream().map(result -> result.into(POJO)).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody ParticipantAssociation participantAssociation)
    {
        if (logic.isEntryAlreadyExists(participantAssociation)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(participantAssociation).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long update(@PathVariable("id") Long id, @RequestBody ParticipantAssociation updatedParticipantAssociation)
    {
        Optional<Long> existingMatchingRecordId = logic.isUpdateRedundant(id, updatedParticipantAssociation);

        if (existingMatchingRecordId.isEmpty()) {
            ParticipantAssociationRecord existingRecord = logic.fetchById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED));
            ParticipantAssociationRecord updateRecord = new ParticipantAssociationRecord(updatedParticipantAssociation);
            return logic.updateExisting(updateRecord, existingRecord);
        }

        if (existingMatchingRecordId.get().equals(id)) {
            return id;
        } else {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
    }
}
