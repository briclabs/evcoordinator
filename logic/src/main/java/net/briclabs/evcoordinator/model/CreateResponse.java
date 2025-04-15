package net.briclabs.evcoordinator.model;

import java.util.Map;

/**
 * Represents the response to a create operation.
 * <p>
 * This record class encapsulates the results and messages
 * related to a create request. It is primarily used to
 * transfer the outcome of CRUD operations, specifically inserts,
 * over API responses.
 * <p>
 * Fields:
 * @param insertedId the ID of the inserted record.
 * @param messages A map containing informational or error messages associated with the update operation.
 *             The map's keys are the names of fields. The values are the messages specific to those fields.
 */
public record CreateResponse(Long insertedId, Map<String,String> messages) {
}
