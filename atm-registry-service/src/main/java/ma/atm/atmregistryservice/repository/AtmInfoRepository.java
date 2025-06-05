package ma.atm.atmregistryservice.repository;


import ma.atm.atmregistryservice.model.AtmInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtmInfoRepository extends JpaRepository<AtmInfo,String> {

    /**
     * Finds an ATM by its serial number.
     *
     * @param serialNumber the serial number of the ATM
     * @return the ATM information if found, otherwise null
     */
    AtmInfo findBySerialNumber(String serialNumber);
}
