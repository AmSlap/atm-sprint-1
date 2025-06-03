package ma.atm.dataingestionservice.exception;

/**
 * Custom exception for errors occurring during message processing.
 */
public class MessageProcessingException extends Exception {
    
    public MessageProcessingException(String message) {
        super(message);
    }
    
    public MessageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
