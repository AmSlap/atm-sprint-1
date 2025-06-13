package ma.atm.atmstateservice.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public class AtmCombinedDto {
    // From AtmStateSummaryDto
    private String atmId;
    private String operationalState;
    private String overallHealth;
    private Boolean lowCashFlag;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime lastUpdateTimestamp;

    // From AtmRegistryDto
    private String label;
    private String brand;
    private String model;
    private String region;
    private String agencyCode;
    private String agencyName;
    private String locationAddress;
    private Double locationLatitude;
    private Double locationLongitude;
    private String ipAddress;

    // Default constructor
    public AtmCombinedDto() {}

    // Full constructor
    public AtmCombinedDto(String atmId, String operationalState, String overallHealth,
                          Boolean lowCashFlag, OffsetDateTime lastUpdateTimestamp,
                          String label, String brand, String model, String region,
                          String agencyCode, String agencyName, String locationAddress,
                          Double locationLatitude, Double locationLongitude, String ipAddress) {
        this.atmId = atmId;
        this.operationalState = operationalState;
        this.overallHealth = overallHealth;
        this.lowCashFlag = lowCashFlag;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.label = label;
        this.brand = brand;
        this.model = model;
        this.region = region;
        this.agencyCode = agencyCode;
        this.agencyName = agencyName;
        this.locationAddress = locationAddress;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    public String getAtmId() {
        return atmId;
    }

    public void setAtmId(String atmId) {
        this.atmId = atmId;
    }

    public String getOperationalState() {
        return operationalState;
    }

    public void setOperationalState(String operationalState) {
        this.operationalState = operationalState;
    }

    public String getOverallHealth() {
        return overallHealth;
    }

    public void setOverallHealth(String overallHealth) {
        this.overallHealth = overallHealth;
    }

    public Boolean getLowCashFlag() {
        return lowCashFlag;
    }

    public void setLowCashFlag(Boolean lowCashFlag) {
        this.lowCashFlag = lowCashFlag;
    }

    public OffsetDateTime getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(OffsetDateTime lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAgencyCode() {
        return agencyCode;
    }

    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "AtmCombinedDto{" +
                "atmId='" + atmId + '\'' +
                ", operationalState='" + operationalState + '\'' +
                ", overallHealth='" + overallHealth + '\'' +
                ", lowCashFlag=" + lowCashFlag +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", label='" + label + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", region='" + region + '\'' +
                ", agencyCode='" + agencyCode + '\'' +
                ", agencyName='" + agencyName + '\'' +
                ", locationAddress='" + locationAddress + '\'' +
                ", locationLatitude=" + locationLatitude +
                ", locationLongitude=" + locationLongitude +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}