package net.briclabs.evcoordinator.validation;

import java.util.Map;

public interface Validator {

    /**
     * Retrieves a map of validation messages collected during the validation process.
     * Each entry in the map contains a key (representing a field name) and a value
     * (representing a message associated with the field).
     *
     * @return a map where the key is the field name and the value is the corresponding
     *         validation message.
     */
    Map<String, String> getMessages();
}
