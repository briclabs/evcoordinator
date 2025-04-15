package net.briclabs.evcoordinator.model;

import java.util.Map;

/**
 * Represents the response to an update operation.
 * <p>
 * This record class encapsulates the results and messages
 * related to an update request. It is primarily used to
 * transfer the outcome of CRUD operations, specifically updates,
 * over API responses.
 * <p>
 * Fields:
 * @param numberOfRecordsUpdated The number of records successfully updated during the operation.
 * @param messages A map containing informational or error messages associated with the update operation.
 *             The map's keys are the names of fields. The values are the messages specific to those fields.
 */
public record UpdateResponse(Integer numberOfRecordsUpdated, Map<String,String> messages) {
}
