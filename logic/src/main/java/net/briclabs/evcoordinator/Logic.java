package net.briclabs.evcoordinator;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.tools.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Remove any fields that are not valid fields for the given table.
     * @param searchCriteria the criteria received.
     * @param table the applicable table.
     * @param <R> the type of record belonging to that table.
     */
    static <R extends UpdatableRecordImpl<R>> void stripOutUnknownFields(Map<String, String> searchCriteria, TableImpl<R> table) {
        searchCriteria.entrySet().stream().filter(e -> StringUtils.isBlank(e.getValue().trim()) || table.fieldStream().noneMatch(field -> field.getName().equalsIgnoreCase(e.getKey())))
                .map(Map.Entry::getKey)
                .forEach(searchCriteria::remove);
    }

    public static <R extends UpdatableRecordImpl<R>, T> Optional<Condition> buildPossibleCondition(TableField<R, T> field, String key, T value) {
        return field.getName().equalsIgnoreCase(key) ? Optional.of(field.eq(value)) : Optional.empty();
    }
}
