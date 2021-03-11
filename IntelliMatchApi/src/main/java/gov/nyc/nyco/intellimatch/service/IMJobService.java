package gov.nyc.nyco.intellimatch.service;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.constant.IMConstant;
import gov.nyc.nyco.intellimatch.models.IMFile;
import gov.nyc.nyco.intellimatch.models.IMJob;
import gov.nyc.nyco.intellimatch.models.IMUser;
import gov.nyc.nyco.intellimatch.repository.IMFileRepository;
import gov.nyc.nyco.intellimatch.repository.IMJobRepository;

/**
 * Job service, create, execute, purge job.
 * 
 */
@Service
public class IMJobService {

	final static IMAuditLogService logger = new IMAuditLogService(IMJobService.class);

	@Autowired
	private IMUserService userService;
	
	@Autowired
	private SSHClientService sshClient;
	
	@Autowired
	private IMFileService fileService;

	@Autowired
	private IMFileRepository fileRepo;
	
	@Autowired
	private IMAgencyCodeService portService;
	
	@Autowired
	private IMEmailService emailService;	
	
	@Autowired
	private IMAxwayReportService rptService;	
	
	
	@Value("${mdmservice.loadScriptCmd}")
	private String loadScriptCmd;
	
	@Value("${mdmservice.extractScriptCmd}")
	private String extractScriptCmd;

	@Value("${mdmservice.purgeScriptCmd}")
	private String purgeScriptCmd;

	@Value("${mdmservice.loadScriptCmd}")
	private String precciScriptCmd;
	
	@Value("${mdmservice.Jobid.format}")
	private String JobidFormat;
	
	@Value("${mdmservice.job.onefileoneload}")
	private boolean onefileoneload;
	
	@Value("${mdmservice.job.email.placeholder}")
	private String EMAIL_PLACE_HOLD;
	
	@Value("${mdmservice.job.report.placeholder}")
	private String REPORT_IND_PLACE_HOLD;
	
	@Value("${mdmservice.job.envronment.variable}")
	private String LINUX_ENV;
	
	@Value("${mdmservice.data.match.records.prefix}")
	private String matchRecordsPrefix;
	
	@Value("${mdmservice.data.reject.records.prefix}")
	private String rejectRecordsPrefix;

	@Value("${mdmservice.data.duped.records.prefix}")
	private String dupedRecordsPrefix;
	
	// Command name and template
	private final Map<String,String> JOB_COMMAND_MAP = new HashMap<String,String>();
	
	@Autowired
	private IMJobRepository jobRepo;
	
	@PostConstruct
	private void init() {
		JOB_COMMAND_MAP.put(IMConstant.JOB_COMMAND_LOAD, loadScriptCmd);
		JOB_COMMAND_MAP.put(IMConstant.JOB_COMMAND_MATCH, extractScriptCmd);
		JOB_COMMAND_MAP.put(IMConstant.JOB_COMMAND_PURGE, purgeScriptCmd);
		JOB_COMMAND_MAP.put(IMConstant.JOB_COMMAND_PRECCI, precciScriptCmd);
	}
	

	public List<IMJob> findAllJobsInPending() {
		Sort sort = Sort.by(new Order( Direction.ASC, "createdDate"));
		return jobRepo.findAllByStatus(IMConstant.JOB_STATUS_PENDING, sort);
	}

	public IMJob findNextJob() {
		IMJob job = null;
		
		List<IMJob> jobs = findAllJobsInPending();
		if(jobs != null && !jobs.isEmpty()) {
			job = jobs.get(0);
		}
		return job;
	}
	
