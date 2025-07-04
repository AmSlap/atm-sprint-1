package ma.atm.dataingestionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentMessage extends BaseAtmMessage {

    @JsonProperty("errorType")
    private String errorType;

    @JsonProperty("incidentDescription")
    private String incidentDescription;


}
