package ma.atm.atmstateservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;



public class AtmRegistryDto {
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
    private String agencyCode;
    private String agencyName; // populated from join

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime lastModified;

    // Default constructor
    public AtmRegistryDto() {}

    // Full constructor
    public AtmRegistryDto(String atmId, String serialNumber, String brand, String model,
                          String label, String ipAddress, String region, String locationAddress,
                          Double locationLatitude, Double locationLongitude, String agencyCode,
                          String agencyName, OffsetDateTime createdDate, OffsetDateTime lastModified) {
        this.atmId = atmId;
        this.serialNumber = serialNumber;
        this.brand = brand;
        this.model = model;
        this.label = label;
        this.ipAddress = ipAddress;
        this.region = region;
        this.locationAddress = locationAddress;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.agencyCode = agencyCode;
        this.agencyName = agencyName;
        this.createdDate = createdDate;
        this.lastModified = lastModified;
    }

    // Getters and Setters
    public String getAtmId() {
        return atmId;
    }

    public void setAtmId(String atmId) {
        this.atmId = atmId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "AtmRegistryDto{" +
                "atmId='" + atmId + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", label='" + label + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", region='" + region + '\'' +
                ", locationAddress='" + locationAddress + '\'' +
                ", locationLatitude=" + locationLatitude +
                ", locationLongitude=" + locationLongitude +
                ", agencyCode='" + agencyCode + '\'' +
                ", agencyName='" + agencyName + '\'' +
                ", createdDate=" + createdDate +
                ", lastModified=" + lastModified +
                '}';
    }
}
