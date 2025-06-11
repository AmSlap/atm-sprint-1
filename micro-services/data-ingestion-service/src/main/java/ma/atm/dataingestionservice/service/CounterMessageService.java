package ma.atm.dataingestionservice.service;


import ma.atm.dataingestionservice.model.CounterMessage;

/**
 * Service interface for processing ATM counter messages.
 */
public interface CounterMessageService {
    
    /**
     * Processes a received counter message.
     * 
     * @param message The counter message to process.
     */
    void process(CounterMessage message);
}
