package net.briclabs.evcoordinator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReadLogic {

    /**
     * Fetches an object from the database via its PKID.
     * @param id the Primary Key ID of the object to fetch.
     * @return a POJO representation of the object, if the object was found.
     */
    Optional<? extends Serializable> fetchById(Long id);

    /**
     * Fetches an object from the database via searchable criteria.
     * @param searchCriteria the criteria with which the object should be searched.
     * @param offset the number of records to skip when returning.
     * @param max the maximum number of records to return.
     * @return the POJO representations of the object(s) found, if any were found.
     */
    List<? extends Serializable> fetchByCriteria(Map<String, String> searchCriteria, int offset, int max);
}
