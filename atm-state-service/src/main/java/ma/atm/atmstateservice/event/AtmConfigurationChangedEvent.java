package ma.atm.atmstateservice.event;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

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

    // Field specific to ConfigurationMessage
    @JsonProperty("peripherals")
    private JsonNode peripherals;


}
