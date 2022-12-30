package net.briclabs.evcoordinator.participant;

import net.briclabs.evcoordinator.ApiController;
import net.briclabs.evcoordinator.ApiVersion;
import net.briclabs.evcoordinator.generated.tables.daos.ParticipantAssociationRecordDao;
import net.briclabs.evcoordinator.generated.tables.pojos.ParticipantAssociationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(ApiVersion.V1 + "/participant/assoc")
public class ParticipantAssociationController extends ApiController {


    @Autowired
    private ParticipantAssociationRecordDao dao;

    @GetMapping(value = "/{id}")
    public ParticipantAssociationRecord findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }
}
