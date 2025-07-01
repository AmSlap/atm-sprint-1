package ma.atm.dataingestionservice.service.impl;

import ma.atm.dataingestionservice.exception.MessageProcessingException;
import ma.atm.dataingestionservice.model.IncidentMessage;
import ma.atm.dataingestionservice.pulsar.producer.ProcessedMessagePublisher;
import ma.atm.dataingestionservice.service.IncidentMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncidentMessageServiceImpl implements IncidentMessageService {
    @Autowired
    private ProcessedMessagePublisher processedMessagePublisher;



    @Override
    public void process(IncidentMessage message) throws MessageProcessingException {
        if (message == null) {
            throw new MessageProcessingException("Incident message cannot be null");
        }

        if (message.getAtmId() == null || message.getAtmId().isEmpty()) {
            throw new MessageProcessingException("ATM ID is required in the incident message");
        }



        // Log the incident message details
        System.out.println("Processing Incident Message for ATM ID: " + message.getAtmId());

        // Publish to the internal Pulsar topic
        processedMessagePublisher.publishIncidentEvent(message);

    }
}
