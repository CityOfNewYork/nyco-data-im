package gov.nyc.nyco.intellimatch.service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nyc.nyco.intellimatch.constant.IMConstant;
import gov.nyc.nyco.intellimatch.models.IMAgencyCode;
import gov.nyc.nyco.intellimatch.models.IMFile;
import gov.nyc.nyco.intellimatch.models.IMUser;
import gov.nyc.nyco.intellimatch.repository.IMFileRepository;
import gov.nyc.nyco.intellimatch.repository.IMUserRepository;

/**
 * File service, create, delete, update file in file table.
 * 
 */
@Service
public class IMFileService {
	final static IMAuditLogService logger = new IMAuditLogService(IMFileService.class);
	
	@Autowired
	private IMFileRepository fileRepo;
	
	@Value("${mdmservice.enviroment.iis_inpath}")
	private String iisInPath;

    @Value("${mdmservice.data.columns}")
	private Integer data_columns;	
	
    @Value("${mdmservice.data.delimiter}")
    private String mdmdataDelimiter;
    
    @Value("${mdmservice.data.filename.trail.format}")
    private String trailFormat;
    
	@Autowired
	private SSHClientService sshClient;

	@Autowired
	private AmazonClientService amazonClient;
	
	@Autowired
	private IMDataRuleService validateService;
	
	@Autowired
	private IMAgencyCodeService portService;
	
	@Autowired
	private IMEmailService emailService;	
	
	@Autowired
	private IMUserRepository userRepo;
	
