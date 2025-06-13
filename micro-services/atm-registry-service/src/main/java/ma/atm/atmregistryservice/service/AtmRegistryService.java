package ma.atm.atmregistryservice.service;


import jakarta.persistence.EntityNotFoundException;
import ma.atm.atmregistryservice.model.Agency;
import ma.atm.atmregistryservice.model.AtmInfo;
import ma.atm.atmregistryservice.repository.AgencyRepository;
import ma.atm.atmregistryservice.repository.AtmInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AtmRegistryService {

    private final AtmInfoRepository atmInfoRepository;
    private final AgencyRepository agencyRepository; // Inject AgencyRepository

    @Autowired
    public AtmRegistryService(AtmInfoRepository atmInfoRepository, AgencyRepository agencyRepository) {
        this.atmInfoRepository = atmInfoRepository;
        this.agencyRepository = agencyRepository; // Initialize
    }

    @Transactional(readOnly = true)
    public List<AtmInfo> getAllAtms() {
        return atmInfoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<AtmInfo> getAtmById(String atmId) {
        return atmInfoRepository.findById(atmId);
    }

    // Helper method to find agency or throw exception
    private Agency findAgencyByCode(String agencyCode) {
        return agencyRepository.findById(agencyCode)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found with code: " + agencyCode));
    }

    // Modified createAtm to accept agencyCode and link the relationship
    @Transactional
    public AtmInfo createAtm(AtmInfo atmInfo, String agencyCode) {
        if (atmInfoRepository.existsById(atmInfo.getAtmId())) {
            throw new IllegalArgumentException("ATM with ID " + atmInfo.getAtmId() + " already exists.");
        }
        //Agency agency = findAgencyByCode(agencyCode);
        //atmInfo.setAgency(agency); // Set the relationship
        return atmInfoRepository.save(atmInfo);
    }

    // Modified updateAtm to handle agency change
    @Transactional
    public Optional<AtmInfo> updateAtm(String atmId, AtmInfo updatedAtmInfo, String agencyCode) {
        return atmInfoRepository.findById(atmId)
                .map(existingAtm -> {
                    Agency agency = findAgencyByCode(agencyCode);
                    existingAtm.setAgency(agency); // Update relationship

                    existingAtm.setSerialNumber(updatedAtmInfo.getSerialNumber());
                    existingAtm.setBrand(updatedAtmInfo.getBrand());
                    existingAtm.setModel(updatedAtmInfo.getModel());
                    existingAtm.setLabel(updatedAtmInfo.getLabel());
                    existingAtm.setIpAddress(updatedAtmInfo.getIpAddress());
                    existingAtm.setRegion(updatedAtmInfo.getRegion());
                    existingAtm.setLocationAddress(updatedAtmInfo.getLocationAddress());
                    existingAtm.setLocationLatitude(updatedAtmInfo.getLocationLatitude());
                    existingAtm.setLocationLongitude(updatedAtmInfo.getLocationLongitude());

                    return atmInfoRepository.save(existingAtm);
                });
    }

    @Transactional
    public boolean deleteAtm(String atmId) {
        if (atmInfoRepository.existsById(atmId)) {
            atmInfoRepository.deleteById(atmId);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean atmExists(String atmId) {
        return atmInfoRepository.existsById(atmId);
    }
}
