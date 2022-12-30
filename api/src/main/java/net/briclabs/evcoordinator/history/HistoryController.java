package net.briclabs.evcoordinator.history;

import net.briclabs.evcoordinator.ApiController;
import net.briclabs.evcoordinator.ApiVersion;
import net.briclabs.evcoordinator.generated.tables.daos.DataHistoryRecordDao;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiVersion.V1 + "/history")
public class HistoryController extends ApiController {

    @Autowired
    private DataHistoryRecordDao dao;

    @GetMapping(value = "/{id}")
    public DataHistoryRecord findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }
}
