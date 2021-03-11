package gov.nyc.nyco.intellimatch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nyc.nyco.intellimatch.service.IMAuditLogService;
import gov.nyc.nyco.intellimatch.service.IMJobService;


/**
 * Execute mdm match job in IIS server though ssh connection at FIFO. This thread start from spring main application, 
 * only run one job at once, runs at predefined interval.
 */
@Component
@Scope("prototype")
public class IMMdmServiceJob extends Thread{
	final static IMAuditLogService logger = new IMAuditLogService(IMMdmServiceJob.class);
	
	private IMJobService jobService;
	private ApplicationContext applicationContext;
	private int sleepSeconds ;
	private int lazySleeptime ;

	private static boolean isRunning = true;
	
    /**
     * @return check thread status, returns job status if running or terminated
     * 
     */
	public static String hello() {
		return IMMdmServiceJob.class.getSimpleName() + (isRunning?" is running ":" is terminated");
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
	
    /**
     * Execute one job, then sleep at regular interval if job returns true, otherwise sleep at lazy mode.
     * 
     * Exit if exception happens.
     * 
     */
    @Override
    public void run() {
    	while(isRunning) {
            System.out.println(getName() + " is running");

    		// Start the clock
    		long start = System.currentTimeMillis();

    		try {
    			// service
    			jobService =  (IMJobService)applicationContext.getAutowireCapableBeanFactory().getBean("IMJobService");
    			boolean rc = jobService.processNextJob();
            	logger.debug("Elapsed time: " + (System.currentTimeMillis() - start));
            	
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


