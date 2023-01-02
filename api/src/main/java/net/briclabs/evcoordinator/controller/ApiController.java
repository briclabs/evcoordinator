package net.briclabs.evcoordinator.controller;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiController.V1)
public abstract class ApiController {

    public static final String V1 = "v1";

    protected final DSLContext jooq;

    @Autowired
    public ApiController(DSLContext dslContext) {
        this.jooq = dslContext;
    }
}
