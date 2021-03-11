package gov.nyc.nyco.intellimatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gov.nyc.nyco.intellimatch.models.IMUser;

@Repository
public interface IMUserRepository extends JpaRepository<IMUser,Integer> {
	public IMUser findByUsername(String username);
	
	List<IMUser> findAll();
	public List<IMUser> findAllByStatus(String status);
	public List<IMUser> findAllByAtg(String atg);
	public List<IMUser> findByPasswordStartingWith(String prefix);
}
