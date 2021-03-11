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
@Table(name="CCISERVICE_EMAIL")
public class IMEmail {

	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "CCISERVICE_EMAIL_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "CCISERVICE_EMAIL_SEQ")
	private Long id;
	
	@Column(columnDefinition = "VARCHAR2(100)")
	private String subject;
	@Column(columnDefinition = "VARCHAR2(100)")
	private String toEmail;
	@Column(columnDefinition = "VARCHAR2(100)")
	private String frEmail;
	@Column(columnDefinition = "VARCHAR2(100)")
	private String ccEmail;
	
	@Column(columnDefinition = "VARCHAR2(2000)")
	private String message;
	
	@Column(columnDefinition = "char(1) default '0'")
	private String sentInd;
	
	@Column(name = "CREATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date createdDate ;

	@Column(name = "UPDATED_DT", columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date updatedDate;
	
}
