package ma.atm.atmmessagesproducer.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

/**
 * Base class for all ATM messages.
 * Contains common fields that are present in all message types.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseAtmMessage {
    
    @JsonProperty("atmId")
    private String atmId;
    
    @JsonProperty("messageType")
    private String messageType;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    /**
     * Get the specific message type enum value.
     * 
     * @return The MessageType enum value
     */
    public MessageType getMessageTypeEnum() {
        return MessageType.fromString(messageType);
    }
}
