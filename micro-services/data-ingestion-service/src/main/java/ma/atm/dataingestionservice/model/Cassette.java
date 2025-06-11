package ma.atm.dataingestionservice.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Cassette {

    private Long id; // Auto-generated unique key for the cassette record

    private String cassetteId;


    private Integer denomination;

    private String currency;

    private Integer notesRemaining;

    private String cassetteStatus;

    @JsonProperty("totalAmount")
    private Long totalAmount;

    @JsonProperty("rejectCount")
    private Integer rejectCount;

    @JsonProperty("dispensedSinceRefill")
    private Integer dispensedSinceRefill;
}