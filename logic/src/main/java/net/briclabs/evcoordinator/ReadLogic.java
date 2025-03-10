package net.briclabs.evcoordinator;

import org.jooq.Field;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public interface ReadLogic {

    /**
     * Fetches an object from the database via its PKID.
     * @param id the Primary Key ID of the object to fetch.
     * @return a POJO representation of the object, if the object was found.
     */
    Optional<? extends Serializable> fetchById(Long id);

    /**
     * Fetches an object from the database via searchable criteria.
     * @param exactCriteria whether the criteria should be exact.
     * @param searchCriteria the criteria with which the object should be searched.
     * @param sortColumn the column on which the results should be sorted.
     * @param sortAscending whether to sort in ascending order (default = false).
     * @param offset the number of records to skip when returning.
     * @param max the maximum number of records to return.
     * @return the POJO representations of the object(s) found, if any were found.
     */
    ListWithCount<? extends Serializable> fetchByCriteria(boolean exactCriteria, Map<String, String> searchCriteria, String sortColumn, Boolean sortAscending, int offset, int max);

    /**
     * Resolves a column name to a jOOQ Field dynamically.
     *
     * @param columnName   The column name as a string.
     * @param defaultField The default field to return if the column name cannot be resolved.
     * @return The corresponding jOOQ Field.
     */
    Field<?> resolveField(String columnName, Field<?> defaultField);
}
