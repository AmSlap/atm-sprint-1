package ma.atm.atmregistryservice.controller;


import jakarta.persistence.EntityNotFoundException;
import ma.atm.atmregistryservice.dto.AtmRequest;
import ma.atm.atmregistryservice.model.AtmInfo;
import ma.atm.atmregistryservice.service.AtmRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registry/atms" ) // Base path for ATM endpoints
public class AtmRegistryController {

    private final AtmRegistryService atmRegistryService;

    @Autowired
    public AtmRegistryController(AtmRegistryService atmRegistryService) {
        this.atmRegistryService = atmRegistryService;
    }

    // Helper to map DTO to Entity (Consider using MapStruct for complex mappings)
    private AtmInfo mapDtoToEntity(AtmRequest dto) {
        AtmInfo entity = new AtmInfo();
        entity.setAtmId(dto.getAtmId()); // Be careful with setting ID on updates
        entity.setSerialNumber(dto.getSerialNumber());
        entity.setBrand(dto.getBrand());
        entity.setModel(dto.getModel());
        entity.setLabel(dto.getLabel());
        entity.setIpAddress(dto.getIpAddress());
        entity.setRegion(dto.getRegion());
        entity.setLocationAddress(dto.getLocationAddress());
        entity.setLocationLatitude(dto.getLocationLatitude());
        entity.setLocationLongitude(dto.getLocationLongitude());
        // Agency relationship is handled in the service layer
        return entity;
    }

    @PostMapping
    public ResponseEntity<?> createAtm(@RequestBody AtmRequest atmRequest) {
        try {
            AtmInfo atmInfo = mapDtoToEntity(atmRequest);
            // Pass both the mapped entity and the agencyCode to the service
            AtmInfo createdAtm = atmRegistryService.createAtm(atmInfo, atmRequest.getAgencyCode());
            return new ResponseEntity<>(createdAtm, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Agency not found
        }
    }

    @GetMapping
    public ResponseEntity<List<AtmInfo>> getAllAtms() {
        List<AtmInfo> atms = atmRegistryService.getAllAtms();
        // Note: This will likely include the Agency object due to default fetch behavior
        // or if accessed. Consider creating an AtmResponse DTO to control output.
        return ResponseEntity.ok(atms);
    }

    @GetMapping("/{atmId}")
    public ResponseEntity<AtmInfo> getAtmById(@PathVariable String atmId) {
        return atmRegistryService.getAtmById(atmId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{atmId}")
    public ResponseEntity<?> updateAtm(@PathVariable String atmId, @RequestBody AtmRequest atmRequest) {
        try {
            // Ensure the ID in the path matches the ID in the body if present
            if (atmRequest.getAtmId() != null && !atmId.equals(atmRequest.getAtmId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ATM ID in path does not match ID in request body.");
            }
            AtmInfo atmInfo = mapDtoToEntity(atmRequest);
            return atmRegistryService.updateAtm(atmId, atmInfo, atmRequest.getAgencyCode())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Agency not found
        }
    }

    @DeleteMapping("/{atmId}")
    public ResponseEntity<Void> deleteAtm(@PathVariable String atmId) {
        if (atmRegistryService.deleteAtm(atmId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint to check existence (might be useful for AtmStateService)
    @GetMapping("/{atmId}/exists")
    public ResponseEntity<Void> checkAtmExists(@PathVariable String atmId) {
        return atmRegistryService.atmExists(atmId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}