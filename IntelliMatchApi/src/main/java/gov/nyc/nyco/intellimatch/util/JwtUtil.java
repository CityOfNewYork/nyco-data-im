package gov.nyc.nyco.intellimatch.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.constant.IMConstant;
import gov.nyc.nyco.intellimatch.service.IMAuditLogService;
import gov.nyc.nyco.intellimatch.service.IMUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT utility.
 * 
 */
@Service
public class JwtUtil {
	final static IMAuditLogService logger = new IMAuditLogService(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

	@Autowired
	private IMUserService userService;	
   
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails, String serviceType) {
        return createToken(userService.getClaims(userDetails.getUsername(), serviceType), userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
    	Date validity = new Date(System.currentTimeMillis() + expiration * 1000);
    	
    	// check claims
    	if(claims == null || claims.isEmpty()) {
    		String err = subject + " is not granted for service";
    		logger.error(err );
    		return null;
    	}
    	
    	// check if user is active
    	String status = (String)claims.get("status");
    	if(!IMConstant.USER_STATUS_ACTIVE.equals(status)) {
    		String err = subject + " is " + status;
    		logger.error(err );
    		return null;
    	}
    	
    	// check if started
    	Date startDate = (Date)claims.get("startDate");
    	if(startDate != null && startDate.compareTo(new Date()) > 0) {
    		validity = startDate;
    		String pattern = "yyyyMMddHHmmss";
    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    		String err = subject + " will be available on " + simpleDateFormat.format(startDate);
    		logger.error(err );
    		return null;
    	}

    	// check if expired
    	Date expireDate = (Date)claims.get("expireDate");
    	if(expireDate != null && expireDate.compareTo(validity) < 0) {
    		validity = expireDate;
    		String pattern = "yyyyMMddHHmmss";
    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    		String err = subject + " was expired on " + simpleDateFormat.format(expireDate);
    		logger.error(err );
    		return null;
    	}

    	// reset attempt when login success
    	try {
        	userService.resetAttempt((String)claims.get("username"));
    	}catch (Exception e) {
    		String err = "Failed to reset attempt.";
	    	logger.error(err + e.toString() );
	    	e.printStackTrace();
    	}
    	
    	return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secret).compact();	


    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
}