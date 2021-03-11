package gov.nyc.nyco.intellimatch.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.bean.CsvToBeanBuilder;

import gov.nyc.nyco.intellimatch.models.IMDataRule;

/**
 * Validate uploaded mdm match file base on validating rules.
 * 
 */
@Service
public class IMDataRuleService {
	final static IMAuditLogService logger = new IMAuditLogService(IMDataRuleService.class);
	
    @Value("${mdmservice.data.columns}")
	private Integer data_columns;	

	private HashMap<Integer, IMDataRule> ruleMap = new HashMap<Integer, IMDataRule>();
	
    @Value("${mdmservice.rule.file}")
	private String ruleFile;
   
    @Value("${mdmservice.rule.delimiter}")
	private String ruleDelimiter;   
    
    @Value("${mdmservice.maxErrorCount}")
    private Integer maxErrorCount;
    
    @Value("${mdmservice.data.delimiter}")
    private String mdmdataDelimiter;

	@Value("${mdmservice.data.filename.reqex}")
	private String filenameRegex;
	
	public IMDataRuleService() {
		super();
	}
	
	@PostConstruct
	private void initRules() {
		try {
			logger.debug("Init rules..." + ruleFile);
			logger.debug("ruleDelimiter..." + ruleDelimiter);

			// Read file from resource
			InputStream is = IMDataRuleService.class.getResourceAsStream(ruleFile);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<IMDataRule> ruleList = new CsvToBeanBuilder(new InputStreamReader(is))
			            .withType(IMDataRule.class)
			            .withSeparator(ruleDelimiter.charAt(0))
			            .withSkipLines(1)
			            .build()
			            .parse();
			
			if(ruleList.size() == data_columns) {
				for(IMDataRule rule : ruleList) {
					ruleMap.put(rule.getFieldNumber(), rule);
				}
			}
			
		} catch (IllegalStateException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
	}

	public String validateFile(String fname, byte[] source)  {
		logger.debug("validateFile start... ");
	
		StringBuffer errorSb = new StringBuffer();
		StringBuffer warnSb = new StringBuffer();
		
		// validate file name 
		if(!fname.matches(filenameRegex)) {
			errorSb.append("Filename [" +  fname + "] does not match AGENCY>_<SYSTEM>_<YYYYMMDDHHMMSS>.dat") ;
		}else {

			try {
				
				if(ruleMap.isEmpty()) {
					initRules();
				}
				
				InputStream targetStream = new ByteArrayInputStream(source);
				BufferedReader br = new BufferedReader(new InputStreamReader(targetStream));
	
				int errCnt = 0;
				
				// loop for file
				int i = 0;
			    for (String line = br.readLine(); line != null && errCnt <= maxErrorCount ; line = br.readLine()) {
			    	String err = "";
			    	i++;
			    	
			    	// split line to column
			    	String[] lineArr = line.split(mdmdataDelimiter);
			    	
			    	int colNum = lineArr.length;
			    	
			    	// validate number of columns
			    	if(colNum == data_columns) {
			    		// Loop for column
			    		for(int col = 0; col < colNum; col++) {
			    			String val = lineArr[col];
			    			IMDataRule rule = ruleMap.get(col+1);
			    			if(rule != null) {
			    				err = rule.matches(val);
			    				if(err != null && !err.isEmpty()) {
						    		errCnt++;
						    		if(rule.getIsFieldRequired()!= null && !rule.getIsFieldRequired().isEmpty()){
							    		errorSb.append("Line " + i + " : " + err + "\n");
						    		}else {
							    		warnSb.append("Line " + i + " : " + err + "\n");
						    		}
			    				}
			    			}
			    			
			    			// validate SRC_SYS_ACRNYM at col = 6
			    			if(col == 6 && !fname.startsWith(val)) {
			    				err = "Source System Acronym " + val +  " does not match filename : " + fname; 
			    				errorSb.append("Line " + i + " : " + err + "\n");
			    			}
			    		}
			    	}else{
			    		err = "Line " + i + " : " + " has " + colNum + " columns," ;
			    		errCnt++;
			    	}
	
			        if (!err.isEmpty()) {
				        logger.debug("line..." + err);
			        	errorSb.append("Line " + i + " error : " + err + "\n");		
			        }
			    }
			}catch (IOException e) {
				errorSb.append("Exception happened : " + e.toString());
				logger.error(e.toString());
				e.printStackTrace();
			}
		}
	
		if(errorSb.length() > 0) {
			return "ERROR:" + errorSb.toString();
		}else if(warnSb.length() > 0){
			return "WARN:" + warnSb.toString();
		}else {
			return "";
		}
		
	}
	
}
