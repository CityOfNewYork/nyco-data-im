package gov.nyc.nyco.intellimatch.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gov.nyc.nyco.intellimatch.models.IMAgencyCode;

@Repository
public interface IMAgencyCodeRepository extends JpaRepository<IMAgencyCode,Integer> {
	List<IMAgencyCode> findAll();
	List<IMAgencyCode> findAll(Sort sort);

	List<IMAgencyCode> findAllByUsername(String username);
	List<IMAgencyCode> findAllByUsernameAndOrgAgncyName(String username, String orgAgncyName);
	
	List<IMAgencyCode> findAllByAtg(String atg);
	IMAgencyCode findByAgncyId(Integer agncyId);

}
