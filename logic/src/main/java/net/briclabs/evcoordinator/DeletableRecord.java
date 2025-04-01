package net.briclabs.evcoordinator;

public interface DeletableRecord {

    /**
     * Deletes a record from the database.
     * @param id the ID of the record to be deleted.
     */
    void delete(Long id);
}
