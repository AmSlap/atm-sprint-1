package ma.atm.dataingestionservice.service;

import ma.atm.dataingestionservice.exception.MessageProcessingException;
import ma.atm.dataingestionservice.model.IncidentMessage;

public interface IncidentMessageService {
    void process(IncidentMessage message) throws MessageProcessingException;
}
