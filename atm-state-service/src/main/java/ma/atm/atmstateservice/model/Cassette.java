package ma.atm.atmstateservice.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "cassette")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cassette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Internal primary key for the cassette record

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atm_id", nullable = false) // Link back to the AtmCounter summary
    private AtmCounter atmCounter;

    @Column(name = "denomination")
    private Integer denomination;

    @Column(name = "currency")
    private String currency;

    @Column(name = "notes_remaining")
    private Integer notesRemaining;

    @Column(name = "cassette_status")
    private String cassetteStatus; // e.g., "OK", "LOW", "EMPTY", "PROBLEM", "NOT_CONFIGURED"


    @JsonProperty("totalAmount")
    private Long totalAmount;

    @JsonProperty("rejectCount")
    private Integer rejectCount;

    @JsonProperty("dispensedSinceRefill")
    private Integer dispensedSinceRefill;

}