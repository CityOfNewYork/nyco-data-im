package gov.nyc.nyco.intellimatch.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.qos.logback.classic.Level;
import gov.nyc.nyco.intellimatch.models.IMAuditLog;
import gov.nyc.nyco.intellimatch.repository.IMAuditLogRepository;

/**
 * Insert log into audit table and audit log file.
 * 
 */
public class IMAuditLogService {
	private Logger logger = null;

	IMAuditLogRepository auditRepo;
	
	private static ApplicationContext context;

	public IMAuditLogService(Class<?> clazz) {
		logger = LoggerFactory.getLogger(clazz);
	}
	
	public static void setApplicationContext(ApplicationContext ctx) {
		context = ctx;
	}
	
    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     * @since 1.4
     */
    public void trace(String msg) {
    	String lvl = Level.TRACE.levelStr;
    	logger.trace(msg);
    	create(lvl, msg);
    }
    public void trace(String msg, List<String> addlInfoList) {
    	String lvl = Level.TRACE.levelStr;
    	logger.trace(msg);
    	create(lvl, msg, addlInfoList);
    }
    
    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    public void debug(String msg) {
    	String lvl = Level.DEBUG.levelStr;
    	logger.debug(msg);
    	create(lvl, msg);
    }
    public void debug(String msg, List<String> addlInfoList) {
    	String lvl = Level.DEBUG.levelStr;
    	logger.debug(msg);
    	create(lvl, msg, addlInfoList);
    }
    
    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    public void info(String msg) {
    	String lvl = Level.INFO.levelStr;
    	logger.info(msg);
    	
    	create(lvl, msg);
    }
    public void info(String msg, List<String> addlInfoList) {
    	String lvl = Level.INFO.levelStr;
    	logger.info(msg);
    	
    	create(lvl, msg, addlInfoList);
    }
    
    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    public void warn(String msg) {
    	String lvl = Level.WARN.levelStr;
    	logger.warn(msg);
    	create(lvl, msg);
    }
    
    public void warn(String msg, List<String> addlInfoList) {
    	String lvl = Level.WARN.levelStr;
    	logger.warn(msg);
    	create(lvl, msg, addlInfoList);
    }
    
    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    public void error(String msg) {
    	String lvl = Level.ERROR.levelStr;
    	logger.error(msg);
    	create(lvl, msg);
    }
    
    /**
     * Log a message at the ERROR level with additional info.
     *
     * @param msg the message string to be logged
     * @param addlInfoList additional parameters pass to audit log
     */
    public void error(String msg, List<String> addlInfoList) {
    	String lvl = Level.ERROR.levelStr;
    	logger.error(msg);
    	create(lvl, msg, addlInfoList);
    }
    
	private void create(String logLevel, String message) {
		create(logLevel, message, null);
	}
	
	private void create(String logLevel, String message, List<String> addlInfoList) {
		String username = null;
		try {
			// check if username is available 
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			username = auth.getName();
		}catch(Exception e) {
			// get thread name
			username = Thread.currentThread().getName();
			// ignore
			// logger.info("username is null, use thread name " + username);
		}
		
		if(context != null && auditRepo == null) {
			auditRepo =  (IMAuditLogRepository)context.getAutowireCapableBeanFactory().getBean("IMAuditLogRepository");
		}
		
		if(auditRepo != null) {
			IMAuditLog audit = new IMAuditLog();
			
			audit.setUsername(username.substring(0, Math.min(50, username.length())));
			audit.setLogLevel(logLevel);

			String logMsg = logger.getName() +  " - " + message;
			// audit.setMessage(logMsg.getBytes());
			audit.setMessage(logMsg.substring(0, Math.min(2000, logMsg.length())));
			audit.setCreatedDate(new Date());
			
			// additional info for audit log
			if(addlInfoList != null && !addlInfoList.isEmpty()) {
				for(String info : addlInfoList) {
				    try {
				        // match run
				    	int run = Integer.parseInt(info);
				        audit.setMatchRun(run);
				    } catch (NumberFormatException nfe) {
				    	audit.setAtg(info);
				    }		
				}
			}
			
			
			auditRepo.save(audit);
		}else {
			logger.info("auditRepo is not initialized. Audit log skipped.");
		}
	}

}
