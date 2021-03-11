package gov.nyc.nyco.intellimatch.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import gov.nyc.nyco.intellimatch.constant.IMConstant;
import gov.nyc.nyco.intellimatch.models.IMUser;
import gov.nyc.nyco.intellimatch.repository.IMUserRepository;

/**
 * User profile service, create, manage user profile.
 * 
 */
@Service
public class IMUserService  {
	final static IMAuditLogService logger = new IMAuditLogService(IMUserService.class);
	
	@Autowired
	private IMUserRepository userReposity;	
	
	@Autowired
	private IMEmailService emailService;	
	
	@Autowired
	private IMFileService fileService;
	
	@Autowired
	private IMJobService jobService;
	
	@Value("${userprofile.notification.days.before.expire}")
	private Integer daysBeforeExpire;	
	
	@Value("${userprofile.notification.days.before.purge}")
	private Integer daysBeforePurge;	
	
	@Value("${userprofile.attempt.max}")
	private Integer attemptMax;	
	
	@Value("${userprofile.attempt.reset.minutes}")
	private Integer resetMinutes;		
	
	@Value("${spring.application.name}")
	private String appName;

	@Value("${spring.application.version}")
	private String appVersion;

	@Value("${mdmservice.report.output.axwayurl}")
	private String axwayurl;

	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSizeString;

	@Value("${mdmservice.data.analyzing.records}")
	private Integer analyingRecordsPerSecond;

	@Value("${mdmservice.data.matching.records}")
	private Integer matchingRecordsPerSecond;

	@Value("${userprofile.password.prefix}")
	private String passwordPrefix;
	
	@Value("${jwt.user.timeout}")
	private Integer timeout;
	
	
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	public void encryptPassword() {
		List<IMUser> users = userReposity.findByPasswordStartingWith(passwordPrefix);
		if(users != null && !users.isEmpty()) {
			for(IMUser user : users) {
				user.setPassword(passwordEncoder.encode(user.getPassword().replace(passwordPrefix, "")));
			}
			userReposity.saveAll(users);
		}
	}
	
	public IMUser findUserByUsername(String username){
		// logger.debug("username:"+username);
		IMUser u = null;
		List<IMUser> userList = userReposity.findAll();
		for(IMUser user : userList){
			if(username.equals(user.getUsername())) {
				u = user;
			}
		}
		
		return u;
	}
    
	public IMUser create(IMUser u) {
		return userReposity.save(u);
	}
	
