package ma.atm.atmstateservice.repository;

import ma.atm.atmstateservice.model.Cassette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CassetteRepository extends JpaRepository<Cassette, Long> {

}
