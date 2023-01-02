package net.briclabs.evcoordinator.controller.payment;

import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.daos.PaymentInfoDao;
import net.briclabs.evcoordinator.generated.tables.pojos.PaymentInfo;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping(ApiController.V1 + "/payment/info")
public class PaymentInfoController extends ApiController {

    @Autowired
    private PaymentInfoDao dao;

    public PaymentInfoController(DSLContext dslContext) {
        super(dslContext);
    }

    @GetMapping(value = "/{id}")
    public PaymentInfo findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody PaymentInfo paymentInfo)
    {
        if (paymentInfo != null && dao.findOptionalById(paymentInfo.getId()).isEmpty()) {
            dao.insert(paymentInfo);
        }
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody PaymentInfo updatedPaymentInfo)
    {
        Optional<PaymentInfo> existingEvent = dao.findOptionalById(id);
        if (existingEvent.isEmpty()) {
            create(updatedPaymentInfo);
            return;
        }
        if (!existingEvent.get().equals(updatedPaymentInfo)) {
            dao.update(updatedPaymentInfo);
        }
    }
}
