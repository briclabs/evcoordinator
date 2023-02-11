package net.briclabs.evcoordinator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ReadController {

    @GetMapping(value = "/{id}")
    Serializable findById(@PathVariable("id") Long id);

    @GetMapping(value = "/{offset}/{max}")
    List<? extends Serializable> findByCriteria(@PathVariable("offset") int offset, @PathVariable("max") int max, @RequestParam Map<String, String> criteria);

}
