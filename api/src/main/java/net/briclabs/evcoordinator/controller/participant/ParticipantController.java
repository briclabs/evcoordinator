package net.briclabs.evcoordinator.controller.participant;

import net.briclabs.evcoordinator.ParticipantLogic;
import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import net.briclabs.evcoordinator.generated.tables.records.ParticipantRecord;
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
@RequestMapping(ApiController.V1 + "/participant")
public class ParticipantController extends ApiController<ParticipantLogic> {

    public static final Class<Participant> POJO = Participant.class;

    @Autowired
    public ParticipantController(DSLContext dslContext) {
        super(dslContext, new ParticipantLogic(dslContext));
    }

    @GetMapping(value = "/{id}")
    public Participant findById(@PathVariable("id") Long id)
    {
        return logic.fetchById(id).map(r -> r.into(POJO)).orElse(null);
    }

    @GetMapping(value = "/{offset}/{max}")
    public List<Participant> findByCriteria(
            @PathVariable("offset") int offset,
            @PathVariable("max") int max,
            @RequestParam Map<String, String> criteria) {
        return logic.fetchByCriteria(criteria, offset, max).stream().map(result -> result.into(POJO)).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody Participant participant)
    {
        if (logic.isEntryAlreadyExists(participant)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }

        return logic.insertNew(participant).orElseThrow(() -> new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Long update(@PathVariable("id") Long id, @RequestBody Participant updatedParticipant)
    {
        Optional<Long> existingMatchingRecordId = logic.isUpdateRedundant(id, updatedParticipant);

        if (existingMatchingRecordId.isEmpty()) {
            ParticipantRecord existingRecord = logic.fetchById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED));
            ParticipantRecord updateRecord = new ParticipantRecord(updatedParticipant);
            return logic.updateExisting(updateRecord, existingRecord);
        }

        if (existingMatchingRecordId.get().equals(id)) {
            return id;
        } else {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
    }
}
