package ma.atm.notificationservice.model.enums;

public enum IncidentStatus {
    CREATED("Created"),
    IN_PROGRESS("In Progress"),
    WAITING_FOR_ASSESSMENT("Waiting for Assessment"),
    WAITING_FOR_INSURANCE("Waiting for Insurance"),
    WAITING_FOR_PROCUREMENT("Waiting for Procurement"),
    WAITING_FOR_RESOLUTION("Waiting for Resolution"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    ABORTED("Aborted");

    private final String displayName;

    IncidentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
