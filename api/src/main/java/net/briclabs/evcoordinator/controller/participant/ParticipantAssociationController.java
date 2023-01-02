package net.briclabs.evcoordinator.controller.participant;

import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.daos.ParticipantAssociationRecordDao;
import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociationRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping(ApiController.V1 + "/participant/assoc")
public class ParticipantAssociationController extends ApiController {


    @Autowired
    private ParticipantAssociationRecordDao dao;

    public ParticipantAssociationController(DSLContext dslContext) {
        super(dslContext);
    }

    @GetMapping(value = "/{id}")
    public ParticipantAssociationRecord findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody ParticipantAssociationRecord participantAssociation)
    {
        if (participantAssociation != null && dao.findOptionalById(participantAssociation.getId()).isEmpty()) {
            dao.insert(participantAssociation);
        }
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody ParticipantAssociationRecord updatedParticipantAssociation)
    {
        Optional<ParticipantAssociationRecord> existingEvent = dao.findOptionalById(id);
        if (existingEvent.isEmpty()) {
            create(updatedParticipantAssociation);
            return;
        }
        if (!existingEvent.get().equals(updatedParticipantAssociation)) {
            dao.update(updatedParticipantAssociation);
        }
    }
}
