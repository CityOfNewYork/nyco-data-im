package gov.nyc.nyco.intellimatch.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.models.IMUser;
import gov.nyc.nyco.intellimatch.repository.IMUserRepository;

/**
 * JWT User service.
 * 
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {
	@Autowired
	private IMUserRepository userReposity;	
	
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
    	IMUser u = userReposity.findByUsername(s);
        if (u == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", s));
        } else {
        	return new User(u.getUsername(), u.getPassword(),
                    new ArrayList<>());
        }
    }

}