package gov.nyc.nyco.intellimatch.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class AuthenticationRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	private String username;
    private String password;
    private String serviceType;


    //need default constructor for JSON Parsing
    public AuthenticationRequest()
    {

    }

    public AuthenticationRequest(String username, String password) {
        this.setUsername(username);
        this.setPassword(password);
    }


}