	public IMFile create(MultipartFile file, String serviceType, String username) {
		
		long start = System.currentTimeMillis();
		IMUser user = userRepo.findByUsername(username);

		// check if save data to db
		// boolean saveDb = (IMConstant.IMSERVICE_TYPE_AWS.equals(serviceType) && awsdataSavetodb) || (IMConstant.IMSERVICE_TYPE_MDM.equals(serviceType) || mdmdataSavetodb);
		
		IMFile f = new IMFile();
		f.setName(file.getOriginalFilename());
		f.setType(file.getContentType());
		f.setUsername(username);
		f.setServiceType(serviceType);
		f.setFsize(file.getSize());
		f.setCreatedDate(new Date());
		f.setUsername(username);
		f.setDepartment(user.getDepartment());

		String errMsg = "";
		try {
			// TODO
			//if(saveDb) {
			//	// memory
			//	//f.setData(file.getBytes());
			//}	
			
			byte[] data = file.getBytes();
			String dataStr = new String(data, StandardCharsets.UTF_8);

			f.setLines(new Long(countLines(dataStr)));
			
			if(IMConstant.IMSERVICE_TYPE_MDM.equals(serviceType)) {
				f.setAt(user.getAt());
				f.setAlgo(user.getAlgo());
				f.setAtg(user.getAtg());
				
				// assign available port
				// String orgAgncyName = getOrgAgncyName(f.getName()) ;
				String orgAgncyName = getOrgAgncyNameFromData(dataStr) ;
				f.setOrgAgncyName(orgAgncyName);
				
				IMAgencyCode port = null;
				if(orgAgncyName != null && !orgAgncyName.isEmpty()) {
					port = portService.create(username, orgAgncyName , user.getAt(), user.getAtg(), user.getAlgo(), user.getMaxPorts());
				}

				if(port != null) {
					f.setAgncyId(port.getAgncyId());
					//f.setAt(port.getAt());
					//f.setAlgo(port.getAlgo());
					//f.setAtg(port.getAtg());

					// Upload file to IIS folder
					String fPath = getMdmFileUploadPath(f.getAgncyId());
					f.setPath(fPath);
					
					// new file name in IIS server
					String fTrail = getNameTrail();
					String fname = port.getCciAgncyName() + "_" + fTrail;
					f.setFname(fname);
					
					// set file status to anallyzing
					f.setStatus(IMConstant.FILE_STATUS_ANALYZING);
					f.setUpdatedDate(new Date());
					f = fileRepo.save(f);
					
					// validate data
					errMsg = validateService.validateFile(orgAgncyName + "_" + fTrail, dataStr.getBytes());
					
					// errMsg is not null
					f.setErrorMessage(errMsg.substring(0,Math.min(2000, errMsg.length())));
					
					if(errMsg != null && !errMsg.isEmpty() && errMsg.startsWith("ERROR:")) {
						logger.error(errMsg, Arrays.asList(new String[] { user.getAtg() } ));
						f.setPath(null);
						f.setStatus(IMConstant.FILE_STATUS_FAIL);
						
						// release port and reset field
						f.setAgncyId(null);
						f.setFname(null);
						
						// update to release dependency 
						f = fileRepo.save(f);
						
						// revoke port
						portService.deleteById(port.getAgncyId());
						
						// send notification for Purged
						String subject = "Your matching file is failed to upload.";
						String text =  f.getName() + " has following error, please fix it and upload again. \n" + errMsg;
						emailService.create(subject, text, user.getUsername());
						
					}else{
						if(errMsg != null && !errMsg.isEmpty() && errMsg.startsWith("WARN:")) {
							logger.warn("errMsg" + errMsg, Arrays.asList(new String[] { user.getAtg() } ));
						}
						
						// replace HRA_WMS with AGNCY_1
						String src = mdmdataDelimiter + port.getOrgAgncyName()  + mdmdataDelimiter;
						String des = mdmdataDelimiter + port.getCciAgncyName()  + mdmdataDelimiter;
						logger.debug(src + " replaced with " + des, Arrays.asList(new String[] { user.getAtg() } ));
						dataStr = dataStr.replaceAll(src , des);

						// upload file to SSH server
						sshClient.upload(dataStr.getBytes(), f.getFname(), f.getPath());
						f.setFsize((long)dataStr.length());
						f.setStatus(IMConstant.FILE_STATUS_READY);
						
						// send notification for Purged
						String subject = "Your matching file is uploaded successfully.";
						String text =  f.getName() + " is uploaded as " + f.getFname() + " on port " + f.getAgncyId();
						emailService.create(subject, text, user.getUsername());
						
					}
					
				}else {
					errMsg = "ERROR:Port is not available";
					logger.error(errMsg,Arrays.asList(new String[] { user.getAtg() } ));
					f.setErrorMessage(errMsg.substring(0,Math.min(2000, errMsg.length())));
					f.setStatus(IMConstant.FILE_STATUS_FAIL);
				}

			}else if(IMConstant.IMSERVICE_TYPE_AWS.equals(serviceType)) {
				// set dbname and table name
				f.setDbName(user.getDbName());
				f.setTableName(amazonClient.getTableName(f.getName(), username));
				// exclude header line 
				f.setLines(f.getLines()-1); 
				
				// create a query-result folder if this is first time upload a file
				List<IMFile> files = fileRepo.findAllByUsernameAndServiceType(username, IMConstant.IMSERVICE_TYPE_AWS);
				if(files == null || files.isEmpty()) {
					amazonClient.sendQueryResultFile(user.getAwsUserid());
				}
				
				String path = amazonClient.uploadFile(file, username);
				f.setPath(path);
				f.setStatus(IMConstant.FILE_STATUS_PENDING);
				
				// delete if file already uploaded
				files = fileRepo.findAllByUsernameAndServiceTypeAndName(username, IMConstant.IMSERVICE_TYPE_AWS, f.getName());
				if(files == null || !files.isEmpty()) {
					fileRepo.deleteAll(files);
				}
				
			}
			
		} catch (Exception e) {
			errMsg = "ERROR:Error when uploading file : " + f.getName();
			logger.error("Error when uploading file : " + f.getName() , Arrays.asList(new String[] { user.getAtg() } ));
			logger.error(e.toString(), Arrays.asList(new String[] { user.getAtg() } ));
			f.setErrorMessage(errMsg.substring(0,Math.min(2000, errMsg.length())));
			f.setStatus(IMConstant.FILE_STATUS_FAIL);
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		f.setElapsed(end - start);
		f.setUpdatedDate(new Date());
		return fileRepo.save(f);
		
	}
	
	public void deleteById(Long id) throws Exception {
		boolean deletable = true;
		IMFile f = fileRepo.findById(id);

		if(f == null) {
			String errMsg = "File can not be found for ID = " + id;
			logger.error(errMsg);
			throw new Exception(errMsg);
		}

		String status = f.getStatus();
		if(IMConstant.FILE_STATUS_MATCHING.equals(status)) {
			String errMsg = "File can not be deleted after status " + status;
			logger.error(errMsg );
			throw new Exception(errMsg);
		}
		
		String path = f.getPath();
		if(path == null || path.isEmpty()) {
			logger.warn("File path is empty, path : " + path);
			deletable = false;
		}

		logger.debug("File path : " + path);
		if(deletable) {
			String serviceType = f.getServiceType();
			if(IMConstant.IMSERVICE_TYPE_AWS.equals(serviceType)) {
				// delete file from S3, path is full key
				amazonClient.deleteFile(f.getPath());
			}else if( IMConstant.IMSERVICE_TYPE_MDM.equals(serviceType)) {
				try {
					sshClient.delete(path + "/" + f.getFname());
				} catch (JSchException | SftpException e) {
					// file may be deleted after load
					logger.error("Error : Failed to delete file : " + f.getPath() );
					logger.error(e.toString() );
					e.printStackTrace();
				}
			}else {
				logger.error("Error : Wrong Service Type : " + serviceType );
			}
		}
		
		// delete file
		fileRepo.deleteById(id);
		
		// release port
		portService.deleteById(f.getAgncyId());	
	}
	
	public void updateErrorViewedInd(Long id) {
		IMFile f = fileRepo.findById(id);
		f.setErrorViewedInd(IMConstant.FILE_ERROR_VIEWED_IND_ONE);
		fileRepo.save(f);
	}
	
	public void updateStatusToMatching(String atg, String status) {
		logger.debug("updateStatusToMatching...");
		
		List<IMFile> files = fileRepo.findAllByAtgAndStatus(atg, status);
		if(files != null && !files.isEmpty()) {
			for(IMFile file : files) {
				file.setStatus(IMConstant.FILE_STATUS_MATCHING);
				file.setUpdatedDate(new Date());
				// send notification for match started
				String subject = file.getFname() + " is started on matching." ;
				String text = subject + " at port : " + file.getAgncyId() + " access token group : " + file.getAtg();
				emailService.create(subject, text, file.getUsername());

			}
			fileRepo.saveAll(files);
		}
	}
	
	public void updateStatusToCompleted(String atg, String status) {
		logger.debug("updateStatusToCompleted");
		List<IMFile> files = fileRepo.findAllByAtgAndStatus(atg, status);
		if(files != null && !files.isEmpty()) {
			for(IMFile file : files) {
				file.setStatus(IMConstant.FILE_STATUS_COMPLETED);
				file.setUpdatedDate(new Date());
				// send notification for match completed
				String subject = file.getFname() + " is completed on matching." ;
				String text = subject + " at port : " + file.getAgncyId() + " access token group : " + file.getAtg();
				emailService.create(subject, text, file.getUsername());
			}
			fileRepo.saveAll(files);
		}
	}
	
	
	public void purgeAwsData(String username) {
		// delete from file table and from S3
		List<IMFile> files = fileRepo.findAllByUsernameAndServiceType(username, IMConstant.IMSERVICE_TYPE_AWS);
		if(files != null && !files.isEmpty()) {
			for(IMFile file : files) {
				try {
					deleteById(file.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		// place an expired file into AWS S3 bucket
		IMUser user = userRepo.findByUsername(username);
		amazonClient.sendExpiredFile(user.getAwsUserid());
	}
	
	public void purgeMdmData(String username) {
		List<IMFile> files = fileRepo.findAllByUsernameAndServiceType(username, IMConstant.IMSERVICE_TYPE_MDM);
		if(files != null && !files.isEmpty()) {
			for(IMFile file : files) {
				try {
					deleteById(file.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isMdmDataExist(String username) {
		List<IMFile> files = fileRepo.findAllByUsernameAndServiceType(username, IMConstant.IMSERVICE_TYPE_MDM);
		return (files != null && files.isEmpty());
	}
	
	private int countLines(String str){
	    String[] lines = str.split("\r\n|\r|\n");
	    return  lines.length;
	}
	
	private String getMdmFileUploadPath(Integer id) {
		String path = iisInPath + "/" + id;
		return path;
	}
	
	private String getOrgAgncyNameFromData(String str) {
		String orgAgncyName = "";
		
		String[] lines = str.split("\r\n|\r|\n");
		if(lines != null && lines.length > 0) {
			String line = lines[0];
			String[] cols = line.split(mdmdataDelimiter);
			if(cols.length == data_columns) {
				// column 7th
				orgAgncyName = cols[6];
			}
		}
		
		return orgAgncyName;
	}

	private String getNameTrail() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(trailFormat);
		return simpleDateFormat.format(new Date()) + ".dat";
	}
	
	private List<String> getAllDbNames(List<IMFile> files) {
		List<String> dbNames = new ArrayList<String>();
		
		if(files != null && !files.isEmpty()) {
			for(IMFile file : files) {
				String dbName = file.getDbName();
				if(!dbNames.contains(dbName)) {
					dbNames.add(dbName);
				}
			}
		}
		
		return dbNames;
	}
	
	public boolean updateAwsFileStatus() {
		boolean sts = false;
		logger.debug("updateAwsFileStatus ...");
		
		List<IMFile> files = fileRepo.findAllByServiceTypeAndStatus(IMConstant.IMSERVICE_TYPE_AWS, IMConstant.FILE_STATUS_PENDING);
		List<String> dbNames = getAllDbNames( files );

		// retrieve catalogued table names
		Map<String, List<String>> dbTblMap = new HashMap<String, List<String>>();
		for(String dbName : dbNames) {
			List<String> tableNames = amazonClient.getAllTables(dbName);
			if(!tableNames.isEmpty()) {
				dbTblMap.put(dbName, tableNames);
			}
		}
		
		// update file status
		List<IMFile> updateFiles = new ArrayList<IMFile>();
		List<String> requestDbNames = new ArrayList<String>();
		for(IMFile file : files) {
			if(dbTblMap.containsKey(file.getDbName())) {
				String dbName = file.getDbName();
				
				if(dbTblMap.get(dbName).contains(file.getTableName())) {
					sts = true;
					
					file.setStatus(IMConstant.FILE_STATUS_CATALOGUED);
					file.setUpdatedDate(new Date());
					updateFiles.add(file);
					
					// send notification for file catalogued
					String subject = file.getName() + " is catalogued." ;
					String text = subject + " as table " + file.getTableName() + " in AWS Glue database :  " + dbName;
					emailService.create(subject, text, file.getUsername());

				}else {
					// check after 20 minutes for request
					if( (new Date()).getTime() - file.getCreatedDate().getTime() > 20*60*1000 ) {
						// send notification for file catalogued
						/* avoid too many emails
						String subject = file.getName() + " may failed to catalog." ;
						String text = subject + " as table " + file.getTableName() + " in AWS Glue database :  " + file.getDbName() 
						            + "\n" + " please delete the file and re upload it again.";
						
						emailService.create(subject, text, file.getUsername());
						*/
					}
					// check after 10 minutes for request catalog
					else if( (new Date()).getTime() - file.getCreatedDate().getTime() > 10*60*1000 ) {
						if(!requestDbNames.contains(file.getDbName())){
							requestDbNames.add(file.getDbName());
						}
					}
				}
			}else {
				// no catalog list yet
				String dbName = file.getDbName();
				if( (new Date()).getTime() - file.getCreatedDate().getTime() > 10*60*1000 ) {
					requestDbNames.add(dbName);
				}
			}
		}
		
		// update file status in table
		if(!updateFiles.isEmpty()) {
			fileRepo.saveAll(updateFiles);
		}
		
		// send request to s3 for catalog
		for(String db : requestDbNames) {
			logger.debug("Send request to s3 for catalog : " + db);
			amazonClient.sendTableCatalogRequestFile(db);
		}
		
		return sts;
	}
	
	public void purgeAllByAtg(String atg) {
		List<IMFile> files = fileRepo.findAllByServiceTypeAndAtg(IMConstant.IMSERVICE_TYPE_MDM, atg);
		if(files != null && !files.isEmpty()) {
			fileRepo.deleteAll(files);
		}
	}
	

	
}
