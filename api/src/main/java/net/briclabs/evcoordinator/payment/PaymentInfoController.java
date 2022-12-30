package net.briclabs.evcoordinator.payment;

import net.briclabs.evcoordinator.ApiController;
import net.briclabs.evcoordinator.ApiVersion;
import net.briclabs.evcoordinator.generated.tables.daos.PaymentInfoDao;
import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(ApiVersion.V1 + "/payment/info")
public class PaymentInfoController extends ApiController {

    @Autowired
    private PaymentInfoDao dao;

    @GetMapping(value = "/{id}")
    public PaymentInfo findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }
}
