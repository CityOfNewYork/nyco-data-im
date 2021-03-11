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
@Table(name="CCISERVICE_AXWAY_REPORT")
public class IMAxwayReport {
	@Id
	@Column(name = "ID")
	@SequenceGenerator(name = "CCISERVICE_AXWAY_REPORT_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "CCISERVICE_AXWAY_REPORT_SEQ")
	private Long id;
	
	private String name;			// report name
	private String type;			// report type
	private String department;      // department

	@Column(columnDefinition = "TIMESTAMP(6) default systimestamp")
	private Date createdDate ;		// Job Create time
	private Integer run ;			// run order: Match 1,2...
	
	@Column(name = "ACCESS_TOKEN_CD")
	private String at;				// Access token
	@Column(name = "MATCHING_EID_GROUP")
	private String atg;				// Access token Group
	@Column(name = "MATCHING_ALGO")
	private String algo;			// Matching algo
	
	@Column(columnDefinition = "varchar2(30)")
	private String outputAxwayId;
	
	public IMAxwayReport() {}


}