    /**
     * Create user claims for user control.
     * 
     * @param username login username
     * @param serviceType login service type, MDM or AWS
     * @return Returns user claims.
     * 
     */
	public Map<String, Object> getClaims(String username, String serviceType){
	    Map<String, Object> claims = new HashMap<>();

	    IMUser u = findUserByUsername(username);
	    
	    // check if user has this service
	    if(u.getType() == null || !u.getType().contains(serviceType)) {
	    	return null;
	    }
	    
	    
	    claims.put("appName", appName);
	    claims.put("appVersion", appVersion);
	    claims.put("serviceType", serviceType);
	    
	    // Start/expire date
	    Date startDate = null;
	    Date expireDate = null;
	    if(serviceType == null || serviceType.isEmpty()) {
	    	// start date
	    	if(u.getAwsStartDate() != null && u.getMdmStartDate() != null) {
	    		SortedSet<Date> st = new TreeSet<Date>();
	    		st.add(u.getAwsStartDate());
	    		st.add(u.getMdmStartDate());
	    		startDate = st.first();
	    	}else if(u.getAwsStartDate() != null) {
	    		startDate = u.getAwsStartDate();
	    		
	    	}else if(u.getMdmStartDate() != null) {
	    		startDate = u.getMdmStartDate();
	    	}
	    	claims.put("startDate", startDate);
	    	
	    	// expire date
	    	if(u.getAwsExpireDate() != null && u.getMdmExpireDate() != null) {
	    		SortedSet<Date> st = new TreeSet<Date>();
	    		st.add(u.getAwsExpireDate());
	    		st.add(u.getMdmExpireDate());
	    		expireDate = st.last();
	    	}else if(u.getAwsExpireDate() != null) {
	    		expireDate = u.getAwsExpireDate();
	    		
	    	}else if(u.getMdmExpireDate() != null) {
	    		expireDate = u.getMdmExpireDate();
	    	}
	    	claims.put("expireDate", expireDate);
	    }else if(serviceType.equals(IMConstant.IMSERVICE_TYPE_AWS)) {
	    	claims.put("startDate", u.getAwsStartDate());
	    	claims.put("expireDate", u.getAwsExpireDate());
	    }else if(serviceType.equals(IMConstant.IMSERVICE_TYPE_MDM)) {
	    	claims.put("startDate", u.getMdmStartDate());
	    	claims.put("expireDate", u.getMdmExpireDate());
	    }
	    claims.put("awsStartDate", u.getAwsExpireDate());
	    claims.put("mdmStartDate", u.getMdmExpireDate());
	    claims.put("awsExpireDate", u.getAwsExpireDate());
	    claims.put("mdmExpireDate", u.getMdmExpireDate());
	    
	    claims.put("jti", UUID.randomUUID().toString());
	    claims.put("username", u.getUsername());
	    claims.put("firstName", u.getFirstName());
	    claims.put("lastName", u.getLastName());
	    claims.put("report_ind", u.getReportInd());
	    
	    long maxSize = DataSize.parse(maxFileSizeString).toBytes();
	    if(u.getMdmSizeLimit() != null) {
		    claims.put("mdmSizeLimit", u.getMdmSizeLimit());
	    }else {
		    claims.put("mdmSizeLimit", maxSize);
	    }
	    
	    if(u.getAwsSizeLimit() != null) {
		    claims.put("awsSizeLimit", u.getAwsSizeLimit());
	    }else {
	    	claims.put("awsSizeLimit", maxSize);
	    }
	    
	    claims.put("type", u.getType());
	    claims.put("im_usage", u.getImUsage());
	    claims.put("reportAccess", u.getReportAccess());
	    claims.put("awsUserid", u.getAwsUserid());
	    claims.put("dbName", u.getDbName());
	    claims.put("email", u.getEmail());
	    claims.put("status", u.getStatus());
	    claims.put("at", u.getAt());
	    claims.put("atg", u.getAtg());
	    claims.put("algo", u.getAlgo());
	    claims.put("project", u.getProject());
	    claims.put("department", u.getDepartment());
	    claims.put("maxPorts", u.getMaxPorts());
	    claims.put("outputAxwayId", u.getOutputAxwayId());
	    claims.put("axwayurl", axwayurl);
	    claims.put("timeout", timeout);
	    
	    if(claims.get("expireDate") != null) {
		    claims.put("expireDateFormated", new SimpleDateFormat("MMMMM dd, yyyy").format((Date)claims.get("expireDate")));
	    }else {
		    claims.put("expireDateFormated", "Not Set");
	    }

	    // eta
	    claims.put("analyingRecordsPerSecond", analyingRecordsPerSecond);
	    claims.put("matchingRecordsPerSecond", matchingRecordsPerSecond);
	    
	    // TODO get user roles
	    claims.put("PRECCI", "PRECCI");
	    claims.put("LOAD", "LOAD");
	    claims.put("MATCH", "MATCH");
	    claims.put("LOADMATCH", "LOADMATCH");
	    claims.put("PURGE", "PURGE");
	    claims.put("MDMUPLOAD", "MDMUPLOAD");
	    claims.put("AWSLAUNCH", "AWSLAUNCH");
	    claims.put("AWSUPLOAD", "AWSUPLOAD");
		    
	    return claims;
	}

