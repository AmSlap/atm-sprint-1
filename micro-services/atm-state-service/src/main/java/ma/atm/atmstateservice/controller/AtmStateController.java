package ma.atm.atmstateservice.controller;


import ma.atm.atmstateservice.dto.*;
import ma.atm.atmstateservice.feign.RegistryServiceClient;
import ma.atm.atmstateservice.model.AtmConfiguration;
import ma.atm.atmstateservice.model.AtmCounter;
import ma.atm.atmstateservice.model.AtmStatus;
import ma.atm.atmstateservice.model.Cassette;
import ma.atm.atmstateservice.repository.AtmConfigurationRepository;
import ma.atm.atmstateservice.repository.AtmCounterRepository;
import ma.atm.atmstateservice.repository.AtmStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/state/atms")
public class AtmStateController {

    private final AtmStatusRepository statusRepository;
    private final AtmConfigurationRepository configRepository;
    private final AtmCounterRepository counterRepository;
    private final RegistryServiceClient registryClient; // Add Feign client

    @Autowired
    public AtmStateController(AtmStatusRepository statusRepository,
                              AtmConfigurationRepository configRepository,
                              AtmCounterRepository counterRepository,
                              RegistryServiceClient registryClient) {
        this.statusRepository = statusRepository;
        this.configRepository = configRepository;
        this.counterRepository = counterRepository;
        this.registryClient = registryClient;
    }

    // --- EXISTING ENDPOINTS (unchanged) ---