	public boolean processNextJob() {
		boolean sts = false;
		
		// find first job
		IMJob job = findNextJob();
		if(job != null) {
			sts = true;
			long start = System.currentTimeMillis();
			try {
				// set job in running status
				job.setStatus(IMConstant.JOB_STATUS_RUNNING);
				job.setUpdatedDate(new Date());
				job = jobRepo.save(job);
				
				String subject = "Your file is starting to process.";
				String text =  job.getCommand() + " is running " + " for " 
							+ (job.getCommand().equals(IMConstant.JOB_COMMAND_LOAD)?"file : " + job.getFname(): " ATG : " + job.getAtg());
				emailService.create(subject, text, job.getUsername());
				
				// update file status to matching
				if(IMConstant.JOB_COMMAND_LOAD.equals(job.getCommand()) ||  IMConstant.JOB_COMMAND_PRECCI.equals(job.getCommand())) {
					fileService.updateStatusToMatching(job.getAtg(), IMConstant.FILE_STATUS_READY);
				}
				
				List<String> results = sshClient.execCommand(job.getScript());
				if(results != null && results.size() == 2) {

					String out = results.get(0);
					if(out != null && !out.isEmpty()) {
						job.setMessage(out.substring(0,Math.min(2000, out.length())));
						
						// update file match count
						if(job.getFileId() != null){
							IMFile file = fileRepo.findById(job.getFileId());
							if(file != null) {
								int successCnt = getCountFromString(job.getMessage(), matchRecordsPrefix);
								int rejectCnt = getCountFromString(job.getMessage(), rejectRecordsPrefix);
								int dupedCnt = getCountFromString(job.getMessage(), dupedRecordsPrefix);
								
								file.setMdmMatchRecords(successCnt);
								file.setMdmRejectRecords(rejectCnt);
								file.setMdmDupedRecords(dupedCnt);
								
								fileRepo.save(file);
							}
						}
					}
					
					String err = results.get(1);
					if(err != null && !err.isEmpty()) {
						job.setErrorMessage(err.substring(0,Math.min(2000, err.length())));
					}

					logger.debug("Script : " + job.getScript(), Arrays.asList(new String[] { job.getAtg(), job.getMatchRun().toString() } ));
					logger.debug("Result : " + out, Arrays.asList(new String[] { job.getAtg(), job.getMatchRun().toString() } ));
					logger.debug("Error : " + err, Arrays.asList(new String[] { job.getAtg(), job.getMatchRun().toString() } ));
					
					if(job.getErrorMessage() != null && !job.getErrorMessage().isEmpty()) {
						job.setStatus(IMConstant.JOB_STATUS_FAILED);
					}else {
						job.setStatus(IMConstant.JOB_STATUS_COMPLETED);
					}
				}else {
					String err = "Error happened when running job : " + job.getScript();
					job.setErrorMessage(err);
					job.setStatus(IMConstant.JOB_STATUS_FAILED);
					
				}

				// update file status to completed
				if(IMConstant.JOB_COMMAND_LOAD.equals(job.getCommand()) ||  IMConstant.JOB_COMMAND_PRECCI.equals(job.getCommand())) {
					fileService.updateStatusToCompleted(job.getAtg(), IMConstant.FILE_STATUS_MATCHING);
				}
				
				// if command is purge and not failed, then purge all tables
				if( IMConstant.JOB_COMMAND_PURGE.equals(job.getCommand()) && IMConstant.JOB_STATUS_COMPLETED.equals(job.getStatus()) ) {
					fileService.purgeAllByAtg(job.getAtg());
					portService.purgeAllByAtg(job.getAtg());
					rptService.purgeAllByAtg(job.getAtg());

					// TODO save or purge
					boolean keepjob = false;
					if(keepjob) {
						long end = System.currentTimeMillis();
						job.setElapsed(end - start);
						job.setUpdatedDate(new Date());
						jobRepo.save(job);
					}else {
						purgeAllByAtg(job.getAtg());
						return sts;
					}
				}

				subject = "Your file has been processed.";
				text =  job.getCommand() + " completed " + " for " 
						+  (job.getCommand().equals(IMConstant.JOB_COMMAND_LOAD)?"file : " + job.getFname(): " ATG : " + job.getAtg());
				emailService.create(subject, text, job.getUsername());
				
			} catch (Exception e) {
				String err = "Exception happened when running job : " + e.toString();
				
				job.setErrorMessage(err);
				job.setStatus(IMConstant.JOB_STATUS_FAILED);
				e.printStackTrace();
				
				String subject = "Your file is failed to process.";
				String text =  job.getCommand() + " is failed " + " for your group files " 
						+  (job.getCommand().equals(IMConstant.JOB_COMMAND_LOAD)?"file : " + job.getFname(): " ATG : " + job.getAtg());
				emailService.create(subject, text, job.getUsername());
				
			}
			
			// update job
			long end = System.currentTimeMillis();
			job.setElapsed(end - start);
			job.setUpdatedDate(new Date());
			jobRepo.save(job);

		}
		
		return sts;
	}
	
	public boolean isJobCreated(String username) {
		boolean rc = false;
		
		IMUser user = userService.findUserByUsername(username);
		if(user != null) {
			int pendCnt = jobRepo.findAllByAtgAndStatus(user.getAtg(), IMConstant.JOB_STATUS_PENDING).size();
			int runCnt = jobRepo.findAllByAtgAndStatus(user.getAtg(), IMConstant.JOB_STATUS_RUNNING).size();
			rc = (pendCnt + runCnt) > 0;
		}
		
		return rc;
	}
	
