package ma.atm.dataingestionservice.service;

import ma.atm.dataingestionservice.exception.MessageProcessingException;

/**
 * Service interface for dispatching incoming ATM messages to the appropriate processor.
 */
public interface MessageDispatcherService {
    
    /**
     * Parses and dispatches a raw message payload (e.g., JSON string) to the correct service.
     * 
     * @param messagePayload The raw message content received from Pulsar.
     * @throws MessageProcessingException if parsing or processing fails.
     */
    void dispatch(String messagePayload) throws MessageProcessingException;
}
