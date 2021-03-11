package gov.nyc.nyco.intellimatch.service;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.models.IMAgencyCode;
import gov.nyc.nyco.intellimatch.models.IMFile;
import gov.nyc.nyco.intellimatch.repository.IMAgencyCodeRepository;
import gov.nyc.nyco.intellimatch.repository.IMFileRepository;

/**
 * Assign a available port to new uploaded file.
 * 
 */
@Service
public class IMAgencyCodeService {
	final static IMAuditLogService logger = new IMAuditLogService(IMAgencyCodeService.class);
	
	@Value("${mdmservice.enviroment.max_agency}")
	private Integer maxAgency;
	
	@Value("${mdmservice.agencynamme}")
	private String agencyNamme;

	@Autowired
	private IMFileRepository fileRepo;
	
	@Autowired
	private IMAccessTokenService atService;
	
	
	private SortedSet<Integer> availablePorts = new TreeSet<Integer>();
	
	
	@Autowired
	private IMAgencyCodeRepository portRepo;
	
	public IMAgencyCodeService() {
		super();
	}

	@PostConstruct
	private void resetPorts() {
		// reset available ports
		availablePorts.clear();
		
		// regenerate all ports
		for(int i = 1; i <= maxAgency; i++) {
			availablePorts.add(i);
		}
	}

	public Integer getAvailablePortCount() {
		return maxAgency - (int) portRepo.count();
	}
	
	
	public IMAgencyCode create(String username, String orgAgncyName,  String at, String atg, String algo, Integer portLimit) {
		// reset all ports
		resetPorts();

		IMAgencyCode port = null;
		
		// find all used ports
		List<IMAgencyCode> ports = portRepo.findAll();
		
		// check if port already assigned
		for(IMAgencyCode existPort : ports) {
			if(username.equals(existPort.getUsername()) && orgAgncyName.equals(existPort.getOrgAgncyName()) && at.equals(existPort.getAt())) {
				port = existPort;
			}else {
				availablePorts.remove(existPort.getAgncyId());
			}
		}
		
		// assign a new port
		if(port == null && !availablePorts.isEmpty()) {
			// check user port limit
			if(portLimit == null || portRepo.findAllByUsername(username).size() < portLimit) {
				Integer agncyId = availablePorts.first();
				port = new IMAgencyCode();
				port.setAgncyId(agncyId);
				port.setOrgAgncyName(orgAgncyName);
				port.setUsername(username);
				port.setAt(at);
				port.setAtg(atg);
				port.setAlgo(algo);

				port.setCciAgncyName( String.format(agencyNamme, agncyId) );
				port = portRepo.save(port);

				// update ACCESS_TOKEN table
				atService.update(port.getCciAgncyName(), port.getAt(), port.getAtg(), port.getAlgo());
				
			}else {
				logger.debug("User reached port limit = " + portLimit);
			}
		}

		return port;
	} 

	public void deleteById(Integer agncyId) {
		boolean deletable = true;
		IMAgencyCode port = portRepo.findByAgncyId(agncyId);
		
		// check if port exist
		if(port == null) {
			logger.error("Port can not be found for AGNCY_ID = " + agncyId);
			deletable = false;
		}
		
		// check if port in use
		List<IMFile> files = fileRepo.findAllByAgncyId(agncyId);
		if(files != null && !files.isEmpty()) {
			logger.error("Port is in use : " + agncyId);
			deletable = false;
		}
		
		if(deletable) {
			portRepo.delete(port);
		}
	}
	
	public void purgeAllByAtg(String atg) {
		List<IMAgencyCode> ports = portRepo.findAllByAtg(atg);
		if(ports != null && !ports.isEmpty()) {
			portRepo.deleteAll(ports);
		}
	}
	
}
