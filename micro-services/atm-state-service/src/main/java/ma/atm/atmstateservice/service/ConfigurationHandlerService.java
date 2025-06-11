package ma.atm.atmstateservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.atm.atmstateservice.event.AtmConfigurationChangedEvent;
import ma.atm.atmstateservice.model.AtmConfiguration;
import ma.atm.atmstateservice.repository.AtmConfigurationRepository;
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

    @Autowired
    public ConfigurationHandlerService(AtmConfigurationRepository atmConfigurationRepository,
                                       ObjectMapper objectMapper) {
        this.atmConfigurationRepository = atmConfigurationRepository;
        this.objectMapper = objectMapper;
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
            configuration.setOverallHealth(calculateOverallHealth(newPeripheralDetails));
        } else {
            configuration.setOverallHealth("UNKNOWN"); // Or some default value.
        }

        configuration.setLastUpdateTimestamp(event.getTimestamp()!= null ?
                event.getTimestamp().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));

        atmConfigurationRepository.save(configuration);
        log.info("Successfully processed and updated configuration change for ATM: {}", event.getAtmId());
    }

    // Calculate overall health from the peripheral details map.
    private String calculateOverallHealth(Map<String, Object> peripheralDetails) {
        boolean hasCritical = false;
        boolean hasWarning = false;

        if (peripheralDetails != null) {
            for (Map.Entry<String, Object> entry : peripheralDetails.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Map) {
                    Map<?, ?> peripheral = (Map<?, ?>) value;
                    Object statusObj = peripheral.get("status");
                    if (statusObj != null) {
                        String status = statusObj.toString().toUpperCase();
                        if (status.contains("CRITICAL") || status.contains("DOWN") || status.contains("ERROR")) {
                            hasCritical = true;
                            break; // Critical takes precedence.
                        }
                        if (status.contains("WARNING") || status.contains("LOW")) {
                            hasWarning = true;
                        }
                    }
                }
            }
        }

        if (hasCritical) {
            return "RED";
        }
        if (hasWarning) {
            return "ORANGE";
        }
        return "GREEN";
    }
}