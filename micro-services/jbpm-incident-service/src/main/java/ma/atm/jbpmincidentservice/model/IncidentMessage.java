package ma.atm.jbpmincidentservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentMessage {
    // Fields from BaseAtmMessage
    @JsonProperty("atmId")
    private String atmId;

    @JsonProperty("timestamp")
    private Instant timestamp;



    @JsonProperty("errorType")
    private String errorType;

    @JsonProperty("incidentDescription")
    private String incidentDescription;

}
