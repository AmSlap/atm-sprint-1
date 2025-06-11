package ma.atm.atmstateservice.controller;


import ma.atm.atmstateservice.dto.*;
import ma.atm.atmstateservice.model.AtmConfiguration;
import ma.atm.atmstateservice.model.AtmCounter;
import ma.atm.atmstateservice.model.AtmStatus;
import ma.atm.atmstateservice.model.Cassette;
import ma.atm.atmstateservice.repository.AtmConfigurationRepository;
import ma.atm.atmstateservice.repository.AtmCounterRepository;
import ma.atm.atmstateservice.repository.AtmStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/state/atms" )
public class AtmStateController {

    private final AtmStatusRepository statusRepository;
    private final AtmConfigurationRepository configRepository;
    private final AtmCounterRepository counterRepository;

    @Autowired
    public AtmStateController(AtmStatusRepository statusRepository,
                              AtmConfigurationRepository configRepository,
                              AtmCounterRepository counterRepository) {
        this.statusRepository = statusRepository;
        this.configRepository = configRepository;
        this.counterRepository = counterRepository;
    }

    // --- Mappers ---
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
    // --- End Mappers ---

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
}