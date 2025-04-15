package net.briclabs.evcoordinator;

import java.io.Serializable;
import java.util.Map;

public interface Validatable<P extends Serializable> {
    /**
     * Validates a given POJO.
     * This method is used to ensure that the provided POJO adheres to expected constraints or requirements.
     *
     * @param pojo the Plain Old Java Object (POJO) to validate.
     * @return A map containing informational or error messages associated with the update operation.
     *         The map's keys are the names of fields. The values are the messages specific to those fields.
     */
    Map<String, String> validate(P pojo);
}
