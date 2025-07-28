package ma.atm.notificationservice.model.enums;

public enum IncidentType {
    UNDER_MAINTENANCE("Under Maintenance"),
    OUTSIDE_MAINTENANCE_UNDER_INSURANCE("Outside Maintenance - Under Insurance"),
    OUTSIDE_MAINTENANCE_OUTSIDE_INSURANCE("Outside Maintenance - Outside Insurance"),
    NOT_CLASSIFIED("Not Classified");

    private final String displayName;

    IncidentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
