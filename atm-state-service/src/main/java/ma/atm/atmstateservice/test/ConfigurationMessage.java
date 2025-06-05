package ma.atm.atmstateservice.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a configuration message from an ATM.
 * Contains information about the status of hardware peripherals.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationMessage extends BaseAtmMessage {
    
    // Using a Map for flexibility, assuming peripherals data is a nested JSON object
    // Key: Peripheral name (e.g., "cardReader", "receiptPrinter")
    // Value: Another Map or a specific object representing the peripheral's status details
    @JsonProperty("peripherals")
    private JsonNode peripherals;
}