    /**
     * Expire user access when expire date comes
     * <pre>
     *  1. send notification before expire                        
     *  2. update notification indicator                          
     *  3. send notification when expired                         
     *  4. update status to expired when both AWS and MDM expired 
     * </pre>
     */
	public void expire() {
		List<IMUser> updatedUsers = new ArrayList<IMUser>();
		
		List<IMUser> users = userReposity.findAllByStatus(IMConstant.USER_STATUS_ACTIVE);
		if(users != null && !users.isEmpty()) {
			boolean userUpdated = false;
			
			String pattern = "MM/dd/yyyy";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			
			Date now = new Date();
		    
		    Calendar cal = Calendar.getInstance();
		    cal.setTime( now );
		    cal.add( Calendar.DATE, daysBeforeExpire );
		    Date startDate = cal.getTime(); 
			
			for(IMUser user : users) {
				
				boolean isAwsUser = (user.getImUsage() == null) || user.getImUsage().contains(IMConstant.IMSERVICE_TYPE_AWS);
				boolean isMdmUser = (user.getImUsage() == null) || user.getImUsage().contains(IMConstant.IMSERVICE_TYPE_MDM);

				///////////////////////////////////////
				// check if AWS account expire
				///////////////////////////////////////
				if(isAwsUser) {
					Date awsExpireDate = user.getAwsExpireDate();

					// set indicator if not set
					if(!IMConstant.USER_NOTIFICATION_IND_LIST.contains(user.getAwsExpireNotificationInd())) {
						user.setAwsExpireNotificationInd(IMConstant.USER_NOTIFICATION_IND_ZERO);
						userUpdated = true;
					}
					
					if(isAwsUser && awsExpireDate != null) {
						
						String prefix = "Your IntelliMatch AWS Service";
						if(startDate.after(awsExpireDate) && IMConstant.USER_NOTIFICATION_IND_ZERO.equals(user.getAwsExpireNotificationInd())) {
							
							// set notification for close to expired
							String subject = prefix + " is about to expire on " + simpleDateFormat.format(awsExpireDate);
							String text = subject + "\n" ;
							emailService.create(subject, text, user.getUsername());
							
							// update user
							user.setAwsExpireNotificationInd(IMConstant.USER_NOTIFICATION_IND_FIRST);
							userUpdated = true;
						}else if(now.after(awsExpireDate) && IMConstant.USER_NOTIFICATION_IND_FIRST.equals(user.getAwsExpireNotificationInd()) ) {
							
							// send notification for expired
							String subject = prefix + " is expired on " + simpleDateFormat.format(awsExpireDate);
							String text = subject + "\n" ;
							emailService.create(subject, text, user.getUsername());
							
							// update user
							user.setAwsExpireNotificationInd(IMConstant.USER_NOTIFICATION_IND_SECOND);
							userUpdated = true;
						} 
					}
				}

				///////////////////////////////////////
				// check if MDM account expire
				///////////////////////////////////////
				if(isMdmUser) {
					Date mdmExpireDate = user.getMdmExpireDate();

					// set indicator if not set
					if(!IMConstant.USER_NOTIFICATION_IND_LIST.contains(user.getMdmExpireNotificationInd())) {
						user.setMdmExpireNotificationInd(IMConstant.USER_NOTIFICATION_IND_ZERO);
						userUpdated = true;
					}
					
					if(mdmExpireDate != null) {
						String prefix = "Your IntelliMatch MDM Service";
						if(startDate.after(mdmExpireDate) && IMConstant.USER_NOTIFICATION_IND_ZERO.equals(user.getMdmExpireNotificationInd())) {
							
							// set notification for close to expired
							String subject = prefix + " is about to expire on " + simpleDateFormat.format(mdmExpireDate);
							String text = subject + "\n" ;
							emailService.create(subject, text, user.getUsername());

							// update user
							user.setMdmExpireNotificationInd(IMConstant.USER_NOTIFICATION_IND_FIRST);
							userUpdated = true;
						} else if(now.after(mdmExpireDate) && IMConstant.USER_NOTIFICATION_IND_FIRST.equals(user.getMdmExpireNotificationInd())) {
							
							// send notification for expired
							String subject = prefix + " is expired on " + simpleDateFormat.format(now);
							String text = subject + "\n" ;
							emailService.create(subject, text, user.getUsername());

							// update user
							user.setMdmExpireNotificationInd(IMConstant.USER_NOTIFICATION_IND_SECOND);
							userUpdated = true;
						} 
					}
				}
			
				// update user status when both expired
				if( (!isAwsUser || IMConstant.USER_NOTIFICATION_IND_SECOND.equals(user.getAwsExpireNotificationInd()))
					&&	
					(!isMdmUser || IMConstant.USER_NOTIFICATION_IND_SECOND.equals(user.getMdmExpireNotificationInd()))
				) {
					
					String prefix = "Your IntelliMatch Account";
					String subject = prefix + " is expired on " + simpleDateFormat.format(now);
					String text = subject + "\n" ;
					emailService.create(subject, text, user.getUsername());
					
					// update user
					user.setStatus(IMConstant.USER_STATUS_EXPIRED);
					userUpdated = true;
				}
				
				// add updated users
				if(userUpdated) {
					user.setUpdatedDate(new Date());
					updatedUsers.add(user);
				}
			}
			
			// update to db
			if(!updatedUsers.isEmpty()) {
				userReposity.saveAll(updatedUsers);
			}
		}
	}
	
