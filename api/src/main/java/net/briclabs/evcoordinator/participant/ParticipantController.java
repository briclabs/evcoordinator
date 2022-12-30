package net.briclabs.evcoordinator.participant;

import net.briclabs.evcoordinator.ApiController;
import net.briclabs.evcoordinator.ApiVersion;
import net.briclabs.evcoordinator.generated.tables.daos.ParticipantDao;
import net.briclabs.evcoordinator.generated.tables.pojos.Participant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.V1 + "/participant")
public class ParticipantController extends ApiController {


    @Autowired
    private ParticipantDao dao;

    @GetMapping(value = "/{id}")
    public Participant findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }
}
