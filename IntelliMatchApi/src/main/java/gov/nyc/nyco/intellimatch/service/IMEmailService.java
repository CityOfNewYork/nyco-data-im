package gov.nyc.nyco.intellimatch.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.models.IMEmail;
import gov.nyc.nyco.intellimatch.models.IMUser;
import gov.nyc.nyco.intellimatch.repository.IMEmailRepository;
import gov.nyc.nyco.intellimatch.repository.IMUserRepository;

/**
 * Email service, create email in email table.
 * 
 */
@Service
public class IMEmailService {

	final static IMAuditLogService logger = new IMAuditLogService(IMEmailService.class);
	
	@Autowired
	private IMUserRepository userRepo;
	
	@Autowired
	private IMEmailRepository emailRepo;
	
	@Autowired
	private EmailClientService emailService;
	
	
    @Value("${spring.mail.from}")
    private String from;

    @Value("${spring.mail.cc}")
    private String cc;
	
    @Value("${spring.mail.enabled}")
    private boolean enableEmail;
    
	public void create(String subject, String text, String username) {
		// skip email if disabled
		if(!enableEmail) return;
		
		IMEmail email = new IMEmail();
		
		IMUser user = userRepo.findByUsername(username);
		if(user != null && !user.getEmail().isEmpty()) {
			email.setSubject(subject);
			email.setMessage(text);
			email.setToEmail(user.getEmail());
			email.setCcEmail(cc);
			email.setFrEmail(from);
			email.setSentInd("0");
			email.setCreatedDate(new Date());
			email.setUpdatedDate(new Date());
			
			email = emailRepo.save(email);
			
			logger.debug("Email created : " + email.toString());
		}

	}
	
	public boolean sendEmail() throws Exception {
		boolean sts = false;
		
		Sort sort = Sort.by(new Order( Direction.ASC, "createdDate"));
		List<IMEmail> emails = emailRepo.findAllBySentInd("0", sort);
		
		if(emails != null && !emails.isEmpty()) {
			for(IMEmail email : emails) {
				sts = true;
				emailService.send(email);
				
				email.setSentInd("1");
				email.setUpdatedDate(new Date());
				
				email = emailRepo.save(email);
				
				Thread.sleep(1000*10);
			}
		}
		return sts;
	}
	
	
	
}
