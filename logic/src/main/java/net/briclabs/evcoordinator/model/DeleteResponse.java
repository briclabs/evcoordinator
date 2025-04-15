package net.briclabs.evcoordinator.model;

import java.util.Map;

/**
 * Represents the response to a delete operation.
 * <p>
 * This record class encapsulates information related to a delete request.
 * It is primarily used to transfer the outcome of CRUD operations, specifically
 * deletions, over API responses.
 *
 * @param messages A map containing informational or error messages associated with the delete operation.
 *                 The map's keys are the names of fields or entities. The values are the messages specific to those fields or entities.
 */
public record DeleteResponse(Map<String,String> messages) {
}
