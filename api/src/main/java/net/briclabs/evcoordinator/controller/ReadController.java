package net.briclabs.evcoordinator.controller;

import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;

public interface ReadController<P extends Serializable> {

    @GetMapping(value = "/{id}")
    ResponseEntity<P> findById(@PathVariable("id") Long id);

    @PostMapping(path = "/search")
    ResponseEntity<ListWithCount<P>> search(@RequestBody SearchRequest searchRequest);
}
