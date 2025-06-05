package ma.atm.atmstateservice.service;


import ma.atm.atmstateservice.event.AtmCountersUpdatedEvent;
import ma.atm.atmstateservice.model.AtmCounter;
import ma.atm.atmstateservice.model.Cassette;
import ma.atm.atmstateservice.repository.AtmCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;




@Service
public class CounterHandlerService {

    private static final Logger log = LoggerFactory.getLogger(CounterHandlerService.class);
    private final AtmCounterRepository atmCounterRepository;

    public CounterHandlerService(AtmCounterRepository atmCounterRepository) {
        this.atmCounterRepository = atmCounterRepository;
    }

    @Transactional
    public void processCounterUpdate(AtmCountersUpdatedEvent event) {
        log.debug("Processing counter update for ATM: {}", event.getAtmId());

        AtmCounter counterSummary = atmCounterRepository.findById(event.getAtmId())
                .orElse(new AtmCounter());

        counterSummary.setAtmId(event.getAtmId());
        counterSummary.setTotalCashAvailable(event.getTotalCashAvailable() != null ? event.getTotalCashAvailable().doubleValue() : null);
        counterSummary.setRejectBinPercentageFull(event.getRejectBin() != null ? event.getRejectBin().getPercentageFull() : null);
        counterSummary.setLastUpdateTimestamp(event.getTimestamp() != null ? event.getTimestamp().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));

        // Update Cassette details using unique cassetteId
        List<Cassette> updatedCassettes = new ArrayList<>();
        boolean isLowCash = false;
        if (event.getCassettes() != null) {
            for (Cassette info : event.getCassettes()) {
                Cassette cassette = findOrCreateCassette(counterSummary, info.getCassetteId());
                cassette.setAtmCounter(counterSummary);
                cassette.setCassetteId(info.getCassetteId()); // Unique identifier
                cassette.setDenomination(info.getDenomination());
                cassette.setCurrency(info.getCurrency());
                cassette.setNotesRemaining(info.getNotesRemaining());
                cassette.setCassetteStatus(info.getCassetteStatus());
                cassette.setTotalAmount(info.getTotalAmount());
                cassette.setRejectCount(info.getRejectCount());
                cassette.setDispensedSinceRefill(info.getDispensedSinceRefill());
                updatedCassettes.add(cassette);

                // Low Cash Logic
                if ("OK".equalsIgnoreCase(info.getCassetteStatus()) && info.getNotesRemaining() != null && info.getNotesRemaining() < 200) {
                    isLowCash = true;
                }
                if ("LOW".equalsIgnoreCase(info.getCassetteStatus()) || "EMPTY".equalsIgnoreCase(info.getCassetteStatus())) {
                    isLowCash = true;
                }
            }
        }

        // Replace cassette list
        if(counterSummary.getCassettes() == null) {
            counterSummary.setCassettes(new ArrayList<>());
        }
        counterSummary.getCassettes().clear();
        counterSummary.getCassettes().addAll(updatedCassettes);
        counterSummary.setLowCashFlag(isLowCash);

        atmCounterRepository.save(counterSummary);
        log.info("Successfully processed counter update for ATM: {}", event.getAtmId());
        log.info(String.valueOf(event));
    }

    // New helper method that looks up by cassetteId in the counterSummary
    private Cassette findOrCreateCassette(AtmCounter counterSummary, String cassetteId) {
        if (counterSummary.getCassettes() != null && cassetteId != null) {
            Optional<Cassette> existing = counterSummary.getCassettes().stream()
                    .filter(c -> cassetteId.equals(c.getCassetteId()))
                    .findFirst();
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return new Cassette();
    }
}