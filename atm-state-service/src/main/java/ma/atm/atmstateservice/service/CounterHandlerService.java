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

    @Autowired
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

        // Update Cassette details
        List<Cassette> updatedCassettes = new ArrayList<>();
        boolean isLowCash = false;
        if (event.getCassettes() != null) {
            for (Cassette info : event.getCassettes()) {
                Cassette cassette = findOrCreateCassette(counterSummary);
                cassette.setAtmCounter(counterSummary); // Ensure back-reference is set
                cassette.setDenomination(info.getDenomination());
                cassette.setCurrency(info.getCurrency());
                cassette.setNotesRemaining(info.getNotesRemaining());
                cassette.setCassetteStatus(info.getCassetteStatus());
                updatedCassettes.add(cassette);

                // Example Low Cash Logic (adjust threshold as needed)
                if ("OK".equalsIgnoreCase(info.getCassetteStatus()) && info.getNotesRemaining() != null && info.getNotesRemaining() < 200) {
                    isLowCash = true;
                }
                if ("LOW".equalsIgnoreCase(info.getCassetteStatus()) || "EMPTY".equalsIgnoreCase(info.getCassetteStatus())) {
                    isLowCash = true;
                }
            }
        }
        // Manage the collection: replace old list with new one
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

    // Helper to find existing cassette in the list or create a new one
    private Cassette findOrCreateCassette(AtmCounter counterSummary) {
        if (counterSummary.getCassettes() != null) {
            Optional<Cassette> existing = counterSummary.getCassettes().stream()
                    .findFirst();
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        // Not found or list is null, create new
        Cassette newCassette = new Cassette();
        return newCassette;
    }
}
