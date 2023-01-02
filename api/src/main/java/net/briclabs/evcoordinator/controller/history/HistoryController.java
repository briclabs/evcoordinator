package net.briclabs.evcoordinator.controller.history;

import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.daos.DataHistoryRecordDao;
import net.briclabs.evcoordinator.generated.tables.pojos.DataHistoryRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiController.V1 + "/history")
public class HistoryController extends ApiController {
// TODO this should only have objects created via TRIGGER...
    @Autowired
    private DataHistoryRecordDao dao;

    public HistoryController(DSLContext dslContext) {
        super(dslContext);
    }

    @GetMapping(value = "/{id}")
    public DataHistoryRecord findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }

}
