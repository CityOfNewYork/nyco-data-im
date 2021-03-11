package gov.nyc.nyco.intellimatch.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;

@Entity
@Data
@Table( name="CCI_INTELLI_AGENCY_CODE",
		uniqueConstraints=@UniqueConstraint(columnNames = {"username", "orgAgncyName"})
	) 
public class IMAgencyCode {
	@Id
	
	@Column(unique=true)
	private Integer agncyId;
	@Column(unique=true)
	private String cciAgncyName;
	
	@Column(columnDefinition = "varchar2(50)")
	private String orgAgncyName;

	private String username;	
	@Column(name = "ACCESS_TOKEN_CD")
	private String at;				// Access token
	@Column(name = "MATCHING_EID_GROUP")
	private String atg;				// Access token Group
	@Column(name = "MATCHING_ALGO")
	private String algo;			// Access token Group
	
	public IMAgencyCode() {}
}
