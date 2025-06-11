package ma.atm.atmregistryservice.controller;


import ma.atm.atmregistryservice.dto.AgencyDto;
import ma.atm.atmregistryservice.model.Agency;
import ma.atm.atmregistryservice.service.AgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registry/agencies" ) // Base path for agency endpoints
public class AgencyController {


    private final AgencyService agencyService;

    @Autowired
    public AgencyController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    // Convert Agency entity to DTO
    private AgencyDto convertToDto(Agency agency) {
        AgencyDto dto = new AgencyDto();
        dto.setAgencyCode(agency.getAgencyCode());
        dto.setAgencyName(agency.getAgencyName());
        dto.setRegion(agency.getRegion());
        dto.setAddress(agency.getAddress());
        dto.setContactPerson(agency.getContactPerson());
        dto.setContactEmail(agency.getContactEmail());
        dto.setContactPhone(agency.getContactPhone());
        return dto;
    }

    // Convert DTO to Agency entity
    private Agency convertToEntity(AgencyDto dto) {
        Agency agency = new Agency();
        agency.setAgencyCode(dto.getAgencyCode());
        agency.setAgencyName(dto.getAgencyName());
        agency.setRegion(dto.getRegion());
        agency.setAddress(dto.getAddress());
        agency.setContactPerson(dto.getContactPerson());
        agency.setContactEmail(dto.getContactEmail());
        agency.setContactPhone(dto.getContactPhone());
        return agency;
    }

    @PostMapping
    public ResponseEntity<AgencyDto> createAgency(@RequestBody AgencyDto agencyDto) {
        try {
            Agency agency = convertToEntity(agencyDto);
            Agency createdAgency = agencyService.createAgency(agency);
            return new ResponseEntity<>(convertToDto(createdAgency), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Handle case where agency code already exists
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<AgencyDto>> getAllAgencies() {
        List<Agency> agencies = agencyService.getAllAgencies();
        List<AgencyDto> agencyDtos = agencies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(agencyDtos);
    }

    @GetMapping("/{agencyCode}")
    public ResponseEntity<AgencyDto> getAgencyByCode(@PathVariable String agencyCode) {
        return agencyService.getAgencyByCode(agencyCode)
                .map(agency -> ResponseEntity.ok(convertToDto(agency)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{agencyCode}")
    public ResponseEntity<AgencyDto> updateAgency(@PathVariable String agencyCode, @RequestBody AgencyDto agencyDto) {
        Agency agency = convertToEntity(agencyDto);
        return agencyService.updateAgency(agencyCode, agency)
                .map(updatedAgency -> ResponseEntity.ok(convertToDto(updatedAgency)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{agencyCode}")
    public ResponseEntity<Void> deleteAgency(@PathVariable String agencyCode) {
        if (agencyService.deleteAgency(agencyCode)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}