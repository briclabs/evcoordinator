package net.briclabs.evcoordinator;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class Logic<R extends UpdatableRecordImpl<R>, P extends Serializable, T extends TableImpl<R>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Logic.class);

    final DSLContext jooq;
    private final Class<P> recordType;
    private final T table;
    private final TableField<R, Long> idColumn;


    public Logic(DSLContext jooq, Class<P> recordType, T table, TableField<R, Long> idColumn
    ) {
        this.jooq = jooq;
        this.recordType = recordType;
        this.table = table;
        this.idColumn = idColumn;

    }

    /**
     * Provides access to the table associated with the logic implementation.
     * @return a jOOQ TableImpl<?> instance that represents the table managed by this logic.
     */
    T getTable() {
        return table;
    }

    /**
     * Retrieves the column representing the primary key ID for the associated database table.
     *
     * @return the TableField instance that represents the primary key ID column.
     */
    TableField<R, Long> getIdColumn() {
        return idColumn;
    }

    /**
     * Provides the class type of the record managed by this logic.
     *
     * @return the class representing the record type.
     */
    Class<P> getRecordType() {
        return recordType;
    }

    /**
     * Fetches a record from the database based on its ID.
     *
     * @param id the ID of the record to be fetched.
     * @return an {@code Optional} containing the fetched record if it exists, or an empty {@code Optional} if no record is found.
     */
    public Optional<P> fetchById(Long id) {
        return jooq
                .selectFrom(getTable())
                .where(getIdColumn().eq(id))
                .fetchOptionalInto(getRecordType());
    }

    /**
     * Fetches records from the database using the given criteria.
     *
     * @param exactCriteria  If true, conditions are combined with AND; otherwise, OR is used.
     * @param searchCriteria A map of field names to values for filtering results.
     * @param sortColumn     The name of the column to sort by (optional).
     * @param sortAscending  Whether to sort in ascending order (optional, defaults to false).
     * @param offset         The number of records to skip (optional, defaults to 0).
     * @param max            The maximum number of records to return (optional, defaults to all).
     * @return A list of matching records.
     */
    public ListWithCount<P> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, Integer offset, Integer max) {
        List<Condition> conditions = parseCriteriaIntoConditions(exactCriteria, searchCriteria);
        Condition whereClause = buildWhereClause(exactCriteria, conditions);

        var query = jooq
                .selectFrom(getTable())
                .where(whereClause)
                .orderBy(sortAscending
                        ? resolveField(sortColumn, getIdColumn()).asc()
                        : resolveField(sortColumn, getIdColumn()).desc())
                .limit(offset, max);
        LOGGER.info("Query SQL: {}", query.getQuery().getSQL());
        int count = jooq
                .selectCount()
                .from(getTable())
                .where(whereClause)
                .fetchOptional(0, Integer.class)
                .orElse(0);
        return new ListWithCount<>(query.fetchStreamInto(getRecordType()).toList(), count);
    }

    private static <R extends UpdatableRecordImpl<R>> Map<String, String> stripOutUnknownFields(Map<String, String> searchCriteria, Table<R> table) {
        return searchCriteria.entrySet().stream()
                .filter(entry -> !StringUtils.isBlank(entry.getValue().trim()) && table.fieldStream().anyMatch(field -> field.getName().equalsIgnoreCase(entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Condition buildWhereClause(boolean exactCriteria, List<Condition> conditions) {
        if (conditions.isEmpty()) {
            return DSL.noCondition();
        } else {
            return exactCriteria ? DSL.and(conditions) : DSL.or(conditions);
        }
    }

    private static <T, R extends UpdatableRecord<R>> Optional<Condition> addPossibleCondition(Field<T> field, String key, String value, boolean exactCriteria) {
        return field.getName().equalsIgnoreCase(key)
                ? Optional.of(getContainsOrEqualsWithJsonBHandling(field, value, exactCriteria))
                : Optional.empty();
    }

    private static <T, R extends UpdatableRecord<R>> Condition getContainsOrEqualsWithJsonBHandling(Field<T> field, String value, boolean exactCriteria) {
        if (field.getDataType().getType().equals(JSON.class)) {
            return exactCriteria ? field.cast(JSONB.class).eq(JSONB.valueOf(value)) : field.cast(JSONB.class).containsIgnoreCase(JSONB.valueOf(value));
        } else {
            return exactCriteria ? field.eq(field.getDataType().convert(value)) : field.containsIgnoreCase(field.getDataType().convert(value));
        }
    }

    private List<Condition> parseCriteriaIntoConditions(boolean exactCriteria, Map<String, String> searchCriteria) {
        var revisedSearchCriteria = stripOutUnknownFields(searchCriteria, getTable());
        List<Condition> matchConditions = new ArrayList<>();
        revisedSearchCriteria.forEach((key, value) -> getTable().fieldStream()
            .filter(field -> field.getName().equalsIgnoreCase(key))
            .findFirst().flatMap(field -> Logic.addPossibleCondition(field, key, value, exactCriteria)).ifPresent(matchConditions::add)
        );
        return matchConditions;
    }

    private Field<?> resolveField(String columnName, Field<?> defaultField) {
        Field<?> retrievedField = getTable().field(columnName);
        return retrievedField == null ? defaultField : retrievedField;
    }
}
