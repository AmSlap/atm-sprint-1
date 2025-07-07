package ma.atm.atmstateservice.event;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtmConfigurationChangedEvent {

    // Fields from BaseAtmMessage
    @JsonProperty("atmId")
    private String atmId;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @Column(name = "overall_health")
    private String overallHealth; // e.g., "GREEN", "ORANGE", "RED"

    // Tells Jackson to deserialize the JSON into an ObjectNode (a concrete subclass of JsonNode)
    // Using a Map to store peripherals data
    @JsonProperty("peripherals")
    private Map<String, Object> peripherals;
}