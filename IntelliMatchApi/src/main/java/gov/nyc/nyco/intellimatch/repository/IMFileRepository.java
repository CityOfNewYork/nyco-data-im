package gov.nyc.nyco.intellimatch.repository;

import java.util.List;

import javax.persistence.OrderBy;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gov.nyc.nyco.intellimatch.models.IMFile;

@Repository
public interface IMFileRepository  extends JpaRepository<IMFile,Integer>{
	
	@OrderBy("id ASC")
	List<IMFile> findAll();
	List<IMFile> findAll(Sort sort);
	List<IMFile> findAllByAgncyId(Integer agncyId);
	List<IMFile> findAllByUsernameAndStatus(String username, String status);
	List<IMFile> findAllByUsernameAndServiceType(String username, String serviceType);
	List<IMFile> findAllByServiceTypeAndStatus(String serviceType, String status);
	List<IMFile> findAllByServiceTypeAndAtg(String serviceType, String atg, Sort sort);
	List<IMFile> findAllByServiceTypeAndAtg(String serviceType, String atg);
	
	List<IMFile> findAllByAtgAndStatus(String atg, String status);
	
	List<IMFile> findAllByUsernameAndServiceTypeAndName(String username, String serviceType, String name);

	IMFile findById(Long id);
	void deleteById(Long id);
	
}
