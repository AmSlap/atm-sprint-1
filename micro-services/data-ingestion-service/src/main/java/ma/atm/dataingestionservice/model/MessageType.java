package ma.atm.dataingestionservice.model;

/**
 * Enum representing the different types of messages that can be received from ATMs.
 */
public enum MessageType {
    STATUS,
    CONFIGURATION,
    COUNTER,
    TRANSACTION,
    INCIDENT,
    UNKNOWN;
    
    /**
     * Convert a string to a MessageType enum value.
     * 
     * @param type The string representation of the message type
     * @return The corresponding MessageType enum value, or UNKNOWN if not recognized
     */
    public static MessageType fromString(String type) {
        if (type == null) {
            return UNKNOWN;
        }
        
        try {
            return MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
