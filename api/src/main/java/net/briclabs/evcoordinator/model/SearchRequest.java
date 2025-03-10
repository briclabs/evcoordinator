package net.briclabs.evcoordinator.model;

import java.util.Map;

/**
 * Represents a request for searching data based on specific criteria and configuration.
 * It encapsulates the configuration for search behavior and the criteria to filter the results.
 *
 * @param searchConfiguration the configuration details for the search, such as sorting, offset, and matching preferences.
 * @param searchCriteria a map of key-value pairs representing the fields and their corresponding values to filter the search results.
 */
public record SearchRequest(SearchConfiguration searchConfiguration, Map<String, String> searchCriteria) {
}
