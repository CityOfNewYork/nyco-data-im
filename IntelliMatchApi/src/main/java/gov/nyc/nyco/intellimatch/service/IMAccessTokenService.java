package gov.nyc.nyco.intellimatch.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.models.IMAccessToken;
import gov.nyc.nyco.intellimatch.repository.IMAccessTokenRepository;

/**
 * Update table access_token when port assigned.
 * 
 */
@Service
public class IMAccessTokenService {
	final static IMAuditLogService logger = new IMAuditLogService(IMAccessTokenService.class);
	
	@Autowired
	private IMAccessTokenRepository atRepo;
	
	public IMAccessTokenService() {
		super();
	}
	
	public void update(String orgAgncyName, String at, String atg, String algo) {
		List<IMAccessToken> atList = atRepo.findAllByOrgAgncyName(orgAgncyName);
		if(atList != null && !atList.isEmpty()) {
			for(IMAccessToken a : atList) {
				String origAtStr = a.toString();
				
				a.setAt(at);
				if(algo != null && !algo.isEmpty()) {
					a.setAlgo(algo);
				}
				a.setAtg(atg);
				a.setUpdatedDate(new Date());
				a= atRepo.save(a);
				
				String newAtStr = a.toString();
				
				logger.debug(origAtStr + " was updated to " + newAtStr);
			}
		}else {
			String err = "Error : not found for " + orgAgncyName;
			logger.error(err);
		}
	}
	
	public Long getAdminSysTpCd(String orgAgncyName) {
		Long adminSysTpCd = null;
		
		List<IMAccessToken> atList = atRepo.findAllByOrgAgncyName(orgAgncyName);
		if(atList != null && !atList.isEmpty()) {
			for(IMAccessToken a : atList) {
				adminSysTpCd = a.getAdminSysTpCd();
				break;
			}
		}else {
			String err = "Error : not found for " + orgAgncyName;
			logger.error(err);
		}

		return adminSysTpCd;
	}
	
	
}
