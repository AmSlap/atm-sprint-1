package ma.atm.atmregistryservice.consumer.event;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtmCountersUpdatedEvent {

    // Fields from BaseAtmMessage
    @JsonProperty("atmId")
    private String atmId;

    @JsonProperty("timestamp")
    private Instant timestamp;

    // Fields specific to CounterMessage
    @JsonProperty("cassettes")
    private List<Cassette> cassettes;

    @JsonProperty("rejectBin")
    private RejectBinInfo rejectBin;

    @JsonProperty("totalCashAvailable")
    private Long totalCashAvailable;

    @JsonProperty("lastDispenseAmount")
    private Long lastDispenseAmount;

    @JsonProperty("lastDispenseTime")
    private String lastDispenseTime; // Keep as String if that's what ingestion sends

    @JsonProperty("dispensedToday")
    private Long dispensedToday;

    // --- Nested Classes matching CounterMessage inner classes ---



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RejectBinInfo { // Matches CounterMessage.RejectBin
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
