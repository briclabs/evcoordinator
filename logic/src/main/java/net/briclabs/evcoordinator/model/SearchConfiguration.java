package net.briclabs.evcoordinator.model;

/**
 * The configuration for the search.
 * @param exactMatch whether the results should be exact matches (`equals` and `and` vs `contains` and `or`).
 * @param sortColumn the column on which the results are to be sorted.
 * @param sortAsc whether to sort the results in ascending order.
 * @param offset how many records to skip before returning the rest.
 * @param max the maximum number of records to return.
 */
public record SearchConfiguration(boolean exactMatch, String sortColumn, boolean sortAsc, int offset, int max) {
}
