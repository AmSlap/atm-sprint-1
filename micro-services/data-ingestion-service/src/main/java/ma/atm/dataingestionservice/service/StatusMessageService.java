package ma.atm.dataingestionservice.service;

import ma.atm.dataingestionservice.model.StatusMessage;

/**
 * Service interface for processing ATM status messages.
 */
public interface StatusMessageService {
    
    /**
     * Processes a received status message.
     * 
     * @param message The status message to process.
     */
    void process(StatusMessage message);
}
