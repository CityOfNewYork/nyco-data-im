package gov.nyc.nyco.intellimatch.repository;

import java.util.List;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gov.nyc.nyco.intellimatch.models.IMAxwayReport;

@Repository
public interface IMAxwayReportRepository extends JpaRepository<IMAxwayReport,Integer> {
	
	@OrderBy("id ASC")
	List<IMAxwayReport> findAll();
	
	@OrderBy("id ASC")
	List<IMAxwayReport> findAllByAtg(String atg);
}
