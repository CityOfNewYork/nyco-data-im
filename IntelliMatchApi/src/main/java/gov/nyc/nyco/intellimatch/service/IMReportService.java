package gov.nyc.nyco.intellimatch.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.models.IMAgencyCode;
import gov.nyc.nyco.intellimatch.models.IMReport;
import gov.nyc.nyco.intellimatch.repository.IMAgencyCodeRepository;

/**
 * IIS report service, retrieve report info from IIS folder.
 * Deprecated, use axway report instead.
 */
@Service
class IMReportService {

	final static IMAuditLogService logger = new IMAuditLogService(IMReportService.class);
	
	@Autowired
	private SSHClientService sshClient;
	
	@Autowired
	private IMAgencyCodeRepository portRepo;
	
	@Value("${mdmservice.enviroment.iis_outpath}")
	private String iisOutPath;

	public IMReport getReport(Integer agncyId, String fname) {
		IMReport rpt = null;
		
		List<IMReport> rpts;
		try {
			rpts = sshClient.getFileList(iisOutPath, agncyId, fname, true );
			if(rpts != null && !rpts.isEmpty()) {
				rpt = rpts.get(0);
				
				logger.debug("report : " + rpt.getData().toString());
			}
		} catch (Exception e) {
			logger.error("Error : " +e.toString());
			e.printStackTrace();
		}
		return rpt;
	}
	
	public List<IMReport> getReports(String username) {
		List<IMReport> rpts = new ArrayList<IMReport>();
		
		List<IMAgencyCode> ports = portRepo.findAllByUsername(username);
		if(ports != null) {
			for(IMAgencyCode port : ports) {
				// search for IIS output folder
				try {
					List<IMReport > files = sshClient.getFileList(iisOutPath, port.getAgncyId(), null, false );
					rpts.addAll(files);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("Error : " +e.toString());
					e.printStackTrace();
				}
			}
		}
		
		return rpts;
	}
	
	
	
}
