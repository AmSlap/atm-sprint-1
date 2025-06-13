package ma.atm.atmstateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtmInfoDto {
    private String atmId;
    private String serialNumber;
    private String brand;
    private String model;
    private String label;
    private String ipAddress;
    private String region;
    private String locationAddress;
    private Double locationLatitude;
    private Double locationLongitude;

    // Include agency code as a string rather than a nested object
    private String agencyCode;

    // Optionally include agency name for convenience
    private String agencyName;
}
