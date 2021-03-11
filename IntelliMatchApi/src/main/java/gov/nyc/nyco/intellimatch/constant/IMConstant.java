package gov.nyc.nyco.intellimatch.constant;

import java.util.Arrays;
import java.util.List;

public class IMConstant {
	// Service type
	public static final String IMSERVICE_TYPE_AWS = "AWS";
	public static final String IMSERVICE_TYPE_MDM = "MDM";
	public static final String IMSERVICE_TYPE_MDM_ADMIN = "MDM-ADMIN";
	public static final String IMSERVICE_TYPE_MDM_FILE = "MDM-FILE";
	public static final String IMSERVICE_TYPE_MDM_REPORT = "MDM-REPORT";
	
	public static final String USER_STATUS_INACTIVE = "Inactive";
	public static final String USER_STATUS_ACTIVE = "Active";
	public static final String USER_STATUS_EXPIRED = "Expired";
	
	// File Status
	public static final String FILE_STATUS_FAIL = "Failed";
	public static final String FILE_STATUS_ANALYZING = "Analyzing";
	public static final String FILE_STATUS_READY = "Ready to Match";
	public static final String FILE_STATUS_MATCHING = "Matching";
	public static final String FILE_STATUS_COMPLETED = "Processed";
	public static final String FILE_STATUS_PENDING = "Pending";
	public static final String FILE_STATUS_CATALOGUED  = "Catalogued";
	
	
	// Job Status
	// Pending -> Inprocess -> Completed, or Pending -> Deleted.
	public static final String JOB_STATUS_PENDING = "Pending";
	public static final String JOB_STATUS_RUNNING = "Running";
	public static final String JOB_STATUS_COMPLETED = "Completed";
	public static final String JOB_STATUS_FAILED = "Failed";
	
	// Job Command
	public static final String JOB_COMMAND_LOAD = "LOAD";
	public static final String JOB_COMMAND_MATCH = "MATCH";
	public static final String JOB_COMMAND_PURGE = "PURGE";
	public static final String JOB_COMMAND_PRECCI = "PreCCI";
	public static final List<String> JOB_COMMAND_LIST = Arrays.asList(JOB_COMMAND_LOAD,JOB_COMMAND_MATCH,JOB_COMMAND_PURGE,JOB_COMMAND_PRECCI);
	
	public static final String USER_NOTIFICATION_IND_ZERO = "0";
	public static final String USER_NOTIFICATION_IND_FIRST = "1";
	public static final String USER_NOTIFICATION_IND_SECOND = "2";
	public static final List<String> USER_NOTIFICATION_IND_LIST = Arrays.asList(USER_NOTIFICATION_IND_ZERO, USER_NOTIFICATION_IND_FIRST, USER_NOTIFICATION_IND_SECOND );

	public static final String DATA_DEL_BY_GROUP = "0";
	public static final String DATA_DEL_BY_USER = "1";
	
	public static final String FILE_ERROR_VIEWED_IND_ONE = "1";
	public static final String FILE_ERROR_VIEWED_IND_ZERO = "0";
	
}
