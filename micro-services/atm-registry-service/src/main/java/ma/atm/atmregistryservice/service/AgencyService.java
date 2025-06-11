package ma.atm.atmregistryservice.service;


import ma.atm.atmregistryservice.model.Agency;
import ma.atm.atmregistryservice.repository.AgencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AgencyService {

    private final AgencyRepository agencyRepository;

    @Autowired
    public AgencyService(AgencyRepository agencyRepository) {
        this.agencyRepository = agencyRepository;
    }

    @Transactional(readOnly = true)
    public List<Agency> getAllAgencies() {
        return agencyRepository.findAll();
        // Consider pagination
    }

    @Transactional(readOnly = true)
    public Optional<Agency> getAgencyByCode(String agencyCode) {
        return agencyRepository.findById(agencyCode);
    }

    @Transactional
    public Agency createAgency(Agency agency) {
        if (agencyRepository.existsById(agency.getAgencyCode())) {
            throw new IllegalArgumentException("Agency with code " + agency.getAgencyCode() + " already exists.");
        }
        // Clear the ATMs set if provided, as it should be managed from the AtmInfo side
        agency.setAtms(null);
        return agencyRepository.save(agency);
    }

    @Transactional
    public Optional<Agency> updateAgency(String agencyCode, Agency updatedAgency) {
        return agencyRepository.findById(agencyCode)
                .map(existingAgency -> {
                    existingAgency.setAgencyName(updatedAgency.getAgencyName());
                    existingAgency.setRegion(updatedAgency.getRegion());
                    existingAgency.setAddress(updatedAgency.getAddress());
                    existingAgency.setContactPerson(updatedAgency.getContactPerson());
                    existingAgency.setContactEmail(updatedAgency.getContactEmail());
                    existingAgency.setContactPhone(updatedAgency.getContactPhone());
                    // Do not update the 'atms' collection here
                    return agencyRepository.save(existingAgency);
                });
    }

    @Transactional
    public boolean deleteAgency(String agencyCode) {
        // Consider adding logic to check if any ATMs are still assigned to this agency
        // before allowing deletion, or handle reassignment/unassignment.
        if (agencyRepository.existsById(agencyCode)) {
            agencyRepository.deleteById(agencyCode);
            return true;
        }
        return false;
    }
}