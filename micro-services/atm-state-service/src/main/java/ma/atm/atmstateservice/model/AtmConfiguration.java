package ma.atm.atmstateservice.model;

import com.fasterxml.jackson.databind.JsonNode; // Import Jackson's JsonNode
import io.hypersistence.utils.hibernate.type.json.JsonType; // Import from hibernate-types
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "atm_configuration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmConfiguration {

    @Id
    @Column(name = "atm_id", nullable = false)
    private String atmId;

    // Store detailed peripheral status as JSONB
    // The @Type annotation tells Hibernate how to handle the mapping
    @Type(JsonType.class)
    @Column(name = "peripheral_details", columnDefinition = "jsonb")

    private Map<String, Object> peripheralDetails; // Using Jackson's JsonNode to represent the JSON

    // Optional: You might still want a calculated overall health field
    @Column(name = "overall_health")
    private String overallHealth; // e.g., "GREEN", "ORANGE", "RED"

    @Column(name = "last_update_timestamp", nullable = false)
    private OffsetDateTime lastUpdateTimestamp;
}
