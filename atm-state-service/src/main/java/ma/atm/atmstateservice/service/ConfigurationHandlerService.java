package ma.atm.atmstateservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import ma.atm.atmstateservice.event.AtmConfigurationChangedEvent;
import ma.atm.atmstateservice.model.AtmConfiguration;
import ma.atm.atmstateservice.repository.AtmConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class ConfigurationHandlerService {

    private final AtmConfigurationRepository atmConfigurationRepository;

    @Autowired
    public ConfigurationHandlerService(AtmConfigurationRepository atmConfigurationRepository) {
        this.atmConfigurationRepository = atmConfigurationRepository;
    }

    @Transactional
    public void processConfigurationChange(AtmConfigurationChangedEvent event) {
        log.debug("Processing configuration change for ATM: {}", event.getAtmId());

        // Retrieve existing configuration, or create a new one if not found.
        AtmConfiguration configuration = atmConfigurationRepository.findById(event.getAtmId())
                .orElse(new AtmConfiguration());
        configuration.setAtmId(event.getAtmId());

        // Get the new peripherals configuration from the event.
        JsonNode newPeripheralDetails = event.getPeripherals();

        // Compare the new configuration with the stored one using deep JSON comparison.
        if (configuration.getPeripheralDetails() != null &&
                configuration.getPeripheralDetails().equals(newPeripheralDetails)) {
            log.info("No change detected in configuration for ATM: {}", event.getAtmId());
            return;
        }

        // Update configuration as a significant (detected) change.
        configuration.setPeripheralDetails(newPeripheralDetails);

        // Calculate overall health based on the JsonNode.
        if (newPeripheralDetails != null) {
            configuration.setOverallHealth(calculateOverallHealth(newPeripheralDetails));
        } else {
            configuration.setOverallHealth("UNKNOWN"); // Or some default value.
        }

        configuration.setLastUpdateTimestamp(event.getTimestamp() != null ?
                event.getTimestamp().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));

        atmConfigurationRepository.save(configuration);
        log.info("Successfully processed and updated configuration change for ATM: {}", event.getAtmId());
    }

    // Placeholder method - Implement your logic to determine overall health.
    private String calculateOverallHealth(JsonNode peripheralDetails) {
        boolean hasCritical = false;
        boolean hasWarning = false;

        if (peripheralDetails != null && peripheralDetails.isObject()) {
            var iterator = peripheralDetails.fields();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                JsonNode peripheral = entry.getValue();
                if (peripheral.has("status")) {
                    String status = peripheral.get("status").asText("").toUpperCase();
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

        if (hasCritical) {
            return "RED";
        }
        if (hasWarning) {
            return "ORANGE";
        }
        return "GREEN";
    }
}