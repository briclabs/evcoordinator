package net.briclabs.evcoordinator;

public interface DeletableRecord {

    /**
     * Deletes a record from the database.
     * @param actorId the ID of the participant performing the deletion.
     * @param idToDelete the ID of the record to be deleted.
     */
    void delete(long actorId, long idToDelete) throws WriteLogic.LogicException;
}
