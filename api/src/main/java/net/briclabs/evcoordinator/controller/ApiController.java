package net.briclabs.evcoordinator.controller;

import net.briclabs.evcoordinator.Logic;
import org.jooq.DSLContext;
import org.jooq.impl.TableImpl;
import org.jooq.impl.UpdatableRecordImpl;

import java.io.Serializable;

public abstract class ApiController<
        R extends UpdatableRecordImpl<R>,
        P extends Serializable,
        T extends TableImpl<R>,
        L extends Logic<R, P, T>
    > implements ReadController<P> {

    public static final String V1 = "v1";

    protected final DSLContext jooq;

    protected final L logic;

    public ApiController(DSLContext dslContext, L logic) {
        this.jooq = dslContext;
        this.logic = logic;
    }
}
