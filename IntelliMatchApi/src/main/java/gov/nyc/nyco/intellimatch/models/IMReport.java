package gov.nyc.nyco.intellimatch.models;

import lombok.Data;

/*
 * Report from folder, current not been used
 * User Axway instead
 * 
 */

@Data
public class IMReport {
	private Integer agncyId;
	private String  path;
	private String  fname;
	private String  type;
	private Long size;
	private byte[] data;
}
