package ma.atm.atmmessagesproducer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Represents a status message from an ATM.
 * Contains information about the operational state of the ATM.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusMessage extends BaseAtmMessage {
    
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
