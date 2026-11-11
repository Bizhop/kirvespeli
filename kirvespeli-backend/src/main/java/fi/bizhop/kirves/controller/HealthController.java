package fi.bizhop.kirves.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    // Liveness only: no user and no database access, so deploy checks and monitoring do not wake the database.
    @RequestMapping(value = "/health/live", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Map<String, String> live() {
        return Map.of("status", "UP");
    }
}
