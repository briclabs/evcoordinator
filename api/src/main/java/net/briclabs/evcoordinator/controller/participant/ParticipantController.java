package net.briclabs.evcoordinator.controller.participant;

import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.daos.ParticipantDao;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(ApiController.V1 + "/participant")
public class ParticipantController extends ApiController {


    @Autowired
    private ParticipantDao dao;

    public ParticipantController(DSLContext dslContext) {
        super(dslContext);
    }

    @GetMapping(value = "/{id}")
    public Participant findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody Participant participant)
    {
        if (participant != null && dao.findOptionalById(participant.getId()).isEmpty()) {
            dao.insert(participant);
        }
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody Participant updatedParticipant)
    {
        Optional<Participant> existingEvent = dao.findOptionalById(id);
        if (existingEvent.isEmpty()) {
            create(updatedParticipant);
            return;
        }
        if (!existingEvent.get().equals(updatedParticipant)) {
            dao.update(updatedParticipant);
        }
    }
}
