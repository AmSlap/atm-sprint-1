package ma.atm.dataingestionservice.model;
;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents a counter message from an ATM.
 * Contains information about cash levels and cassette status.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CounterMessage extends BaseAtmMessage {
    
    @JsonProperty("cassettes")
    private List<Cassette> cassettes;
    
    @JsonProperty("rejectBin")
    private RejectBin rejectBin;
    
    @JsonProperty("totalCashAvailable")
    private Long totalCashAvailable;
    
    @JsonProperty("lastDispenseAmount")
    private Long lastDispenseAmount;
    
    @JsonProperty("lastDispenseTime")
    private String lastDispenseTime;
    
    @JsonProperty("dispensedToday")
    private Long dispensedToday;
    
    /**
     * Represents a single cassette in the ATM.
     */
    /**
     * Represents the reject bin in the ATM.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RejectBin {
        @JsonProperty("capacity")
        private Integer capacity;
        
        @JsonProperty("notesStored")
        private Integer notesStored;
        
        @JsonProperty("percentageFull")
        private Integer percentageFull;
        
        @JsonProperty("status")
        private String status;
    }
}
