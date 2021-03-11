package gov.nyc.nyco.intellimatch.service;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import gov.nyc.nyco.intellimatch.models.IMEmail;

/**
 * Email service for sending email.
 * 
 */
@Service
public class  EmailClientService {
	@Value("${spring.mail.host}")
    private String mailServerHost;

    @Value("${spring.mail.port}")
    private Integer mailServerPort;

    @Value("${spring.mail.username}")
    private String mailServerUsername;

    @Value("${spring.mail.password}")
    private String mailServerPassword;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean mailServerAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean mailServerStartTls;
    
    @Value("${spring.mail.properties.mail.smtp.ehlo}")
    private boolean mailServerEhlo;

    @Value("${spring.mail.properties.mail.debug}")
    private boolean mailDebug;
    
    
    @Autowired
    JavaMailSender gmailSender;
    
    private JavaMailSenderImpl mailSender = null;
    
    @PostConstruct
    private void init() {
        mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(mailServerHost);
        mailSender.setPort(mailServerPort);
        mailSender.setUsername(mailServerUsername);
        mailSender.setPassword(mailServerPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", mailServerAuth);
        props.put("mail.smtp.starttls.enable", mailServerStartTls);
        props.put("mail.smtp.ehlo", mailServerEhlo);
        props.put("mail.debug", mailDebug);

    }

    public void send(IMEmail email) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom(email.getFrEmail());
        message.setTo(email.getToEmail()); 
        
        if(email.getCcEmail() != null && !email.getCcEmail().isEmpty()) {
            message.setCc(email.getCcEmail()); 
        }
        
        message.setSubject(email.getSubject()); 
        message.setText(email.getMessage());
        mailSender.send(message);
    }

}


