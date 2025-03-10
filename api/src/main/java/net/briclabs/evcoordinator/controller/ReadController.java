package net.briclabs.evcoordinator.controller;

import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;

public interface ReadController {

    @GetMapping(value = "/{id}")
    Serializable findById(@PathVariable("id") Long id);

    @PostMapping(path = "/search")
    ListWithCount<? extends Serializable> search(@RequestBody SearchRequest searchRequest);
}