    // Endpoint to get combined full state for a single ATM
    @GetMapping("/{atmId}")
    public ResponseEntity<AtmFullStateDto> getFullAtmState(@PathVariable String atmId) {
        Optional<AtmStatus> statusOpt = statusRepository.findById(atmId);
        Optional<AtmConfiguration> configOpt = configRepository.findById(atmId);
        Optional<AtmCounter> counterOpt = counterRepository.findById(atmId);

        if (statusOpt.isEmpty() && configOpt.isEmpty() && counterOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AtmFullStateDto fullState = new AtmFullStateDto(
                atmId,
                statusOpt.map(this::mapToStatusDto).orElse(null),
                configOpt.map(this::mapToConfigDto).orElse(null),
                counterOpt.map(this::mapToCounterDto).orElse(null)
        );
        return ResponseEntity.ok(fullState);
    }

    // Endpoint to get just the status
    @GetMapping("/{atmId}/status")
    public ResponseEntity<AtmStatusDto> getAtmStatus(@PathVariable String atmId) {
        return statusRepository.findById(atmId)
                .map(this::mapToStatusDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get just the configuration
    @GetMapping("/{atmId}/configuration")
    public ResponseEntity<AtmConfigurationDto> getAtmConfiguration(@PathVariable String atmId) {
        return configRepository.findById(atmId)
                .map(this::mapToConfigDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get just the counters
    @GetMapping("/{atmId}/counters")
    public ResponseEntity<AtmCounterDto> getAtmCounters(@PathVariable String atmId) {
        return counterRepository.findById(atmId)
                .map(this::mapToCounterDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get a summary list of all ATMs (Consider Pagination)
    @GetMapping
    public ResponseEntity<List<AtmStateSummaryDto>> getAllAtmSummaries() {
        // This is a simplified approach. A more efficient way would be a custom query
        // or fetching only necessary data, especially with many ATMs.
        List<String> atmIds = statusRepository.findAll().stream().map(AtmStatus::getAtmId).collect(Collectors.toList());
        // You might want to get IDs from config or counter repo as well or use a distinct list

        List<AtmStateSummaryDto> summaries = atmIds.stream().map(atmId -> {
            AtmStatus status = statusRepository.findById(atmId).orElse(null);
            AtmConfiguration config = configRepository.findById(atmId).orElse(null);
            AtmCounter counter = counterRepository.findById(atmId).orElse(null);

            OffsetDateTime lastUpdate = null;
            if(status != null) lastUpdate = status.getLastUpdateTimestamp();
            // Add logic to find the latest timestamp among status, config, counter if needed

            return new AtmStateSummaryDto(
                    atmId,
                    status != null ? status.getOperationalState() : "UNKNOWN",
                    config != null ? config.getOverallHealth() : "UNKNOWN",
                    counter != null ? counter.getLowCashFlag() : null,
                    lastUpdate
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }

    // --- NEW ENHANCED ENDPOINTS ---

    /**
     * Get combined ATM data with registry information for dashboard
     * This is the main endpoint the frontend dashboard will use
     */
    @GetMapping("/combined")
    public ResponseEntity<List<AtmCombinedDto>> getAllAtmsWithRegistryInfo() {
        try {
            // Get state summaries from existing method
            List<AtmStateSummaryDto> stateSummaries = getAllAtmSummaries().getBody();
            if (stateSummaries == null) {
                stateSummaries = Collections.emptyList();
            }

            // Get registry data via Feign client
            List<AtmRegistryDto> registryData = Collections.emptyList();
            try {
                registryData = registryClient.getAllAtmRegistry();
            } catch (Exception e) {
                System.err.println("Warning: Could not fetch registry data: " + e.getMessage());
                // Continue with empty registry data
            }

            // Create lookup map for performance
            Map<String, AtmRegistryDto> registryMap = registryData.stream()
                    .collect(Collectors.toMap(AtmRegistryDto::getAtmId, Function.identity()));

            // Combine the data
            List<AtmCombinedDto> combinedData = stateSummaries.stream()
                    .map(summary -> createCombinedDto(summary, registryMap.get(summary.getAtmId())))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(combinedData);

        } catch (Exception e) {
            System.err.println("Error creating combined ATM data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get enhanced ATM details with registry information
     * This provides enriched data for the ATM detail page
     */
    @GetMapping("/{atmId}/enhanced")
    public ResponseEntity<AtmEnhancedDetailDto> getEnhancedAtmDetails(@PathVariable String atmId) {
        try {
            // Get full state data from existing method
            AtmFullStateDto stateData = getFullAtmState(atmId).getBody();
            if (stateData == null) {
                return ResponseEntity.notFound().build();
            }

            // Get registry data for this ATM
            AtmRegistryDto registryData = null;
            try {
                registryData = registryClient.getAtmRegistry(atmId);
            } catch (Exception e) {
                System.err.println("Warning: Could not fetch registry data for ATM " + atmId + ": " + e.getMessage());
                // Continue without registry data
            }

            // Create enhanced response
            AtmEnhancedDetailDto enhancedDetails = createEnhancedDetailDto(stateData, registryData);

            return ResponseEntity.ok(enhancedDetails);

        } catch (Exception e) {
            System.err.println("Error fetching enhanced ATM details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get peripheral status details for an ATM
     * This provides detailed peripheral information for the frontend
     */
    @GetMapping("/{atmId}/peripherals")
    public ResponseEntity<Map<String, Object>> getPeripheralStatus(@PathVariable String atmId) {
        try {
            // Get configuration data which contains peripheral details
            AtmConfigurationDto config = getAtmConfiguration(atmId).getBody();
            if (config == null || config.getPeripheralDetails() == null) {
                return ResponseEntity.ok(Collections.emptyMap());
            }

            // Return peripheral details
            return ResponseEntity.ok(config.getPeripheralDetails());

        } catch (Exception e) {
            System.err.println("Error fetching peripheral status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get agencies list from registry service (proxy endpoint)
     * This allows frontend to get agencies without direct registry service calls
     */
    @GetMapping("/agencies")
    public ResponseEntity<List<AgencyDto>> getAllAgencies() {
        try {
            List<AgencyDto> agencies = registryClient.getAllAgencies();
            return ResponseEntity.ok(agencies);
        } catch (Exception e) {
            System.err.println("Error fetching agencies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    /**
     * Check if ATM exists in registry (proxy endpoint)
     * This helps validate ATM IDs before operations
     */
    @GetMapping("/{atmId}/registry-exists")
    public ResponseEntity<Boolean> checkAtmExistsInRegistry(@PathVariable String atmId) {
        try {
            AtmRegistryDto registry = registryClient.getAtmRegistry(atmId);
            return ResponseEntity.ok(registry != null);
        } catch (Exception e) {
            // If registry service is down or ATM not found, return false
            return ResponseEntity.ok(false);
        }
    }

    // --- HELPER METHODS ---

    /**
     * Create combined DTO from state summary and registry data
     */
    private AtmCombinedDto createCombinedDto(AtmStateSummaryDto summary, AtmRegistryDto registry) {
        AtmCombinedDto combined = new AtmCombinedDto();

        // Copy state data
        combined.setAtmId(summary.getAtmId());
        combined.setOperationalState(summary.getOperationalState());
        combined.setOverallHealth(summary.getOverallHealth());
        combined.setLowCashFlag(summary.getLowCashFlag());
        combined.setLastUpdateTimestamp(summary.getLastUpdateTimestamp());

        // Copy registry data if available
        if (registry != null) {
            combined.setLabel(registry.getLabel());
            combined.setBrand(registry.getBrand());
            combined.setModel(registry.getModel());
            combined.setRegion(registry.getRegion());
            combined.setAgencyCode(registry.getAgencyCode());
            combined.setAgencyName(registry.getAgencyName());
            combined.setLocationAddress(registry.getLocationAddress());
            combined.setLocationLatitude(registry.getLocationLatitude());
            combined.setLocationLongitude(registry.getLocationLongitude());
            combined.setIpAddress(registry.getIpAddress());
        }

        return combined;
    }

    /**
     * Create enhanced detail DTO with both state and registry data
     */
    private AtmEnhancedDetailDto createEnhancedDetailDto(AtmFullStateDto state, AtmRegistryDto registry) {
        AtmEnhancedDetailDto enhanced = new AtmEnhancedDetailDto();

        // Copy all state data
        enhanced.setAtmId(state.getAtmId());
        enhanced.setStatus(state.getStatus());
        enhanced.setConfiguration(state.getConfiguration());
        enhanced.setCounters(state.getCounters());

        // Add registry information
        enhanced.setRegistryInfo(registry);

        return enhanced;
    }

    // --- EXISTING MAPPERS (unchanged) ---

    private AtmStatusDto mapToStatusDto(AtmStatus entity) {
        if (entity == null) return null;
        return new AtmStatusDto(entity.getAtmId(), entity.getOperationalState(), entity.getMaintenanceMode(),
                entity.getLastSuccessfulConnection(), entity.getLastSuccessfulTransaction(), entity.getLastUpdateTimestamp());
    }

    private AtmConfigurationDto mapToConfigDto(AtmConfiguration entity) {
        if (entity == null) return null;
        return new AtmConfigurationDto(entity.getAtmId(), entity.getOverallHealth(),
                entity.getPeripheralDetails(), entity.getLastUpdateTimestamp());
    }

    private CassetteDto mapToCassetteDto(Cassette entity) {
        if (entity == null) return null;
        return new CassetteDto(entity.getCassetteId(), entity.getDenomination(), entity.getCurrency(),
                entity.getNotesRemaining(), entity.getCassetteStatus());
    }

    private AtmCounterDto mapToCounterDto(AtmCounter entity) {
        if (entity == null) return null;
        List<CassetteDto> cassetteDtos = entity.getCassettes() == null ? null :
                entity.getCassettes().stream().map(this::mapToCassetteDto).collect(Collectors.toList());
        return new AtmCounterDto(entity.getAtmId(), entity.getTotalCashAvailable(), entity.getLowCashFlag(),
                entity.getRejectBinPercentageFull(), cassetteDtos, entity.getLastUpdateTimestamp());
    }
}