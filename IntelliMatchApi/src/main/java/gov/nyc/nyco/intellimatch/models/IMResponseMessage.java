package gov.nyc.nyco.intellimatch.models;

import lombok.Data;

@Data
public class IMResponseMessage {
	  private String message;

	  public IMResponseMessage(String message) {
	    this.message = message;
	  }
}