    /**
     * Purge user data when purge date comes.
     * <pre>
     *  1. send notification before purge                          
     *  2. update notification indicator                           
     *  3. send notification when purge                            
     *  4. update status to inactive when both AWS and MDM purged
     *  5. MDM data always purge by group.
     *  6. AWS data can be purged by group or by user, base on file delete indicator. 
     *  </pre> 
     * 
     */
	public void purge() {
		List<IMUser> updatedUsers = new ArrayList<IMUser>();
		
		List<IMUser> users = userReposity.findAllByStatus(IMConstant.USER_STATUS_EXPIRED);
		users.addAll(userReposity.findAllByStatus(IMConstant.USER_STATUS_ACTIVE));
		
		if(users != null && !users.isEmpty()) {
			// find purge date for AWS group
			Map<String, Date> awsGroupPurgeDateMap = new HashMap<String, Date>();
			Map<String, Date> mdmGroupPurgeDateMap = new HashMap<String, Date>();
			for(IMUser user : users) {
				// find last purge date for db group
				if(user.getType() != null && user.getType().contains(IMConstant.IMSERVICE_TYPE_AWS)) {
					String dbName = user.getDbName();
					Date awsPurgeDate = user.getAwsPurgeDate();
					if(awsGroupPurgeDateMap.containsKey(dbName)) {
						Date d = awsGroupPurgeDateMap.get(dbName);
						// take the future one
						if(d != null && awsPurgeDate != null && d.before(awsPurgeDate)) {
							awsGroupPurgeDateMap.put(dbName, awsPurgeDate);
						}
					}else if (awsPurgeDate != null){
						awsGroupPurgeDateMap.put(dbName, awsPurgeDate);
					}
				}

				// Find ATG purge date of admin
				if(user.getType() != null && user.getType().contains(IMConstant.IMSERVICE_TYPE_MDM_ADMIN)) {
					String atg = user.getAtg();
					Date mdmPurgeDate = user.getMdmPurgeDate();
					if(mdmGroupPurgeDateMap.containsKey(atg)) {
						Date d = mdmGroupPurgeDateMap.get(atg);
						// take the future one
						if(d != null && mdmPurgeDate != null && d.before(mdmPurgeDate)) {
							mdmGroupPurgeDateMap.put(atg, mdmPurgeDate);
						}
					}
					mdmGroupPurgeDateMap.put(atg, mdmPurgeDate);
				}
			}
			
			// purge for user
			boolean userUpdated = false;
			
			String pattern = "MM/dd/yyyy";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			
			Date now = new Date();
		    
		    Calendar cal = Calendar.getInstance();
		    cal.setTime( now );
		    cal.add( Calendar.DATE, daysBeforePurge );
		    Date startDate = cal.getTime(); 
			
			for(IMUser user : users) {
				boolean isAwsUser = (user.getImUsage() == null) || user.getType().contains(IMConstant.IMSERVICE_TYPE_AWS);
				boolean isMdmUser = (user.getImUsage() == null) || user.getType().contains(IMConstant.IMSERVICE_TYPE_MDM);
				boolean isAwsDelByGrp = !IMConstant.DATA_DEL_BY_USER.equals(user.getAwsFileDelInd());
				boolean isMdmDelByGrp = !IMConstant.DATA_DEL_BY_USER.equals(user.getMdmFileDelInd());
				
				///////////////////////////////////////
				// check AWS account for Purge
				///////////////////////////////////////
				if(isAwsUser) {
					Date awsPurgeDate = user.getAwsPurgeDate();

					// check if group purge
					if(isAwsDelByGrp) {
						awsPurgeDate = awsGroupPurgeDateMap.get(user.getDbName());
						user.setAwsPurgeDate(awsPurgeDate);
						userUpdated = true;
					}

					// set indicator if not set
					if(!IMConstant.USER_NOTIFICATION_IND_LIST.contains(user.getAwsPurgeNotificationInd())) {
						user.setAwsPurgeNotificationInd(IMConstant.USER_NOTIFICATION_IND_ZERO);
						userUpdated = true;
					}
					// purge 
					if(awsPurgeDate != null) {
						String prefix = "Your IntelliMatch AWS Service data ";
						if(startDate.after(awsPurgeDate) && IMConstant.USER_NOTIFICATION_IND_ZERO.equals(user.getAwsPurgeNotificationInd())) {
							
							// set notification for close to Purged
							String subject = prefix + " is about to purge on " + simpleDateFormat.format(awsPurgeDate);
							String text = subject + "\n" ;
							emailService.create(subject, text, user.getUsername());
							
							// update user
							user.setAwsPurgeNotificationInd(IMConstant.USER_NOTIFICATION_IND_FIRST);
							userUpdated = true;
						}else if(now.after(awsPurgeDate) && IMConstant.USER_NOTIFICATION_IND_FIRST.equals(user.getAwsPurgeNotificationInd()) ) {
							
							// send notification for Purged
							String subject = prefix + " is purged on " + simpleDateFormat.format(now);
							String text = subject + "\n" ;
							emailService.create(subject, text, user.getUsername());
							
							// update user
							user.setAwsPurgeNotificationInd(IMConstant.USER_NOTIFICATION_IND_SECOND);
							userUpdated = true;
							
							// purge file from aws and file table
							fileService.purgeAwsData(user.getUsername());
							
						} 
					}
				}
				

				///////////////////////////////////////
				// check MDM account for Purge
				///////////////////////////////////////
				if(isMdmUser) {
					Date mdmPurgeDate = user.getMdmPurgeDate();

					// check if group purge
					if(isMdmDelByGrp) {
						mdmPurgeDate = mdmGroupPurgeDateMap.get(user.getAtg());
						user.setMdmPurgeDate(mdmPurgeDate);
						userUpdated = true;
					}
					
					// set indicator if not set
					if(!IMConstant.USER_NOTIFICATION_IND_LIST.contains(user.getMdmPurgeNotificationInd())) {
						user.setMdmPurgeNotificationInd(IMConstant.USER_NOTIFICATION_IND_ZERO);
						userUpdated = true;
					}
					
					// purge 
					if(mdmPurgeDate != null) {
						String prefix = "Your IntelliMatch MDM Service ";
						if(startDate.after(mdmPurgeDate) && IMConstant.USER_NOTIFICATION_IND_ZERO.equals(user.getMdmPurgeNotificationInd())) {
							
							// set notification for close to Purged
							String subject = prefix + " is about to purge on " + simpleDateFormat.format(mdmPurgeDate);
							String text = subject + "\n" ;
							emailService.create(subject, text, user.getUsername());

							// update user
							user.setMdmPurgeNotificationInd(IMConstant.USER_NOTIFICATION_IND_FIRST);
							userUpdated = true;
						} else if(now.after(mdmPurgeDate) && IMConstant.USER_NOTIFICATION_IND_FIRST.equals(user.getMdmPurgeNotificationInd())) {
							
							boolean isDataPurgedInIIS = jobService.isDataPurgedInIIS(user.getAtg());
							boolean isFileExist = fileService.isMdmDataExist(user.getUsername());
							
							if(!isDataPurgedInIIS && isFileExist) {
								// send email notification
								// set notification for close to Purged
								String subject = "Your matching file is not purged from IIS server." ;
								String text = subject + "\n Please ask your group admin user to purge them all." ;
								emailService.create(subject, text, user.getUsername());
								
							}else if(user.getType().contains(IMConstant.IMSERVICE_TYPE_MDM_ADMIN)) {
								// create a purge job to do the purge
								try {
									// only admin user trigger purge All
									// send notification for Purged
									String subject = prefix + " is purged on " + simpleDateFormat.format(now);
									String text = subject + "\n" ;
									emailService.create(subject, text, user.getUsername());
									
									// update user
									user.setMdmPurgeNotificationInd(IMConstant.USER_NOTIFICATION_IND_SECOND);
									userUpdated = true;

									jobService.create(IMConstant.JOB_COMMAND_PURGE, user.getUsername());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}else {
								//	// send notification for Purged
								//	String subject = prefix + " is purged on " + simpleDateFormat.format(now);
								//	String text = subject + "\n" ;
								//	emailService.create(subject, text, user.getUsername());
								//	
								//	// update user
								//	user.setMdmPurgeNotificationInd(IMConstant.USER_NOTIFICATION_IND_SECOND);
								//	userUpdated = true;
								//	
								//	// purge file from mdm and file table
								//	fileService.purgeMdmData(user.getUsername());
								//	jobService.deleteAllByUser(user.getUsername());	
								
							}
						} 
					}
				}
			
				// update user status when both Purged
				if(	(!isAwsUser || IMConstant.USER_NOTIFICATION_IND_SECOND.equals(user.getAwsPurgeNotificationInd())) 
						&& 
					(!isMdmUser || IMConstant.USER_NOTIFICATION_IND_SECOND.equals(user.getMdmPurgeNotificationInd()))
				) {
					// send notification for Purged
					String prefix = "Your IntelliMatch Account";
					String subject = prefix + " is Purged on " + simpleDateFormat.format(now);
					String text = subject + "\n" ;
					emailService.create(subject, text, user.getUsername());

					// update user
					user.setStatus(IMConstant.USER_STATUS_INACTIVE);
					userUpdated = true;
				}
				
				// add updated users
				if(userUpdated) {
					user.setUpdatedDate(new Date());
					updatedUsers.add(user);
				}
			}
			
			// update to db
			if(!updatedUsers.isEmpty()) {
				userReposity.saveAll(updatedUsers);
			}
		}
	}
	
    /**
     * Validate user login attempt.
     * <pre>
     *  1. User can login if login within max attempt                          
     *  2. User will be locked when exceed max attempt                           
     *  3. User will be unlocked after specified time.              
     *  4. Attempt will be reset to 1 if login successfully.
     * </pre>
     * 
     *  @param username login username.
     *  @throws Exception when user not found or user login locked due to max attempt. 
     * 
     */
	public void validateAttempt(String username) throws Exception{
		IMUser user = userReposity.findByUsername(username);
		if(user != null) {
			if(user.getAttempt() == null) {
				// init attempt 
				user.setAttempt(1);
			}else if( user.getAttempt() > attemptMax ) {
				logger.debug(username + " attempted to login " + user.getAttempt() + " times, exceed max attempt : " + attemptMax);
				
				// check how long has been past
				Date updatedTime = user.getUpdatedDate();
				Date nowDate = new Date();
			    long diffInMillies = Math.abs(nowDate.getTime() - updatedTime.getTime());
			    long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
			    
			    if(diff < resetMinutes) {
			    	// within time range
					String err = "User account was locked due to too many attempts : " + attemptMax;
					logger.error(err);
					throw new Exception(err);
			    }else {
			    	// reset 
			    	user.setAttempt(1);
			    }
			}else {
				// inc
				user.setAttempt(user.getAttempt() + 1);
			}
			user.setUpdatedDate(new Date());
			userReposity.save(user);
		}else {
			String err = "User can not found : " + username;
			logger.error(err);
			throw new Exception("User can not found.");
		}
		
	}
	
    /**
     * Reset user attempt.
     * 
     * @param username login username.
     * @throws Exception when user not found. 
     * 
     */
	public void resetAttempt(String username) throws Exception{
		IMUser user = userReposity.findByUsername(username);
		if(user != null && user.getAttempt() != 1) {
	    	// reset 
	    	user.setAttempt(1);
	    	userReposity.save(user);
		}
	}

    /**
     * Returns email list for access token group.
     * 
     * @param atg access token group code.
     * @param excludeEmail exclude this email address from return email list
     * 
     * @return Returns email list for access token group.
     */
	public List<String> getAtgEmailList(String atg, String excludeEmail) {
		List<String> emails = new ArrayList<String>();

		List<IMUser> users = userReposity.findAllByAtg(atg);
		if(users != null && !users.isEmpty()) {
			for(IMUser user : users) {
				String email = user.getEmail();
				if(email != null && !email.isEmpty() && !emails.contains(email) && !email.equals(excludeEmail)) {
					emails.add(email);
				}
			}
		}
		return emails;
	}
}
