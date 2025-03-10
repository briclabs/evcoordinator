package net.briclabs.evcoordinator;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.tools.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class Logic implements ReadLogic {

    final DSLContext jooq;

    public Logic(DSLContext jooq) {
        this.jooq = jooq;
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

    /**
     * Builds the final `where` clause.
     * @param exactCriteria if `true` the conditions are strung together with the `and` operator. Otherwise, they are strung together with the `or` operator.
     * @param conditions the conditions to string together.
     * @return the built `where` clause.
     */
    static Condition buildWhereClause(boolean exactCriteria, List<Condition> conditions) {
        if (conditions.isEmpty()) {
            return DSL.noCondition();
        } else {
            return exactCriteria ? DSL.and(conditions) : DSL.or(conditions);
        }
    }

    /**
     * Potentially adds a {@link Condition}, should the provided key match the given field.
     * @param field the field for which this potential condition would be added.
     * @param key the key received, to be matched against a known field.
     * @param value the value to involve in the potential condition.
     * @param exactCriteria whether the criteria should be an exact match.
     * @return the potential condition.
     * @param <T> the type of the value, according to the given field.
     * @param <R> the record type, according to the given field.
     */
    static <T, R extends UpdatableRecord<R>> Optional<Condition> addPossibleCondition(TableField<R, T> field, String key, String value, boolean exactCriteria) {
        return field.getName().equalsIgnoreCase(key)
                ? Optional.of(getContainsOrEqualsWithJsonBHandling(field, value, exactCriteria))
                : Optional.empty();
    }

    /**
     * Builds the atomic comparison clause.
     * NOTE: You cannot do an {@link Condition#eq(Object)} on a {@link JSON} object. You have to cast it to {@link JSONB} first.
     * @param field the field being updated.
     * @param value the value of the update.
     * @param exactCriteria whether the criteria should be an exact match.
     * @return the comparison clause.
     * @param <T> the type of the table field.
     * @param <R> the type of the record.
     */
    private static <T, R extends UpdatableRecord<R>> Condition getContainsOrEqualsWithJsonBHandling(TableField<R, T> field, String value, boolean exactCriteria) {
        if (field.getDataType().getType().equals(JSON.class)) {
            return exactCriteria ? field.cast(JSONB.class).eq(JSONB.valueOf(value)) : field.cast(JSONB.class).containsIgnoreCase(JSONB.valueOf(value));
        } else {
            return exactCriteria ? field.eq(field.getDataType().convert(value)) : field.containsIgnoreCase(field.getDataType().convert(value));
        }
    }
}
