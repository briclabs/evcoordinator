package net.briclabs.evcoordinator.controller;

import net.briclabs.evcoordinator.Logic;
import org.jooq.DSLContext;

public abstract class ApiController<L extends Logic> implements ReadController {

    public static final String V1 = "v1";

    protected final DSLContext jooq;

    protected final L logic;

    public ApiController(DSLContext dslContext, L logic) {
        this.jooq = dslContext;
        this.logic = logic;
    }
}
