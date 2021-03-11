package gov.nyc.nyco.intellimatch.repository;

import java.util.List;

import javax.persistence.OrderBy;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import gov.nyc.nyco.intellimatch.models.IMJob;

public interface IMJobRepository extends JpaRepository<IMJob,Integer> {

	@OrderBy("id ASC")
	List<IMJob> findAll();

	List<IMJob> findAll(Sort s);
	
	List<IMJob> findAllByCommandAndAtgAndStatus(String command, String atg, String status);
	
	List<IMJob> findAllByStatus(String status, Sort sort);
	List<IMJob> findAllByUsername(String username);

	List<IMJob> findAllByAtg(String atg);
	List<IMJob> findAllByAtg(String atg, Sort sort);

	List<IMJob> findAllByAtgAndStatus(String atg, String status);
	
	IMJob findById(Long id);
	void deleteById(Long id);
	
}
