package net.briclabs.evcoordinator.event;

import net.briclabs.evcoordinator.ApiController;
import net.briclabs.evcoordinator.ApiVersion;
import net.briclabs.evcoordinator.generated.tables.daos.EventInfoDao;
import net.briclabs.evcoordinator.generated.tables.pojos.EventInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.V1 + "/event/info")
public class EventInfoController extends ApiController {

    @Autowired
    private EventInfoDao dao;

    @GetMapping(value = "/{id}")
    public EventInfo findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }
}
