package net.briclabs.evcoordinator.payment;

import net.briclabs.evcoordinator.ApiController;
import net.briclabs.evcoordinator.ApiVersion;
import net.briclabs.evcoordinator.generated.tables.daos.PaymentRecordDao;
import net.briclabs.evcoordinator.generated.tables.pojos.PaymentRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(ApiVersion.V1 + "/payment")
public class PaymentController extends ApiController {

    @Autowired
    private PaymentRecordDao dao;

    @GetMapping(value = "/{id}")
    public PaymentRecord findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }
}
