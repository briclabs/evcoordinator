package net.briclabs.evcoordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public abstract class WriteLogic<R extends TableRecordImpl<R>, P extends Serializable, T extends TableImpl<R>> extends Logic<R, P, T> implements Validatable<P> {

    public WriteLogic(ObjectMapper objectMapper, DSLContext jooq, Class<P> recordType, T table, TableField<R, Long> idColumn
    ) {
        super(objectMapper, jooq, recordType, table, idColumn);
    }

    /**
     * Verifies the object does not already exist. Confirms by searching for records that contain the same values.
     *
     * @param pojo the POJO being considered.
     * @return whether the POJO being considered already exists in the database.
     */
    abstract public boolean isAlreadyRecorded(P pojo);

    /**
     * Adds a new record to the database.
     * @param actorId the ID of the participant performing the action.
     * @param pojo the POJO representation of the object to be written to the database.
     * @return the Primary Key ID of the object that has been written to the database.
     */
    abstract public Optional<Long> insertNew(long actorId, P pojo);

    /**
     * Updates an existing record in the database.
     * @param actorId the ID of the participant performing the action.
     * @param update the updated POJO to use in the update.
     * @return the number of records updated.
     * @throws LogicException if something goes wrong during the update.
     */
    abstract public int updateExisting(long actorId, P update) throws LogicException;

    /**
     * Represents an exception that occurs during the execution of logic operations.
     * This exception is designed to provide both a public-facing message and a
     * troubleshooting message for internal debugging purposes.
     * <p>
     * The public message is intended to be user-friendly and can be displayed
     * to end users, while the troubleshooting message is meant for developers
     * or system administrators to diagnose the underlying issue.
     */
    public static class LogicException extends Exception {
        private final Map.Entry<String, String> publicMessage;

        /**
         * Constructs a LogicException with a specific public-facing message and troubleshooting message.
         *
         * @param publicMessage         a key-value pair ({@link Map.Entry}) representing the public-facing
         *                              message, where the key describes the field impacted,
         *                              and the value contains a user-friendly message about the field.
         * @param troubleshootingMessage a detailed message intended for use by developers or system
         *                               administrators for debugging and diagnosing the underlying issue.
         */
        public LogicException(Map.Entry<String, String> publicMessage, String troubleshootingMessage) {
            super(troubleshootingMessage);
            this.publicMessage = publicMessage;
        }

        /**
         * Retrieves the public-facing message associated with this exception.
         * The public-facing message provides a user-friendly key-value pair that can be presented to end users.
         *
         * @return a {@link Map.Entry} containing the public-facing message, where the key represents the impacted field
         *         and the value contains a user-friendly explanation of the issue.
         */
        public Map.Entry<String, String> getPublicMessage() {
            return publicMessage;
        }
    }
}
