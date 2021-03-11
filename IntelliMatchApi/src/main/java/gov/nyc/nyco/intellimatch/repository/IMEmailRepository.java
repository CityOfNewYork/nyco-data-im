package gov.nyc.nyco.intellimatch.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gov.nyc.nyco.intellimatch.models.IMEmail;

@Repository
public interface IMEmailRepository extends JpaRepository<IMEmail,Integer> {
	
	List<IMEmail> findAllBySentInd(String sent, Sort sort);

}
