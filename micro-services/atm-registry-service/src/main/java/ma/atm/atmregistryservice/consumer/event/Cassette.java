package ma.atm.atmregistryservice.consumer.event;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class Cassette {
    private Long id; // Auto-generated unique key for the cassette record

    private String cassetteId;


    private AtmCounter atmCounter;

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