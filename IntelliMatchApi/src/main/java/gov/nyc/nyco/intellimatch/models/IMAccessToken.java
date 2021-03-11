package gov.nyc.nyco.intellimatch.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table( name="ACCESS_TOKEN") 
public class IMAccessToken {
	@Id
	
	@Column(unique=true, name = "ADMIN_SYS_TP_CD", columnDefinition = "NUMBER")
	private Long adminSysTpCd;
	
	@Column(name = "DESCRIPTION", columnDefinition = "varchar2(50)")
	private String orgAgncyName;

	@Column(name = "ACCESS_TOKEN_CD", columnDefinition = "varchar2(10)")
	private String at;				// Access token

	@Column(name = "MATCHING_ALGO", columnDefinition = "varchar2(10)")
	private String algo;			// Access algo
	
	@Column(name = "MATCHING_EID_GROUP", columnDefinition = "varchar2(10)")
	private String atg;				// Access token Group

	@Column(name = "CREATED_DT", columnDefinition = "Date")
	private Date createdDate ;		// Create datetime
	
	@Column(name = "LAST_UPDATE_DT", columnDefinition = "TIMESTAMP(6)")
	private Date updatedDate ;		// last updated datetime	
	
	@Column(name = "RANKING", columnDefinition = "NUMBER(38,0)")
	private Long ranking;

	public IMAccessToken() {}

}
