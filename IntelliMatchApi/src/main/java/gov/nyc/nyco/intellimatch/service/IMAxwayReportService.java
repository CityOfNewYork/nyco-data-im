package gov.nyc.nyco.intellimatch.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.models.IMAxwayReport;
import gov.nyc.nyco.intellimatch.repository.IMAxwayReportRepository;

/**
 * Report service, purge reports generated from axway report table.
 * 
 */
@Service
public class IMAxwayReportService {
	@Autowired
	private IMAxwayReportRepository axwayReportRepo;
	
	void purgeAllByAtg(String atg) {
		List<IMAxwayReport> rpts = axwayReportRepo.findAllByAtg(atg);
		if(rpts != null && !rpts.isEmpty()) {
			axwayReportRepo.deleteAll(rpts);
		}
	}
}
