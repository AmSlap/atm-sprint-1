package ma.atm.jbpmincidentservice.model.enums;

public enum TaskStatus {
    CREATED("Created"),
    READY("Ready"),
    RESERVED("Reserved"),
    IN_PROGRESS("In Progress"),
    SUSPENDED("Suspended"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    ERROR("Error"),
    EXITED("Exited"),
    OBSOLETE("Obsolete");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
