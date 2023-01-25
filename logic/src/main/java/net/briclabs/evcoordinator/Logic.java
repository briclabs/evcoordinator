package net.briclabs.evcoordinator;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public abstract class Logic {

    final DSLContext jooq;

    public Logic(DSLContext jooq) {
        this.jooq = jooq;
    }

    /**
     * Standardized method for obtaining "now" as a {@link OffsetDateTime} in the {@link java.time.ZoneOffset#UTC}.
     *
     * @return "now" as a {@link OffsetDateTime} in the {@link java.time.ZoneOffset#UTC}.
     */
    static OffsetDateTime getNow() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    static <R extends UpdatableRecordImpl<R>, T> void conditionallyAddMatchConditions(TableField<R, T> id, String key, T val, List<Condition> matchConditions) {
        if (key.equals(id.getName())) {
            matchConditions.add(id.eq(val));
        }
    }
}
