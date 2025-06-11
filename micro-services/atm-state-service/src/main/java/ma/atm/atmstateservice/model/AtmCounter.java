package ma.atm.atmstateservice.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "atm_counter_summary") // Renamed for clarity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmCounter {

    @Id
    @Column(name = "atm_id", nullable = false)
    private String atmId;

    @Column(name = "total_cash_available")
    private Double totalCashAvailable;

    @Column(name = "low_cash_flag")
    private Boolean lowCashFlag;

    @Column(name = "reject_bin_percentage_full")
    private Integer rejectBinPercentageFull;

    // One ATM Counter summary has many Cassette details
    @OneToMany(mappedBy = "atmCounter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Cassette> cassettes;

    @Column(name = "last_update_timestamp", nullable = false)
    private OffsetDateTime lastUpdateTimestamp;
}