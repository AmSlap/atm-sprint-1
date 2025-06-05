package ma.atm.atmregistryservice.dto;

import lombok.Data;

@Data
public class AtmRequest {
    // Include all fields from AtmInfo that can be set/updated via API
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

    // Add the agencyCode
    private String agencyCode;
}