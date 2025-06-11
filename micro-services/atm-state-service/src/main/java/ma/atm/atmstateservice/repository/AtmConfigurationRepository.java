package ma.atm.atmstateservice.repository;


import ma.atm.atmstateservice.model.AtmConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtmConfigurationRepository extends JpaRepository<AtmConfiguration, String> {
}
