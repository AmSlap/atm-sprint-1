package ma.atm.atmstateservice.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "atm_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmStatus {

    @Id
    @Column(name = "atm_id", nullable = false)
    private String atmId; // Foreign key referencing AtmInfo, but primary key here

    @Column(name = "operational_state")
    private String operationalState; // e.g., "IN_SERVICE", "OFFLINE", "OUT_OF_SERVICE"

    @Column(name = "maintenance_mode")
    private Boolean maintenanceMode;

    @Column(name = "last_successful_connection")
    private OffsetDateTime lastSuccessfulConnection;

    @Column(name = "last_successful_transaction")
    private OffsetDateTime lastSuccessfulTransaction;

    @Column(name = "last_update_timestamp", nullable = false)
    private OffsetDateTime lastUpdateTimestamp; // When this record was last updated

    // We don't store a direct relationship to AtmInfo here to keep services decoupled.
    // The atmId is the link.
}
