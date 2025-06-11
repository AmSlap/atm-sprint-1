package ma.atm.dataingestionservice.service.impl;


import lombok.extern.slf4j.Slf4j;
import ma.atm.dataingestionservice.model.TransactionMessage;
import ma.atm.dataingestionservice.pulsar.producer.ProcessedMessagePublisher;
import ma.atm.dataingestionservice.service.TransactionMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation for processing ATM transaction messages.
 */
@Service
@Slf4j
public class TransactionMessageServiceImpl implements TransactionMessageService {

    @Autowired
    private ProcessedMessagePublisher processedMessagePublisher;

    @Override
    public void process(TransactionMessage message) {
        log.info("Processing Transaction Message for ATM ID: {}", message.getAtmId());
        log.debug("Transaction Message Details: {}", message);

        if (message.getAtmId() == null || message.getAtmId().isEmpty()) {
            log.error("Invalid Transaction Message: missing ATM ID.");
            throw new IllegalArgumentException("ATM ID is required.");
        }
        if (message.getTransactionId() == null || message.getTransactionId().isEmpty()) {
            log.error("Invalid Transaction Message: missing transaction ID for ATM {}.", message.getAtmId());
            throw new IllegalArgumentException("Transaction ID is required.");
        }

        processedMessagePublisher.publishTransactionEvent(message);
    }
}
