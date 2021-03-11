package gov.nyc.nyco.intellimatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nyc.nyco.intellimatch.IMMdmServiceJob;
import gov.nyc.nyco.intellimatch.IMUserServiceJob;
import gov.nyc.nyco.intellimatch.constant.IMConstant;
import gov.nyc.nyco.intellimatch.models.AuthenticationRequest;
import gov.nyc.nyco.intellimatch.models.AuthenticationResponse;
import gov.nyc.nyco.intellimatch.service.IMAuditLogService;
import gov.nyc.nyco.intellimatch.service.IMUserService;
import gov.nyc.nyco.intellimatch.service.JwtUserDetailsService;
import gov.nyc.nyco.intellimatch.util.JwtUtil;

@RestController
public class AuthenticateController {
	final static IMAuditLogService logger = new IMAuditLogService(AuthenticateController.class);
	
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;
	
	@Autowired
	private IMUserService userService;	

	@Value("${spring.application.name}")
	private String appName;
	
	@RequestMapping({ "/web/hello" })
	public String hello() {
		return appName + " is running<br>" + IMMdmServiceJob.hello() + "<br>" + IMUserServiceJob.hello();
	}
	
	@RequestMapping({ "/api/logout" })
	public String logout() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth.getName();
		
		logger.info(username + " logged out.");
		return "Bye..." + username;
	}
	
	@RequestMapping(value = "/api/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
		logger.info(authenticationRequest.getUsername() + " login...");
		
		try {
			// update attempt
			userService.validateAttempt(authenticationRequest.getUsername());
			
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
			);
		}
		catch (BadCredentialsException e) {
			logger.error(authenticationRequest.getUsername() + " login failed.");
			logger.error(e.toString());
			e.printStackTrace();
			throw new Exception("Incorrect username or password", e);
		}


		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());

		// set default serviceType as MDM
		String serviceType = authenticationRequest.getServiceType();
		if(serviceType == null || serviceType.isEmpty()) {
			serviceType = IMConstant.IMSERVICE_TYPE_MDM;
		}
		
		final String jwt = jwtTokenUtil.generateToken(userDetails, serviceType);
		if(jwt == null || jwt.isEmpty()) {
			logger.error(authenticationRequest.getUsername() + " login failed. Due to expired or is inactive" );
			throw new Exception("User is expired or is inactive.");
		}

		logger.info(authenticationRequest.getUsername() + " login success.");
		
		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}

}
