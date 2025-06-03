package ma.atm.dataingestionservice.service;

import ma.atm.dataingestionservice.exception.MessageProcessingException;
import ma.atm.dataingestionservice.model.ConfigurationMessage;

/**
 * Service interface for processing ATM configuration messages.
 */
public interface ConfigurationMessageService {
    
    /**
     * Processes a received configuration message.
     * 
     * @param message The configuration message to process.
     */
    void process(ConfigurationMessage message) throws MessageProcessingException;
}
