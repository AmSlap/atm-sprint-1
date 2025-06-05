package ma.atm.atmregistryservice.controller;


import ma.atm.atmregistryservice.model.Agency;
import ma.atm.atmregistryservice.service.AgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registry/agencies" ) // Base path for agency endpoints
public class AgencyController {

    private final AgencyService agencyService;

    @Autowired
    public AgencyController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    @PostMapping
    public ResponseEntity<Agency> createAgency(@RequestBody Agency agency) {
        try {
            Agency createdAgency = agencyService.createAgency(agency);
            return new ResponseEntity<>(createdAgency, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Handle case where agency code already exists
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Agency>> getAllAgencies() {
        List<Agency> agencies = agencyService.getAllAgencies();
        return ResponseEntity.ok(agencies);
    }

    @GetMapping("/{agencyCode}")
    public ResponseEntity<Agency> getAgencyByCode(@PathVariable String agencyCode) {
        return agencyService.getAgencyByCode(agencyCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{agencyCode}")
    public ResponseEntity<Agency> updateAgency(@PathVariable String agencyCode, @RequestBody Agency agencyDetails) {
        return agencyService.updateAgency(agencyCode, agencyDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{agencyCode}")
    public ResponseEntity<Void> deleteAgency(@PathVariable String agencyCode) {
        if (agencyService.deleteAgency(agencyCode)) {
            return ResponseEntity.noContent().build();
        } else {
            // Could be not found or potentially prevented due to existing ATMs
            return ResponseEntity.notFound().build();
        }
    }
}