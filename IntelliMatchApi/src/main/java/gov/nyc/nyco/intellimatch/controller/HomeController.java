package gov.nyc.nyco.intellimatch.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.nyc.nyco.intellimatch.constant.IMConstant;
import gov.nyc.nyco.intellimatch.models.IMAgencyCode;
import gov.nyc.nyco.intellimatch.models.IMAxwayReport;
import gov.nyc.nyco.intellimatch.models.IMFile;
import gov.nyc.nyco.intellimatch.models.IMJob;
import gov.nyc.nyco.intellimatch.models.IMResponseMessage;
import gov.nyc.nyco.intellimatch.models.IMUser;
import gov.nyc.nyco.intellimatch.repository.IMAgencyCodeRepository;
import gov.nyc.nyco.intellimatch.repository.IMAxwayReportRepository;
import gov.nyc.nyco.intellimatch.repository.IMFileRepository;
import gov.nyc.nyco.intellimatch.repository.IMJobRepository;
import gov.nyc.nyco.intellimatch.repository.IMUserRepository;
import gov.nyc.nyco.intellimatch.service.IMAgencyCodeService;
import gov.nyc.nyco.intellimatch.service.IMAuditLogService;
import gov.nyc.nyco.intellimatch.service.IMFileService;
import gov.nyc.nyco.intellimatch.service.IMJobService;
//import gov.nyc.nyco.intellimatch.service.IMReportService;


@RestController
public class HomeController {

	final static IMAuditLogService logger = new IMAuditLogService(HomeController.class);
	
	@Autowired
	private IMFileRepository fileRepo;

	@Autowired
	private IMFileService fileService;

	@Autowired
	private IMJobRepository jobRepo;
	
	@Autowired
	private IMJobService jobService;
	
	@Autowired
	private IMAgencyCodeService portService;	
	
	@Autowired
	private IMAgencyCodeRepository portRepo;	
	
	//@Autowired
	//private IMReportService rptService;

	@Autowired
	private IMAxwayReportRepository axwayReportRepo;
	
	@Autowired
	private IMUserRepository repoUser;
	

	@GetMapping("/api/users")
	List<IMUser> getAllUsers() {
	    return repoUser.findAll();
	}
	
	/*
	 * Report API
	 */
	//	@GetMapping("/api/reports")
	//	public List<IMReport> getAllReports(){
	//		synchronized(this){
	//			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	//			String username = auth.getName();
	//			return rptService.getReports(username);
	//		}
	//	}

	//	@GetMapping("/api/reports/{agncyId}/{name}")
	//	public ResponseEntity<ByteArrayResource> downloadRpt(@PathVariable Integer agncyId, @PathVariable String name){
	//		synchronized(this){
	//			IMReport rpt = rptService.getReport(agncyId,name);
	//			
	//			if(rpt != null) {
	//				return ResponseEntity.ok()
	//						//.contentType(MediaType.parseMediaType("text/csv"))
	//						.contentType(MediaType.parseMediaType(rpt.getType()!=null?rpt.getType():"text/plain"))
	//						.header(HttpHeaders.CONTENT_DISPOSITION,"attachment:filename=\""+name+"\"")
	//						.body(new ByteArrayResource(rpt.getData()));
	//			}else{
	//				return ResponseEntity.ok()
	//						//.contentType(MediaType.parseMediaType("text/csv"))
	//						.header(HttpHeaders.CONTENT_DISPOSITION,"attachment:filename=\""+name+"\"")
	//						.body(new ByteArrayResource(null));
	//			}			
	//		}
	//	}
	
	/*
	 * IMAxwayReport API
	 */
	@GetMapping("/api/axwayreports")
	List<IMAxwayReport> getAllFileAxwayReports(){
		return axwayReportRepo.findAll();
	}
	@GetMapping("/api/axwayreports/{atg}")
	public List<IMAxwayReport> getAllFileAxwayReports(@PathVariable String atg){
		return axwayReportRepo.findAllByAtg(atg);
	}
	
	
	/*
	 * IMAgencyCode API
	 */
	@GetMapping("/api/availablePortCount")
	Integer getAvailablePortCount() {
	    return portService.getAvailablePortCount();
	}

