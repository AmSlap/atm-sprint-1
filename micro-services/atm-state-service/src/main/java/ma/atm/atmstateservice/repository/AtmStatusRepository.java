package ma.atm.atmstateservice.repository;

import ma.atm.atmstateservice.model.AtmStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtmStatusRepository extends JpaRepository<AtmStatus, String> {

}
