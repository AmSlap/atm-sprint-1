package ma.atm.atmstateservice.dto;


public class AtmEnhancedDetailDto {
    private String atmId;
    private AtmStatusDto status;
    private AtmConfigurationDto configuration;
    private AtmCounterDto counters;
    private AtmRegistryDto registryInfo; // Additional registry information

    // Default constructor
    public AtmEnhancedDetailDto() {}

    // Full constructor
    public AtmEnhancedDetailDto(String atmId, AtmStatusDto status,
                                AtmConfigurationDto configuration, AtmCounterDto counters,
                                AtmRegistryDto registryInfo) {
        this.atmId = atmId;
        this.status = status;
        this.configuration = configuration;
        this.counters = counters;
        this.registryInfo = registryInfo;
    }

    // Getters and Setters
    public String getAtmId() {
        return atmId;
    }

    public void setAtmId(String atmId) {
        this.atmId = atmId;
    }

    public AtmStatusDto getStatus() {
        return status;
    }

    public void setStatus(AtmStatusDto status) {
        this.status = status;
    }

    public AtmConfigurationDto getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AtmConfigurationDto configuration) {
        this.configuration = configuration;
    }

    public AtmCounterDto getCounters() {
        return counters;
    }

    public void setCounters(AtmCounterDto counters) {
        this.counters = counters;
    }

    public AtmRegistryDto getRegistryInfo() {
        return registryInfo;
    }

    public void setRegistryInfo(AtmRegistryDto registryInfo) {
        this.registryInfo = registryInfo;
    }

    @Override
    public String toString() {
        return "AtmEnhancedDetailDto{" +
                "atmId='" + atmId + '\'' +
                ", status=" + status +
                ", configuration=" + configuration +
                ", counters=" + counters +
                ", registryInfo=" + registryInfo +
                '}';
    }
}