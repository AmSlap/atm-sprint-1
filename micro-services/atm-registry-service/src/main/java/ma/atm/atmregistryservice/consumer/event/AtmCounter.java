package ma.atm.atmregistryservice.consumer.event;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;


@Table(name = "atm_counter_summary") // Renamed for clarity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmCounter {

    private String atmId;

    private Double totalCashAvailable;

    private Boolean lowCashFlag;

    private Integer rejectBinPercentageFull;

    private List<Cassette> cassettes;

    private OffsetDateTime lastUpdateTimestamp;
}