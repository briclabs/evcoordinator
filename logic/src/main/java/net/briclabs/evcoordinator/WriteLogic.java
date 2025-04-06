package net.briclabs.evcoordinator;

import java.io.Serializable;
import java.util.Optional;

public interface WriteLogic<P extends Serializable> {

    /**
     * Verifies the object does not already exist. Confirms by searching for records that contain the same values.
     *
     * @param pojo the POJO being considered.
     * @return whether the POJO being considered already exists in the database.
     */
    boolean isAlreadyRecorded(P pojo);

    /**
     * Adds a new record to the database.
     * @param actorId the ID of the participant performing the action.
     * @param pojo the POJO representation of the object to be written to the database.
     * @return the Primary Key ID of the object that has been written to the database.
     */
    Optional<Long> insertNew(long actorId, P pojo);

    /**
     * Updates an existing record in the database.
     * @param actorId the ID of the participant performing the action.
     * @param update the updated POJO to use in the update.
     * @return the number of records updated.
     */
    int updateExisting(long actorId, P update);
}
