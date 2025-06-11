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
import java.util.Objects;

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

        // Find existing status or create a new one if not found.
        AtmStatus status = atmStatusRepository.findById(event.getAtmId()).orElse(new AtmStatus());

        boolean changed = false;

        // Update atmId if it's a new record
        if (status.getAtmId() == null) {
            status.setAtmId(event.getAtmId());
            changed = true;
        }

        // Only update operationalState if it is different from the existing value.
        if (!Objects.equals(status.getOperationalState(), event.getOperationalState())) {
            status.setOperationalState(event.getOperationalState());
            changed = true;
        }

        // Only update maintenanceMode if it is different.
        if (!Objects.equals(status.getMaintenanceMode(), event.getMaintenanceMode())) {
            status.setMaintenanceMode(event.getMaintenanceMode());
            changed = true;
        }

        // Only update lastSuccessfulConnection if it is different.
        if (event.getLastSuccessfulConnection() != null) {
            OffsetDateTime newLastSuccessfulConnection = event.getLastSuccessfulConnection().atOffset(ZoneOffset.UTC);
            if (!Objects.equals(status.getLastSuccessfulConnection(), newLastSuccessfulConnection)) {
                status.setLastSuccessfulConnection(newLastSuccessfulConnection);
                changed = true;
            }
        }

        // Only update lastSuccessfulTransaction if it is different.
        if (event.getLastSuccessfulTransaction() != null) {
            OffsetDateTime newLastSuccessfulTransaction = event.getLastSuccessfulTransaction().atOffset(ZoneOffset.UTC);
            if (!Objects.equals(status.getLastSuccessfulTransaction(), newLastSuccessfulTransaction)) {
                status.setLastSuccessfulTransaction(newLastSuccessfulTransaction);
                changed = true;
            }
        }

        // Always update lastUpdateTimestamp from event, to record the time when this event was processed.
        status.setLastUpdateTimestamp(
                (event.getTimestamp() != null)
                        ? event.getTimestamp().atOffset(ZoneOffset.UTC)
                        : OffsetDateTime.now(ZoneOffset.UTC)
        );

        // If any significant field changed OR the status entity is new, then save the updated entity.
        if (changed || status.getAtmId() == null) {
            atmStatusRepository.save(status);
            log.info("Successfully processed status update for ATM: {}", event.getAtmId());
        } else {
            log.info("No meaningful change detected for ATM: {}. Skipping update.", event.getAtmId());
        }
    }
}