package ma.atm.atmstateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.atm.atmstateservice.event.AtmConfigurationChangedEvent;
import ma.atm.atmstateservice.model.AtmConfiguration;
import ma.atm.atmstateservice.model.AtmStatus;
import ma.atm.atmstateservice.repository.AtmConfigurationRepository;
import ma.atm.atmstateservice.repository.AtmStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@Slf4j
public class ConfigurationHandlerService {

    private final AtmConfigurationRepository atmConfigurationRepository;
    private final ObjectMapper objectMapper;
    private final AtmStatusRepository atmStatusRepository;

    @Autowired
    public ConfigurationHandlerService(AtmConfigurationRepository atmConfigurationRepository,
                                       ObjectMapper objectMapper, AtmStatusRepository atmStatusRepository) {
        this.atmConfigurationRepository = atmConfigurationRepository;
        this.objectMapper = objectMapper;
        this.atmStatusRepository = atmStatusRepository;
    }


    @Transactional
    public void processConfigurationChange(AtmConfigurationChangedEvent event) {
        log.debug("Processing configuration change for ATM: {}", event.getAtmId());

        // Retrieve existing configuration, or create a new one if not found.
        AtmConfiguration configuration = atmConfigurationRepository.findById(event.getAtmId())
                .orElse(new AtmConfiguration());
        configuration.setAtmId(event.getAtmId());

        // Get the new peripherals configuration from the event.
        Map<String, Object> newPeripheralDetails = event.getPeripherals();

        if (configuration.getPeripheralDetails() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                // Convert maps to JsonNodes for deep comparison.
                JsonNode storedNode = mapper.valueToTree(configuration.getPeripheralDetails());
                JsonNode newNode = mapper.valueToTree(newPeripheralDetails);
                if (storedNode.equals(newNode)) {
                    log.info("No change detected in configuration for ATM: {}", event.getAtmId());
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to compare configuration JSON: {}", e.getMessage(), e);
            }
        }

        // Update configuration as a significant change.
        configuration.setPeripheralDetails(newPeripheralDetails);

        // Calculate overall health based on the new peripheral details.
        if (newPeripheralDetails != null) {
            configuration.setOverallHealth(calculateOverallHealth(event.getAtmId()));
        } else {
            configuration.setOverallHealth("GRAY"); // Or some default value.
        }

        configuration.setLastUpdateTimestamp(event.getTimestamp() != null ?
                event.getTimestamp().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));

        atmConfigurationRepository.save(configuration);
        log.info("Successfully processed and updated configuration change for ATM: {}", event.getAtmId());
    }

    private String calculateOverallHealth(String atmId) {
        AtmStatus atmStatus = atmStatusRepository.findById(atmId)
                .orElseThrow(() -> new RuntimeException("ATM status not found for ATM ID: " + atmId));
        return switch (atmStatus.getOperationalState()) {
            case "Good", "Working" -> "GREEN";
            case "Maintenance" -> "ORANGE";
            case "OutOfService" -> "RED";
            default -> "GRAY"; // Default state if none of the above match
        };

    }

    /**
     * Check if a component is considered critical for ATM operations
     */
    private boolean isCriticalComponent(String componentKey) {
        return componentKey.contains("cash") ||           // cashDispenser, billValidator
                componentKey.contains("card") ||           // cardReader
                componentKey.contains("pin") ||            // pinPad
                componentKey.contains("dispenser") ||      // cashDispenser
                componentKey.contains("validator") ||      // billValidator
                componentKey.contains("sensor");           // security sensors
    }
}