	public void create(String cmd, String username) throws Exception {
		// find Ready to Match files
		IMUser user = userService.findUserByUsername(username);
		
		// get match run, set to File to link job run and file 
		int maxRun = getMatchRun(user.getAtg());
		
		if(IMConstant.JOB_COMMAND_LOAD.equals(cmd)) {
			List<IMFile> files = fileRepo.findAllByAtgAndStatus(user.getAtg(), IMConstant.FILE_STATUS_READY);
			if(files != null && !files.isEmpty()) {
				int matchRun = maxRun+1;

				// create load job
				if(onefileoneload) {
					// 1 file 1 load, as per discuss on Feb.19 with NYCO
					for (IMFile file : files) {
						create(cmd,username,file.getAgncyId(), matchRun, file.getId(), file.getFname());
						file.setMatchRun(matchRun);
					}
				}else {
					// 1 port 1 load 
					List<Integer> idList = new ArrayList<Integer>();
					for (IMFile file : files) {
						Integer id = file.getAgncyId();
						if(id != null && !idList.contains(id)) {
							create(cmd,username,file.getAgncyId(), matchRun, null, null);
							idList.add(id);
						}
						file.setMatchRun(matchRun);
					}
				}
				
				// save file
				fileRepo.saveAll(files);
				
			}	
		}else {
			// not LOAD command
			create(cmd, username, null, maxRun, null,null);
			return;
		}
	}
			
			
	synchronized private void create(String cmd, String username, Integer agncyId, Integer matchRun, Long fileId, String fname) throws Exception {
		// sleep 1 sec to avoid creating same  time job 
		Thread.sleep(1000);
		
		logger.debug("Create job...." + cmd, Arrays.asList(new String[] { matchRun.toString() } ));

		IMJob job = new IMJob();
		job.setCommand(cmd);
		job.setUsername(username);
		job.setCreatedDate(new Date());
		job.setUpdatedDate(new Date());
		job.setMatchRun(matchRun);
		job.setFileId(fileId);
		job.setFname(fname);
		
		
		String template = JOB_COMMAND_MAP.get(cmd);
		if(template == null || template.isEmpty()) {
			String err = "Command not found : " + cmd;
			job.setErrorMessage(err);
			job.setStatus(IMConstant.JOB_STATUS_FAILED);
			logger.error(err, Arrays.asList(new String[] { matchRun.toString() } ));
		}else {
			IMUser user = userService.findUserByUsername(username);
			if(user != null) {
				job.setAt(user.getAt());
				job.setAtg(user.getAtg());
				job.setAlgo(user.getAlgo());
				job.setAgncyId(agncyId);

				// email can not be null
				String email = user.getEmail();
				email = (email==null)?"":email;
				
				// report ind can not be null
				String ind = user.getReportInd();
				ind = (ind==null)?"":ind;
				
				String script = template.replace(EMAIL_PLACE_HOLD, email);
				script = script.replace(REPORT_IND_PLACE_HOLD, ind);
				job.setTemplate(template);

				// Set environement
				String env =  LINUX_ENV.replace("name", "USERNAME").replace("value", username)
				   		   + LINUX_ENV.replace("name", "EMAIL").replace("value", user.getEmail())
				   		   + LINUX_ENV.replace("name", "DEPARTMENT").replace("value", user.getDepartment())
				           + LINUX_ENV.replace("name", "MATCHING_ALGO").replace("value", user.getAlgo()==null?"":user.getAlgo())
				           + LINUX_ENV.replace("name", "ACCESS_TOKEN").replace("value", user.getAt()==null?"":user.getAt())
						   + LINUX_ENV.replace("name", "ACCESS_TOKEN_GROUP").replace("value", user.getAtg()==null?"":user.getAtg())
						   + LINUX_ENV.replace("name", "OUTPUT_AXWAYID").replace("value", user.getOutputAxwayId()==null?"":user.getOutputAxwayId())
				   		   + LINUX_ENV.replace("name", "REPORT_LIST").replace("value", user.getReportAccess()==null?"":user.getReportAccess())
				   		   + LINUX_ENV.replace("name", "MATCH_RUN").replace("value", matchRun==null?"":matchRun.toString())
				   		   ;
				
				// Add agncy id if exists
				if(IMConstant.JOB_COMMAND_LOAD.equals(cmd) && agncyId != null) {
					env = env + LINUX_ENV.replace("name", "PORT_NUMBER").replace("value", String.valueOf(agncyId));
				}
				// add file name if exists
				if(IMConstant.JOB_COMMAND_LOAD.equals(cmd) && fname != null && !fname.isEmpty() ) {
					env = env + LINUX_ENV.replace("name", "FILE_NAME").replace("value", fname);
				}
				
				// add atg email list
				List<String> atgEmails = userService.getAtgEmailList(user.getAtg(), user.getEmail());
				if(!atgEmails.isEmpty()) {
					env = env + LINUX_ENV.replace("name", "ATG_EMAIL_LIST").replace("value", String.join(";", atgEmails));
				}
				
				
				job.setScript(env + script);

				// check if user has role
				@SuppressWarnings("rawtypes")
				Map claimMap = userService.getClaims(username, IMConstant.IMSERVICE_TYPE_MDM);
				String claim = (String)claimMap.get(cmd);
				if(claim != null && !claim.isEmpty() && claim.equals(cmd)) {
					job.setStatus(IMConstant.JOB_STATUS_PENDING);
				}else {
					String err = "User does not have permission : " + cmd;
					job.setErrorMessage(err);
					job.setStatus(IMConstant.JOB_STATUS_FAILED);
					logger.error(err,Arrays.asList(new String[] { user.getAtg(), matchRun.toString() } ));
				}
				
			}else {
				String err = "User not found : " + username;
				job.setStatus(IMConstant.JOB_STATUS_FAILED);
				logger.error(err, Arrays.asList(new String[] { matchRun.toString() } ));
			}
		}
		
		// save
		job =  jobRepo.save(job);
		
		// update with job id if status is pending
		if(IMConstant.JOB_STATUS_PENDING.equals(job.getStatus())) {
			job.setJobId(String.format(JobidFormat, job.getId()));
			job = jobRepo.save(job);
			
		}
	
	}
	
	
	public void deleteById(Long id) {
		boolean deletable = true;
		IMJob job = jobRepo.findById(id);

		if(job == null) {
			logger.error("Job can not be found for ID = " + id);
			deletable = false;
		}
		

		String status = job.getStatus();
		if(IMConstant.JOB_STATUS_RUNNING.equals(status)) {
			logger.warn("Job can not be deleted in status " + status );
			deletable = false;;
		}
		
		if(deletable) {
			jobRepo.deleteById(id);
		}
	}
	
