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
@Table(name="CCISERVICE_AUDITLOG")
public class IMAuditLog {
	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "CCISERVICE_AUDITLOG_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "CCISERVICE_AUDITLOG_SEQ")
	private Integer id;
	
	@Column(columnDefinition = "varchar2(30)")
	private String username;
	
	@Column(name = "LOG_LEVEL ", columnDefinition = "varchar2(20)")
	private String logLevel; 

	@Column(columnDefinition = "VARCHAR2(2000)")
	private String message; 
	
	@Column(name = "CREATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date createdDate ;	
	
	@Column(name = "MATCH_RUN")		
	private Integer matchRun;		// 1,2...
	
	@Column(name = "MATCHING_EID_GROUP", columnDefinition = "varchar2(10)")
	private String atg;				// Access token Group
	
}
