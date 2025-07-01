package ma.atm.dataingestionservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.atm.dataingestionservice.exception.MessageProcessingException;
import ma.atm.dataingestionservice.integration.ConfigurationIntegrationEvent;
import ma.atm.dataingestionservice.model.*;
import ma.atm.dataingestionservice.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation for dispatching incoming ATM messages.
 */
@Service
public class MessageDispatcherServiceImpl implements MessageDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(MessageDispatcherServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final StatusMessageService statusMessageService;
    private final ConfigurationMessageService configurationMessageService;
    private final CounterMessageService counterMessageService;
    private final TransactionMessageService transactionMessageService;

    @Autowired
    public MessageDispatcherServiceImpl(ObjectMapper objectMapper,
                                        StatusMessageService statusMessageService,
                                        ConfigurationMessageService configurationMessageService,
                                        CounterMessageService counterMessageService,
                                        TransactionMessageService transactionMessageService) {
        this.objectMapper = objectMapper;
        this.statusMessageService = statusMessageService;
        this.configurationMessageService = configurationMessageService;
        this.counterMessageService = counterMessageService;
        this.transactionMessageService = transactionMessageService;
    }

    @Override
    public void dispatch(String messagePayload) throws MessageProcessingException {
        log.debug("Received raw message payload: {}", messagePayload);
        try {
            // First, parse into the base class to determine the message type
            BaseAtmMessage baseMessage = objectMapper.readValue(messagePayload, BaseAtmMessage.class);
            MessageType messageType = baseMessage.getMessageTypeEnum();
            String atmId = baseMessage.getAtmId();

            log.info("Dispatching message of type {} for ATM ID: {}", messageType, atmId);

            switch (messageType) {
                case STATUS:
                    StatusMessage statusMessage = objectMapper.readValue(messagePayload, StatusMessage.class);
                    statusMessageService.process(statusMessage);
                    break;
                case CONFIGURATION:
                    ConfigurationMessage configMessage = objectMapper.readValue(messagePayload, ConfigurationMessage.class);
                    configurationMessageService.process(configMessage);
                    break;
                case COUNTER:
                    CounterMessage counterMessage = objectMapper.readValue(messagePayload, CounterMessage.class);
                    counterMessageService.process(counterMessage);
                    break;
                case TRANSACTION:
                    TransactionMessage transactionMessage = objectMapper.readValue(messagePayload, TransactionMessage.class);
                    transactionMessageService.process(transactionMessage);
                    break;

                case INCIDENT:
                    IncidentMessage incidentMessage = objectMapper.readValue(messagePayload, IncidentMessage.class);
                    // Handle incident message processing here
                    log.info("Processing incident message for ATM ID: {}", incidentMessage.getAtmId());
                    // You can add a service to handle incidents if needed
                    break;
                case UNKNOWN:
                default:
                    log.warn("Received message with unknown or missing type for ATM ID: {}. Payload: {}", atmId, messagePayload);
                    // Optionally, send to a specific handler or dead-letter queue
                    // For now, just log it.
                    break;
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message payload: {}", messagePayload, e);
            throw new MessageProcessingException("Error parsing JSON message", e);
        } catch (Exception e) {
            // Catch any other unexpected exceptions during processing
            log.error("Unexpected error processing message payload: {}", messagePayload, e);
            throw new MessageProcessingException("Unexpected error during message processing", e);
        }
    }
}
