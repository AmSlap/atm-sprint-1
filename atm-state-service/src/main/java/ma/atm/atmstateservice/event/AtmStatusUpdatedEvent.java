package ma.atm.atmstateservice.event;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Good practice
public class AtmStatusUpdatedEvent {

    // Fields from BaseAtmMessage
    @JsonProperty("atmId")
    private String atmId;
    @JsonProperty("timestamp")
    private Instant timestamp; // Note: Changed from OffsetDateTime to Instant

    // Fields specific to StatusMessage
    @JsonProperty("operationalState")
    private String operationalState;

    @JsonProperty("previousState")
    private String previousState;

    @JsonProperty("stateChangeReason")
    private String stateChangeReason;

    @JsonProperty("lastSuccessfulConnection")
    private Instant lastSuccessfulConnection;

    @JsonProperty("lastSuccessfulTransaction")
    private Instant lastSuccessfulTransaction;

    @JsonProperty("maintenanceMode")
    private Boolean maintenanceMode;

    @JsonProperty("softwareVersion")
    private String softwareVersion;

    @JsonProperty("uptimeSeconds")
    private Long uptimeSeconds;
}