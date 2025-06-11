package ma.atm.dataingestionservice.service;

import ma.atm.dataingestionservice.model.TransactionMessage;

/**
 * Service interface for processing ATM transaction messages.
 */
public interface TransactionMessageService {
    
    /**
     * Processes a received transaction message.
     * 
     * @param message The transaction message to process.
     */
    void process(TransactionMessage message);
}
