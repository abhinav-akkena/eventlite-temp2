package uk.ac.man.cs.eventlite.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BaseControllerApi {

    @GetMapping(produces = "application/json")
    public Map<String, Object> getApiRoot() {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        
        //Hardcoded for now - didn't have time to think of a smarter solution
        Map<String, Object> links = new HashMap<>();
        links.put("venues", Map.of("href", baseUrl + "/venues"));
        links.put("events", Map.of("href", baseUrl + "/events"));
        links.put("profile", Map.of("href", baseUrl + "/profile"));

        return Collections.singletonMap("_links", links);
    }
}