	public void deleteAllByUser(String username) {
		List<IMJob> jobs = jobRepo.findAllByUsername(username);
		if(jobs != null && !jobs.isEmpty()) {
			for(IMJob job : jobs) {
				deleteById(job.getId());
			}
		}
	}
	
	public boolean isDataPurgedInIIS(String atg) {
		boolean isPurged = true;
		
		List<IMJob> jobs = jobRepo.findAllByCommandAndAtgAndStatus(IMConstant.JOB_COMMAND_PURGE, atg, IMConstant.FILE_STATUS_COMPLETED);
		isPurged = (jobs != null && !jobs.isEmpty());
		
		return isPurged;
	}
	
	public void purgeAllByAtg(String atg) {
		List<IMJob> jobs = jobRepo.findAllByAtg(atg);
		if(jobs != null && !jobs.isEmpty()) {
			jobRepo.deleteAll(jobs);
		}
	}
	
	private int getMatchRun(String atg){
		int run = 0;
		
		List<IMJob> jobs = jobRepo.findAllByAtg(atg);
		if(jobs != null && !jobs.isEmpty()) {
			for(IMJob job : jobs) {
				run = Math.max(run, job.getMatchRun());
			}
		}
		return run;
	}
	
	// find number from string
	private int getCountFromString(String msg, String prefix) {
		int cnt = 0;

		BufferedReader br = new BufferedReader(new StringReader(msg));
	    try {
	    	logger.debug("Prefix : " + prefix);
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				logger.debug("Line : " + line);
				if(!line.isEmpty() && line.startsWith(prefix)) {
					logger.debug("Found count line : " + line + " start with " + prefix);
					String numStr = line.replace(prefix, "").trim();
					cnt = Integer.valueOf(numStr);
					break;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Not Found count start with " + prefix);
			logger.error("Error " + e.toString());
			e.printStackTrace();
		}
		
		return cnt;
	}
	
}
