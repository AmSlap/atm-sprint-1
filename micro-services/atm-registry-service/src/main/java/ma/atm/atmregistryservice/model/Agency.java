package ma.atm.atmregistryservice.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "agency")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

    @Id
    @Column(name = "agency_code", nullable = false, unique = true)
    private String agencyCode; // e.g., "AG001"

    @Column(name = "agency_name", nullable = false)
    private String agencyName;

    @Column(name = "region")
    private String region;

    @Column(name = "address")
    private String address;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    // Relationship: One Agency can have multiple ATMs
    // 'mappedBy = "agency"' refers to the 'agency' field in the AtmInfo entity
    // This side is optional if you don't need to navigate from Agency to ATMs directly,
    // but can be useful.
    @OneToMany(mappedBy = "agency")
    @JsonManagedReference
    private Set<AtmInfo> atms;

    // Add other relevant agency fields as needed
}
