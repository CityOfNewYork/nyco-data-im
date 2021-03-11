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
@Table(name="CCISERVICE_JOBS")
public class IMJob {
	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "CCISERVICE_JOBS_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "CCISERVICE_JOBS_SEQ")
	private Long id;
	
	@Column(unique=true, columnDefinition = "varchar2(30)")
	private String jobId;							// Job ID
	
	@Column(columnDefinition = "varchar2(30)")
	private String command;							// Command:LOAD,MATCH,PURGE,PRECCI
	
	@Column(columnDefinition = "varchar2(255)")
	private String template;						// Command script
	
	@Column(columnDefinition = "VARCHAR2(2000)")
	private String script;							// Job script
	
	@Column(columnDefinition = "varchar2(30)")
	private String status;							// Pending -> Running -> Completed/Failed.

	@Column(columnDefinition = "VARCHAR2(2000)")	
	private String message;							// job result if success
	@Column(columnDefinition = "VARCHAR2(2000)")	
	private String errorMessage;					// job error message if fail

	@Column(columnDefinition = "varchar2(30) ")
	private String username;						// Login User name
	@Column(name = "ACCESS_TOKEN_CD", columnDefinition = "varchar2(10)")
	private String at;								// Access token

	@Column(name = "MATCHING_ALGO", columnDefinition = "varchar2(10)")
	private String algo;							// Access algo
	
	@Column(name = "MATCHING_EID_GROUP", columnDefinition = "varchar2(10)")
	private String atg;								// Access token Group
	
	@Column(name = "AGNCY_ID")		
	private Integer agncyId;						// Port # : 1,2...	
	
	@Column(name = "CREATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date createdDate ;						// Job Create time
	
	@Column(name = "UPDATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date updatedDate;
	
	private Long elapsed;							// Job running time
	
	@Column(name = "MATCH_RUN")		
	private Integer matchRun;						// 1,2...	group by atg
	
	@Column(name = "FILE_ID")	
	private Long fileId;							// File ID
	
	@Column(name = "MDM_FILE_NAME", columnDefinition = "varchar2(50)")
	private String fname;							// file name for LOAD in IIS folder, null for AWS
													// AGNCY_1_20191001120000.dat
	
	public IMJob() {}


}
