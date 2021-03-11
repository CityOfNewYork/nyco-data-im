/**
 * 
 */
package gov.nyc.nyco.intellimatch.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="CCISERVICE_USERS")
public class IMUser
{

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "CCISERVICE_USERS_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "CCISERVICE_USERS_SEQ")
	private Integer id;
	
	@Column(unique=true, columnDefinition = "varchar2(30) ")
	private String username;
	
	@Column(columnDefinition = "varchar2(256) ")
	private String password;

	@Column(name = "CREATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date createdDate ;

	@Column(name = "UPDATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date updatedDate;

	@Column(columnDefinition = "varchar2(30) ")
	private String createdBy ;	

	// START_DT  => awsStartDate, mdmStartDate
	// EXPIRE_DT => mdmStartDate, mdmExpireDate
	private Integer attempt;
	
	@Column(columnDefinition = "varchar2(255) ")
	private String email;
	@Column(columnDefinition = "varchar2(55) ")
	private String lastName;
	@Column(columnDefinition = "varchar2(55) ")
	private String firstName;

	@Column(columnDefinition = "char(1) default '0'")
	private String reportInd;
	@Column(columnDefinition = "varchar2(100) ")
	private String type;			// Admin, FileProvider 
									// AWS, MDM, MDM-ADMIN, MDM-FILE, MDM-REPORT
	
	/*
	 * New fields for new intelliMatch
	 * 
	 */
	@Column(columnDefinition = "varchar2(20)")
	private String status;
	
	@Column(columnDefinition = "varchar2(10) default '100MB'")
	private String mdmSizeLimit;	// default 100 MB
	@Column(columnDefinition = "varchar2(10) default '100MB'")
	private String awsSizeLimit;	// default 100 MB
	
	@Column(columnDefinition = "varchar2(30)")
	private String imUsage;			// Reg IM, WorkforceMatch, AWSTableLoad
	@Column(columnDefinition = "varchar2(255)")
	private String reportAccess; 	// Admin, ReportUser
	
	@Column(columnDefinition = "varchar2(100)")
	private String awsUserid;		// Ex. AROAYJ2M4B2JUY7DZLCHY:hxia@csc.nyc.gov

	@Column(name = "AWS_DB_NAME", columnDefinition = "varchar2(30)")
	private String dbName;			// database name for in aws glue

	@Column(name = "ACCESS_TOKEN_CD", columnDefinition = "varchar2(10)")
	private String at;				// Access token

	@Column(name = "MATCHING_ALGO", columnDefinition = "varchar2(10)")
	private String algo;			// Access algo
	
	@Column(name = "MATCHING_EID_GROUP", columnDefinition = "varchar2(10)")
	private String atg;				// Access token Group

	/**
	 * User can login for AWS service
	 * 1. Use granted for AWS service
	 * 2. User is in Active status
	 * 3. Current Date is between AWS_START_DT and AWS_EXPIRE_DT
	 * 
	 * User data will be purged when
	 * 1. User is expired 
	 * 2. AWS_FILE_DEL_IND = 1 and on AWS_PURGE_DT , or last user of same group
	 * 
	 */
	//	1) different expire date for ams and MDM
	@Column(name = "AWS_START_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date awsStartDate;
	@Column(name = "AWS_EXPIRE_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date awsExpireDate;
	
	//	2) delete aws file by group/indivisual, 1: will delete on purge date, 0 : will delete on last person purge date 
	@Column(name = "AWS_FILE_DEL_IND", columnDefinition = "char(1) default '0'")
	private String awsFileDelInd;
	
	//  3) schedule to terminate user, place terminate file by schedule
	@Column(name = "AWS_PURGE_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date awsPurgeDate;		// Data will be deleted at this date

	
	/**
	 * User can login for MDM service
	 * 1. Use granted for MDM service
	 * 2. User is in Active status
	 * 3. Current Date is between MDM_START_DT and MDM_EXPIRE_DT
	 * 
	 * User data will be purged when
	 * 1. User is expired 
	 * 2. MDM_FILE_DEL_IND = 1 and on MDM_PURGE_DT , or last user of same group
	 * 
	 */
	@Column(name = "MDM_START_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date mdmStartDate;
	@Column(name = "MDM_EXPIRE_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date mdmExpireDate;
	@Column(name = "MDM_PURGE_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date mdmPurgeDate;
	
	// Delete mdm file by group/indivisual, 1: will delete at purge date, 0 : will delete at admin purge date of same atg.
	@Column(name = "MDM_FILE_DEL_IND", columnDefinition = "char(1) default '0'")
	private String mdmFileDelInd;

	// Expire notification
	@Column(columnDefinition = "char(1) default '0'")
	private String awsExpireNotificationInd;
	@Column(columnDefinition = "char(1) default '0'")
	private String mdmExpireNotificationInd;
	
	// Purge notification
	@Column(columnDefinition = "char(1) default '0'")
	private String awsPurgeNotificationInd;
	@Column(columnDefinition = "char(1) default '0'")
	private String mdmPurgeNotificationInd;
	
	@Column(columnDefinition = "varchar2(100)")
	private String department;
	
	@Column(columnDefinition = "varchar2(100)")
	private String project;

	// define max number of files user can upload for MDM service
	private Integer maxPorts;
	
	@Column(columnDefinition = "varchar2(30)")
	private String outputAxwayId;
	
	
	public IMUser() {}
		
}
