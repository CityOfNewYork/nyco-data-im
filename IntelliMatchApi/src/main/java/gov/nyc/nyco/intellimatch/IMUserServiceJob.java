package gov.nyc.nyco.intellimatch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nyc.nyco.intellimatch.service.IMAuditLogService;
import gov.nyc.nyco.intellimatch.service.IMEmailService;
import gov.nyc.nyco.intellimatch.service.IMFileService;
import gov.nyc.nyco.intellimatch.service.IMUserService;

/**
 * Execute following user manage tasks, 
 * <pre>
 * 	User expire
 *  User Purge
 *  Password encryption
 *  Email notifcation
 *  AWS updated file status update
 *  </pre>
 */
@Component
@Scope("prototype")
public class IMUserServiceJob extends Thread{
	final static IMAuditLogService logger = new IMAuditLogService(IMUserServiceJob.class);
	
	private ApplicationContext applicationContext;
	private int sleepSeconds ;
	private int lazySleeptime ;
	private boolean dailyHourlyEnabled;
	
	public void setDailyHourlyEnabled(boolean dailyHourlyEnabled) {
		this.dailyHourlyEnabled = dailyHourlyEnabled;
	}

	private static boolean isRunning = true;
    /**
     * @return Check thread status, returns job status if running or terminated
     */
	public static String hello() {
		return IMUserServiceJob.class.getSimpleName() + (isRunning?" is running ":" is terminated");
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	public void setSleepSeconds(int sleeptime) {
		this.sleepSeconds = sleeptime;
	}
	public void setLazySleepSeconds(int lazySleeptime) {
		this.lazySleeptime = lazySleeptime;
	}
	
	
	
    @Override
    /**
     * Execute user tasks, then sleep at regular interval if job returns true, otherwise sleep at lazy mode.
     * By setting mdmservice.job.daily.hourly.enabled, it is able to run user expire, user purge task daily,
     * and run password encryption hourly.
     *   
     * Exit if exception happens.
     * 
     */
    public void run() {
		String pattern = "yyyy-MM-dd HH";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);   	
		String userServiceExecutedDay = "";
		String userServiceExecutedHour = "";
		
    	while(isRunning) {
    		String thisHour = simpleDateFormat.format(new Date());
    		String today = thisHour.substring(0, 10);
    		
    		// stand out only
            System.out.println(getName() + " is running");

    		// Start the clock
    		long start = System.currentTimeMillis();

    		try {
				// only run once a day
				if( !userServiceExecutedDay.equals(today) || !dailyHourlyEnabled) {
					logger.debug("Running Expire/Purge job at : " + today);
					
					// service
					IMUserService userService =  (IMUserService)applicationContext.getAutowireCapableBeanFactory().getBean("IMUserService");
					userService.expire();
					userService.purge();
					userServiceExecutedDay = today;
					
					// to split job running between job running
					Thread.sleep(1000*3);
				}
    			
    			// only run hourly TODO
    			if(!userServiceExecutedHour.equals(thisHour) || !dailyHourlyEnabled) {
    				logger.debug("Running Password Encryttion job at :" + thisHour);
    				
        			// service
        			IMUserService userService =  (IMUserService)applicationContext.getAutowireCapableBeanFactory().getBean("IMUserService");
        			userService.encryptPassword();
        			userServiceExecutedHour = thisHour;
        			
        			// to split job running between job running
        			Thread.sleep(1000*3);
    			}
    			
    			// Email service
            	IMEmailService emailService =  (IMEmailService)applicationContext.getAutowireCapableBeanFactory().getBean("IMEmailService");
    			boolean rc = emailService.sendEmail();
            	//logger.info("Elapsed time: Email service " + (System.currentTimeMillis() - start));
            	
    			// to split job running between job running
    			Thread.sleep(1000*3);
            	
    			// AWS file Service
            	IMFileService fileService =  (IMFileService)applicationContext.getAutowireCapableBeanFactory().getBean("IMFileService");
            	rc = rc || fileService.updateAwsFileStatus();
            	logger.debug("Elapsed time AWS file Service: " + (System.currentTimeMillis() - start));

            	// sleep more if nothing todo
            	Thread.sleep(1000*(rc?sleepSeconds:lazySleeptime));
    		} catch (Exception e) {
    			logger.error(getName() + " job ended due to" + e.toString());
                e.printStackTrace();
    	    	
    	    	// exit thread
    	    	isRunning = false;
    	    }
    	}
    }

}


