package net.briclabs.evcoordinator.controller.payment;

import net.briclabs.evcoordinator.controller.ApiController;
import net.briclabs.evcoordinator.generated.tables.daos.PaymentRecordDao;
import net.briclabs.evcoordinator.generated.tables.pojos.PaymentRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping(ApiController.V1 + "/payment")
public class PaymentController extends ApiController {

    @Autowired
    private PaymentRecordDao dao;

    public PaymentController(DSLContext dslContext) {
        super(dslContext);
    }

    @GetMapping(value = "/{id}")
    public PaymentRecord findById(@PathVariable("id") Long id)
    {
        return dao.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody PaymentRecord payment)
    {
        if (payment != null && dao.findOptionalById(payment.getId()).isEmpty()) {
            dao.insert(payment);
        }
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody PaymentRecord updatedPayment)
    {
        Optional<PaymentRecord> existingEvent = dao.findOptionalById(id);
        if (existingEvent.isEmpty()) {
            create(updatedPayment);
            return;
        }
        if (!existingEvent.get().equals(updatedPayment)) {
            dao.update(updatedPayment);
        }
    }
}
