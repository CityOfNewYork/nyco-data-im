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
@Table(name="CCISERVICE_FILES")
public class IMFile {
	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "CCISERVICE_FILES_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "CCISERVICE_FILES_SEQ")
	private Long id;
	
	// Source file info
	@Column(name = "FILE_NAME", columnDefinition = "varchar2(100)")
	private String name;			// Original file name
	@Column(name = "FILE_TYPE", columnDefinition = "varchar2(100)")
	private String type;			// original file type
	
	@Column(name = "SERVICE_TYPE", columnDefinition = "varchar2(50)")
	private String serviceType;		// MDM or AWS
	private Long fsize;				// file size
	private Long lines;				// file line count

	// IIS file info
	@Column(name = "FULL_PATH", columnDefinition = "varchar2(255)")
	private String path;			// for file folder for MDM, key(path and file name) for AWS 
	
	@Column(name = "MDM_FILE_NAME", columnDefinition = "varchar2(50)")
	private String fname;			// file name save to IIS folder, null for AWS
									// AGNCY_1_20191001120000.dat
	
	@Column(name = "AGNCY_ID")		
	private Integer agncyId;		// Port # : 1,2...
	
	@Column(columnDefinition = "varchar2(50)")
	private String orgAgncyName;	// for MDM, HRA_WMS, null for AWS	

	@Column(columnDefinition = "varchar2(50)")
	private String status;			// status 
	
	@Column(columnDefinition = "varchar2(30)")
	private String username;		// login username
	@Column(name = "ACCESS_TOKEN_CD", columnDefinition = "varchar2(10)")
	private String at;				// Access token

	@Column(name = "MATCHING_ALGO", columnDefinition = "varchar2(10)")
	private String algo;			// Access algo
	
	@Column(name = "MATCHING_EID_GROUP", columnDefinition = "varchar2(10)")
	private String atg;				// Access token Group
	
	@Column(name = "AWS_DB_NAME", columnDefinition = "varchar2(30)")
	private String dbName;			// database name for in aws glue
	@Column(name = "AWS_TABLE_NAME", columnDefinition = "varchar2(100)")
	private String tableName;		// table name for in aws glue
	
	
	@Column(columnDefinition = "VARCHAR2(2000)")
	private String errorMessage;
	
	@Column(name = "CREATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date createdDate ;		// Upload datetime
	private Long elapsed;			// Milliseconds for upload, validation and save to IIS folder 
	
	@Column(name = "UPDATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date updatedDate;
	
	@Column(columnDefinition = "varchar2(100)")
	private String department;

	@Column(name = "MATCH_RUN")		
	private Integer matchRun;		// 1,2...	
	
	@Column(columnDefinition = "char(1) default '0'")
	private String errorViewedInd;	// Will not show to user if indicator was set to 1
	
	private Integer mdmMatchRecords;
	private Integer mdmRejectRecords;
	private Integer mdmDupedRecords;
	
	public IMFile() {}

}
