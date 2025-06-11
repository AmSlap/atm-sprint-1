package ma.atm.atmregistryservice.repository;


import ma.atm.atmregistryservice.model.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AgencyRepository extends JpaRepository<Agency, String> {
    List<Agency> findByRegion(String region);
}

