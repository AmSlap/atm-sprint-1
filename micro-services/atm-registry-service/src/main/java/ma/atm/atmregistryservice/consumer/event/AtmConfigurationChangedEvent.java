package ma.atm.atmregistryservice.consumer.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Tells Jackson to deserialize the JSON into an ObjectNode (a concrete subclass of JsonNode)
    // Using a Map to store peripherals data
    @JsonProperty("peripherals")
    private Map<String, Object> peripherals;
}