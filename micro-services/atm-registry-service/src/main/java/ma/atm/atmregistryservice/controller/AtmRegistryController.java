package ma.atm.atmregistryservice.controller;


import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.atm.atmregistryservice.dto.AtmInfoDto;
import ma.atm.atmregistryservice.dto.AtmRequest;
import ma.atm.atmregistryservice.model.AtmInfo;
import ma.atm.atmregistryservice.service.AgencyService;
import ma.atm.atmregistryservice.service.AtmRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/registry/atms" ) // Base path for ATM endpoints
public class AtmRegistryController {


    private final AtmRegistryService atmRegistryService;
    private final AgencyService agencyService;

    @Autowired
    public AtmRegistryController(AtmRegistryService atmRegistryService, AgencyService agencyService) {
        this.atmRegistryService = atmRegistryService;
        this.agencyService = agencyService;
    }


    @PostMapping
    public ResponseEntity<?> createAtm(@RequestBody AtmInfoDto atmInfoDto) {
        try {
            AtmInfo atmInfo = convertToEntity(atmInfoDto);
            AtmInfo createdAtm = atmRegistryService.createAtm(atmInfo, atmInfoDto.getAgencyCode());
            return new ResponseEntity<>(convertToDto(createdAtm), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AtmInfoDto>> getAllAtms() {
        List<AtmInfo> atms = atmRegistryService.getAllAtms();
        List<AtmInfoDto> atmDtos = atms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(atmDtos);
    }

    @GetMapping("/{atmId}")
    public ResponseEntity<AtmInfoDto> getAtmById(@PathVariable String atmId) {
        return atmRegistryService.getAtmById(atmId)
                .map(atm -> ResponseEntity.ok(convertToDto(atm)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{atmId}")
    public ResponseEntity<?> updateAtm(@PathVariable String atmId, @RequestBody AtmInfoDto atmInfoDto) {
        try {
            if (atmInfoDto.getAtmId() != null && !atmId.equals(atmInfoDto.getAtmId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("ATM ID in path does not match ID in request body.");
            }

            AtmInfo atmInfo = convertToEntity(atmInfoDto);
            return atmRegistryService.updateAtm(atmId, atmInfo, atmInfoDto.getAgencyCode())
                    .map(updatedAtm -> ResponseEntity.ok(convertToDto(updatedAtm)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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

    @GetMapping("/{atmId}/exists")
    public ResponseEntity<Void> checkAtmExists(@PathVariable String atmId) {
        return atmRegistryService.atmExists(atmId) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }



    // Convert ATM entity to DTO
    private AtmInfoDto convertToDto(AtmInfo atmInfo) {
        AtmInfoDto dto = new AtmInfoDto();
        dto.setAtmId(atmInfo.getAtmId());
        dto.setSerialNumber(atmInfo.getSerialNumber());
        dto.setBrand(atmInfo.getBrand());
        dto.setModel(atmInfo.getModel());
        dto.setLabel(atmInfo.getLabel());
        dto.setIpAddress(atmInfo.getIpAddress());
        dto.setRegion(atmInfo.getRegion());
        dto.setLocationAddress(atmInfo.getLocationAddress());
        dto.setLocationLatitude(atmInfo.getLocationLatitude());
        dto.setLocationLongitude(atmInfo.getLocationLongitude());

        // Handle agency relationship
        if (atmInfo.getAgency() != null) {
            dto.setAgencyCode(atmInfo.getAgency().getAgencyCode());
            dto.setAgencyName(atmInfo.getAgency().getAgencyName());
        }

        return dto;
    }

    // Convert DTO to entity
    private AtmInfo convertToEntity(AtmInfoDto dto) {
        AtmInfo entity = new AtmInfo();
        entity.setAtmId(dto.getAtmId());
        entity.setSerialNumber(dto.getSerialNumber());
        entity.setBrand(dto.getBrand());
        entity.setModel(dto.getModel());
        entity.setLabel(dto.getLabel());
        entity.setIpAddress(dto.getIpAddress());
        entity.setRegion(dto.getRegion());
        entity.setLocationAddress(dto.getLocationAddress());
        entity.setLocationLatitude(dto.getLocationLatitude());
        entity.setLocationLongitude(dto.getLocationLongitude());
        // Agency will be set in the service layer
        return entity;
    }
}