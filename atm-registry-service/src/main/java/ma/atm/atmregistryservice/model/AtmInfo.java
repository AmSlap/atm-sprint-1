package ma.atm.atmregistryservice.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "atm_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmInfo {

    @Id
    @Column(name = "atm_id", nullable = false, unique = true)
    private String atmId; // GAB Number, e.g., "GAB12345"

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "label")
    private String label; // User-friendly name or identifier

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "region")
    private String region;

    @Column(name = "location_address")
    private String locationAddress;

    @Column(name = "location_latitude")
    private Double locationLatitude;

    @Column(name = "location_longitude")
    private Double locationLongitude;

    // Relationship: Many ATMs can belong to one Agency
    @ManyToOne(fetch = FetchType.LAZY) // LAZY fetch is generally recommended for performance
    @JoinColumn(name = "agency_code_fk", referencedColumnName = "agency_code") // Name of the foreign key column in atm_info table
    private Agency agency;
}