	@GetMapping("/api/ports")
	List<IMAgencyCode> getAllPorts() {
	    return portRepo.findAll(Sort.by(new Order( Direction.ASC, "agncyId")));
	}
	
	@GetMapping("/api/getPorts/{username}")
	List<IMAgencyCode> getAllPorts(@PathVariable String username) {
	    return portRepo.findAllByUsername(username);
	}
	
	@GetMapping("/api/getPorts/{username}/{orgAgncyName}")
	List<IMAgencyCode> getPorts(@PathVariable String username , @PathVariable String orgAgncyName) {
	    return portRepo.findAllByUsernameAndOrgAgncyName(username, orgAgncyName);
	}
	
	
	/*
	 * IMJob API
	 */
	@GetMapping("/api/jobs")
	List<IMJob> getAllJobs() {
	    return jobRepo.findAll(Sort.by(new Order( Direction.ASC, "id")));
	}

	@GetMapping("/api/jobs/{atg}")
	List<IMJob> getAllJobs(@PathVariable String atg) {
	    return jobRepo.findAllByAtg(atg, Sort.by(new Order( Direction.ASC, "createdDate"))  );
	}
	
	@PostMapping(value= "/api/delJob")
	@Transactional
	ResponseEntity<IMResponseMessage> delJob(@RequestParam("id") Long id)  {
		String message = "Job id : " + id;
		logger.debug(message);

	    try {
	    	jobService.deleteById(id);

			message += " was deleted successfully." ;
		    logger.debug(message);
		    
		    return ResponseEntity.status(HttpStatus.OK).body(new IMResponseMessage(message));
	    } catch (Exception e) {
	    	logger.error("Exception " + e.toString());
		    
            // printStackTrace method 
            // prints line numbers + call stack 
            e.printStackTrace(); 
            
		    message += " was failed to delete." ;
		    logger.error(message);
		    
		    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new IMResponseMessage(message));
	    }
	}

	@PostMapping(value= "/api/addJob")
	@Transactional
	public ResponseEntity<IMResponseMessage> addJob(@RequestParam("cmd") String cmd)  {
		synchronized(this){
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String username = auth.getName();
			
			String message = "Job : " + cmd;
			logger.debug(message);
	
		    try {
		    	// check if job created
		    	if(jobService.isJobCreated(username)) {
		    		throw new Exception("Job already Created.");
		    	}
		    	
		    	String[] cmdArr = cmd.split(",");
		    	
		    	for(String c : cmdArr){
		    		if(!IMConstant.JOB_COMMAND_LIST.contains(c) ) {
		    			throw new Exception("Command not found : " + c);
		    		}
			    	jobService.create(c, username);
		    	}
	
				message += " was created successfully." ;
			    logger.debug(message);
			    
			    return ResponseEntity.status(HttpStatus.OK).body(new IMResponseMessage(message));
		    } catch (Exception e) {
		    	logger.error("Exception " + e.toString());
			    
	            // printStackTrace method 
	            // prints line numbers + call stack 
	            e.printStackTrace(); 
	            
			    message += " was failed to create." ;
			    logger.error(message);
			    
			    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new IMResponseMessage(message));
		    }
		}
	}
	
	
	/*
	 * IMFile API
	 */
	@GetMapping("/api/files")
	List<IMFile> getAllFile() {
	    return fileRepo.findAll(Sort.by(new Order( Direction.ASC, "id")));
	}

	@GetMapping("/api/awsFiles")
	public List<IMFile> getAllAwsFile() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth.getName();
	    return fileRepo.findAllByUsernameAndServiceType(username, "AWS");
	}
	
	@GetMapping("/api/files/{id}")
	IMFile getFile(@PathVariable Long id) {
	    return fileRepo.findById(id);
	}
	
	@GetMapping("/api/files/{serviceType}/{atg}")
	public List<IMFile> getAllFiles(@PathVariable String serviceType, @PathVariable String atg) {
	    return fileRepo.findAllByServiceTypeAndAtg(serviceType,atg, Sort.by(new Order( Direction.DESC, "id")));
	}

	
	@PostMapping(value= "/api/delFile")
	@Transactional
	public ResponseEntity<IMResponseMessage> delFile(@RequestParam("id") Long id)  {
		String message = "File id : " + id;
		logger.debug(message);

	    try {
	    	fileService.deleteById(id);

			message += " was deleted successfully." ;
		    logger.debug(message);
		    
		    return ResponseEntity.status(HttpStatus.OK).body(new IMResponseMessage(message));
	    } catch (Exception e) {
	    	logger.error("Exception " + e.toString());
		    
            // printStackTrace method 
            // prints line numbers + call stack 
            e.printStackTrace(); 
            
		    message += " was failed to delete." ;
		    logger.error(message);
		    
		    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new IMResponseMessage(message));
	    }
	}

	
	@PostMapping(value= "/api/viewed")
	@Transactional
	public ResponseEntity<IMResponseMessage> viewed(@RequestParam("id") Long id)  {
		String message = "File id : " + id;
		logger.debug(message);

	    try {
	    	fileService.updateErrorViewedInd(id);

			message += " was updated successfully." ;
		    logger.debug(message);
		    
		    return ResponseEntity.status(HttpStatus.OK).body(new IMResponseMessage(message));
	    } catch (Exception e) {
	    	logger.error("Exception " + e.toString());
		    
            // printStackTrace method 
            // prints line numbers + call stack 
            e.printStackTrace(); 
            
		    message += " was failed to update." ;
		    logger.error(message);
		    
		    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new IMResponseMessage(message));
	    }
	}
	
	/*
	 * File upload API
	 */
	@PostMapping(value = "/api/awsUpload")
	public ResponseEntity<IMResponseMessage> uploadAwsFile(@RequestParam("file") MultipartFile file)  {
		synchronized(this){
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String username = auth.getName();
			
		    String message = file.getOriginalFilename();
		    try {
			    logger.debug("Before upload : " + message);
		        
			    this.fileService.create(file,"AWS",username);
				
				logger.debug("After " + message);
		
			    message += " was uploaded successfully." ;
			    logger.debug(message);
			    
			    return ResponseEntity.status(HttpStatus.OK).body(new IMResponseMessage(message));
		    } catch (Exception e) {
		    	logger.error("Exception " + e.toString());
			    
	            // printStackTrace method 
	            // prints line numbers + call stack 
	            e.printStackTrace(); 
	            
			    message += " was failed to upload." ;
			    logger.error(message);
			    
			    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new IMResponseMessage(message));
		    }
	    }

	}
	
	@PostMapping("/api/mdmUpload")
	public ResponseEntity<IMResponseMessage> uploadMdmFile(@RequestParam("file") MultipartFile file)  {
	    synchronized(this){
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String username = auth.getName();
			
		    String message = file.getOriginalFilename();
		    try {
			    logger.debug("Before upload : " + message);
		        
			    this.fileService.create(file,"MDM",username);
				
				logger.debug("After " + message);
				
			    message += " was uploaded successfully." ;
			    logger.debug(message);
		
			    return ResponseEntity.status(HttpStatus.OK).body(new IMResponseMessage(message));
		    } catch (Exception e) {
		    	logger.error("Exception " + e.toString());

	            // printStackTrace method 
	            // prints line numbers + call stack 
	            e.printStackTrace(); 
	            
			    message += " was failed to upload." ;
			    logger.error(message);
			    
			    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new IMResponseMessage(message));
		    }
	    }

	}
	
}
