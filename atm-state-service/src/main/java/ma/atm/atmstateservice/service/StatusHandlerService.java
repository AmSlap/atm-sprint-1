package ma.atm.atmstateservice.service;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.atm.atmstateservice.event.AtmStatusUpdatedEvent;
import ma.atm.atmstateservice.model.AtmStatus;
import ma.atm.atmstateservice.repository.AtmStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class StatusHandlerService {

    private final AtmStatusRepository atmStatusRepository;

    @Autowired
    public StatusHandlerService(AtmStatusRepository atmStatusRepository) {
        this.atmStatusRepository = atmStatusRepository;
    }

    @Transactional
    public void processStatusUpdate(AtmStatusUpdatedEvent event) {
        log.debug("Processing status update for ATM: {}", event.getAtmId());

        // Find existing or create new status entity
        AtmStatus status = atmStatusRepository.findById(event.getAtmId())
                .orElse(new AtmStatus());

        // Map event data to entity
        status.setAtmId(event.getAtmId());
        status.setOperationalState(event.getOperationalState());
        status.setMaintenanceMode(event.getMaintenanceMode());
        // Convert Instant to OffsetDateTime (assuming UTC)
        status.setLastSuccessfulConnection(event.getLastSuccessfulConnection() != null ? event.getLastSuccessfulConnection().atOffset(ZoneOffset.UTC) : null);
        status.setLastSuccessfulTransaction(event.getLastSuccessfulTransaction() != null ? event.getLastSuccessfulTransaction().atOffset(ZoneOffset.UTC) : null);
        status.setLastUpdateTimestamp(event.getTimestamp() != null ? event.getTimestamp().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));

        // Save the updated entity
        atmStatusRepository.save(status);
        log.info("Successfully processed status update for ATM: {}", event.getAtmId());
    }
}
