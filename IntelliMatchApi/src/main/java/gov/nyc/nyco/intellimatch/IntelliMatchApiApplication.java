/*-
 * #%L
 * IntelliMatch V1.0
 * %%
 * Copyright (C) 2021 NYCO
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gov.nyc.nyco.intellimatch;

import java.util.Calendar;
import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import gov.nyc.nyco.intellimatch.models.IMAxwayReport;
import gov.nyc.nyco.intellimatch.models.IMUser;
import gov.nyc.nyco.intellimatch.repository.IMAxwayReportRepository;
import gov.nyc.nyco.intellimatch.repository.IMUserRepository;
import gov.nyc.nyco.intellimatch.service.IMAuditLogService;


/**
 * IntelliMatch Rest API Main
 */
@Configuration
@SpringBootApplication
@ComponentScan("gov.nyc.nyco.intellimatch")
public class IntelliMatchApiApplication implements CommandLineRunner {
	@Autowired
	private IMUserRepository userRepo;
	
	@Autowired
	private IMAxwayReportRepository rptRepo;
	
	@Autowired
	private ApplicationContext applicationContext;
	
    @Value("${mdmservice.job.sleeptime}")
    private Integer sleeptime;

    @Value("${mdmservice.job.sleeptime.lazy}")
    private Integer lazySleeptime;

    @Value("${mdmservice.job.daily.hourly.enabled}")
    private boolean dailyHourlyEnabled;
    
    @Value("${mdmservice.job.match.enabled}")
    private boolean matchEnabled;
	
    @Value("${mdmservice.job.email.enabled}")
    private boolean emailEnabled;
    
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String createDDL ;

	
	public static void main(String[] args) {
		SpringApplication.run(IntelliMatchApiApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		// create test data only for test
		if("create".equals(createDDL)) {
			createTestUser();
			createTestReport();
		}

		startJobs();
	}
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**");
			}
		};
	}

	/**
	 * Start jobs.
	 */	
	public void startJobs() {
    	// Ingest content into logger
    	IMAuditLogService.setApplicationContext(applicationContext);
    	
    	// start mdm service job
		if(matchEnabled) {
	    	IMMdmServiceJob jobThread = new IMMdmServiceJob();
	    	jobThread.setName("IntelliMatch MDM Service Job");
	    	jobThread.setApplicationContext(applicationContext);
	    	jobThread.setSleepSeconds(sleeptime);
	    	jobThread.setLazySleepSeconds(lazySleeptime);
	    	jobThread.start();
		}
		
		// start user manage job
		if(emailEnabled) {
	    	IMUserServiceJob userThread = new IMUserServiceJob();
	    	userThread.setName("IntelliMatch User Service Job");
	    	userThread.setApplicationContext(applicationContext);
	    	userThread.setSleepSeconds(sleeptime);
	    	userThread.setLazySleepSeconds(lazySleeptime);
	    	userThread.setDailyHourlyEnabled(dailyHourlyEnabled);
	    	userThread.start();
		}
	}
	
	@Transactional
	public void createTestUser() {
		Date expireDate = new Date();
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(expireDate); 
		cal.add(Calendar.DATE, 20);
		expireDate = cal.getTime();
		cal.add(Calendar.DATE, -25);
		Date startDate = cal.getTime();
		
		IMUser u = null;

		u = new IMUser();
		u.setUsername("hxia");
		u.setPassword("PW:hxia");
		u.setStatus("Active");
		u.setAwsUserid("AROAYJ2M4B2JUY7DZLCHY:hxia@csc.nyc.gov");
		u.setDbName("dataresearcher");
		u.setAt("AT1");
		u.setAtg("ATG1");
		u.setAlgo("ALGO1");
		u.setReportInd("1");
		u.setEmail("hxia@doitt.nyc.gov");
		u.setFirstName("Howard");
		u.setLastName("Xia");
		u.setAwsStartDate(startDate);
		u.setMdmStartDate(startDate);
		u.setAwsExpireDate(expireDate);
		u.setMdmExpireDate(expireDate);
		u.setAwsPurgeDate(expireDate);
		u.setMdmPurgeDate(expireDate);
		u.setType("MDM-ADMIN,AWS");
		u.setProject("Project Covid-19");
		u.setDepartment("Department of Homeless Services");
		u.setMaxPorts(1);
		userRepo.save(u);

		u = new IMUser();
		u.setUsername("vmehta");
		u.setPassword("PW:vmehta");
		u.setStatus("Active");
		u.setAwsUserid("AROAYJ2M4B2JWSYVZCPWQ:vmehta@csc.nyc.gov");
		u.setDbName("dataanalyst");
		u.setAt("AT1");
		u.setAtg("ATG1");
		u.setReportInd("1");
		u.setEmail("vmehta@doitt.nyc.gov");
		u.setFirstName("Vishal");
		u.setLastName("Mehta");
		u.setAwsExpireDate(expireDate);
		u.setMdmExpireDate(expireDate);
		u.setAwsPurgeDate(expireDate);
		u.setMdmPurgeDate(expireDate);
		u.setProject("Project Covid-19");
		u.setDepartment("Department of Homeless Services");
		u.setType("MDM,AWS");

		userRepo.save(u);

		u = new IMUser();
		u.setUsername("sakola");
		u.setPassword("PW:sakola");
		u.setStatus("Active");
		u.setAwsUserid("AROAYJ2M4B2JWSYVZCPWQ:sakola@csc.nyc.gov");
		u.setDbName("dataanalyst");
		u.setAt("AT1");
		u.setAtg("ATG1");
		u.setReportInd("1");
		u.setEmail("sakola@doitt.nyc.gov");
		u.setFirstName("Saiteja");
		u.setLastName("Kola");
		u.setAwsExpireDate(expireDate);
		u.setMdmExpireDate(expireDate);
		u.setAwsPurgeDate(expireDate);
		u.setMdmPurgeDate(expireDate);
		u.setType("MDM,AWS");
		u.setProject("Covid-19");
		u.setDepartment("Department of Homeless Services");
		userRepo.save(u);
		
		u = new IMUser();
		u.setUsername("admin");
		u.setPassword("PW:admin");
		u.setStatus("Active");
		u.setAt("AT1");
		u.setAtg("ATG1");
		u.setReportInd("1");
		u.setEmail("hxia@doitt.nyc.gov");
		u.setFirstName("Admin");
		u.setLastName("NYCO");
		u.setType("MDM,AWS");
		u.setProject("Covid-19");
		u.setDepartment("Department of Homeless Services");
		userRepo.save(u);
	}
	
	@Transactional
	public void createTestReport() {
		IMAxwayReport r = null;

		r = new IMAxwayReport();
		r.setType("EID");
		r.setDepartment("Dept Homeless Services");
		r.setCreatedDate(new Date());
		r.setAt("AT1");
		r.setAtg("ATG1");

		r.setId(new Long(1));
		r.setRun(1);
		r.setName("eid_report001.csv");
		rptRepo.save(r);
		r.setId(new Long(2));
		r.setName("eid_report002.csv");
		rptRepo.save(r);
		
		r.setId(new Long(3));
		r.setRun(2);
		r.setName("eid_report001.csv");
		rptRepo.save(r);
		r.setId(new Long(4));
		r.setName("eid_report002.csv");
		rptRepo.save(r);
	}
	
}










