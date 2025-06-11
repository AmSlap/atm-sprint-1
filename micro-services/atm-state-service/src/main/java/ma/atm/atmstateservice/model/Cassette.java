package ma.atm.atmstateservice.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@Entity
public class Cassette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-generated unique key for the cassette record

    // Unique identifier coming from the event payload (if present)
    @Column(name = "cassette_id", unique = true)
    private String cassetteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atm_id", nullable = false)
    private AtmCounter atmCounter;

    @Column(name = "denomination")
    private Integer denomination;

    @Column(name = "currency")
    private String currency;

    @Column(name = "notes_remaining")
    private Integer notesRemaining;

    @Column(name = "cassette_status")
    private String cassetteStatus;

    @JsonProperty("totalAmount")
    private Long totalAmount;

    @JsonProperty("rejectCount")
    private Integer rejectCount;

    @JsonProperty("dispensedSinceRefill")
    private Integer dispensedSinceRefill;
}