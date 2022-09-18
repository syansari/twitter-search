package io.ansari.search.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Health {

    @GetMapping(value = "/healthcheck")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Application Running");
    }


}
