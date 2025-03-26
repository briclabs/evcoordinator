package net.briclabs.evcoordinator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.Serializable;

public interface WriteController<P extends Serializable> {

    /**
     * Creates an instance of this object.
     * @param pojo the POJO received to be created.
     * @return the PKID of the created object.
     * @throws HttpClientErrorException
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Long> create(@RequestBody P pojo) throws HttpClientErrorException;

    /**
     * Updates an instance of this object.
     * @param pojo the updated POJO received to be written to the database.
     * @return the number of records updated.
     */
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<Integer> update(@RequestBody P pojo);

}
