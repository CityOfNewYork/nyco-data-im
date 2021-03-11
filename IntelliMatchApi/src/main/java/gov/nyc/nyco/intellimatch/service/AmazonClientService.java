package gov.nyc.nyco.intellimatch.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import gov.nyc.nyco.intellimatch.models.IMUser;

/**
 * Amazon Client service for uploading file to S3.
 * 
 */
@Service
public class AmazonClientService {
	final static IMAuditLogService logger = new IMAuditLogService(AmazonClientService.class);

	private AmazonS3 s3client;
	
	@Autowired
	private IMUserService userService;

	@Value("${aws.endpointUrl}")
	private String endpointUrl;
	@Value("${aws.bucketName}")
	private String bucketName;
	@Value("${aws.region}")
	private String region;
	@Value("${aws.prefix}")
	private String prefix;

	@Value("${aws.userschema.catalog.prefix}")
	private String catalogPrefix;
	
	
	@Value("${aws.userschema.expired.prefix}")
	private String expiredPrefix;

	@Value("${aws.userschema.queryresult.prefix}")
	private String queryresultPrefix;

	@Value("${aws.userschema.tablename.withusername}")
	private boolean tablenameWithUsername;
	
	
	
	// AWS credentials
	@Value("${aws.access_key_id}")
	private String accessKey;
	@Value("${aws.secret_access_key}")
	private String secretKey;
	@Value("${aws.session_token}")
	private String sessionToken;

	@PostConstruct
	private void initializeAmazon() {
		logger.debug("Initialize Amazon Client ...");
		logger.debug("accessKey : " + this.accessKey);

		AWSCredentials credentials = null;

		if (sessionToken != null && sessionToken.length() > 0) {
			credentials = new BasicSessionCredentials(accessKey, secretKey, sessionToken);
		} else {
			credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		}

		this.s3client = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(region))
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}

	synchronized public String uploadFile(MultipartFile multipartFile, String username) throws Exception {
		File file = convertMultiPartToFile(multipartFile);
		
		String fileName = generateFileName(multipartFile, username);
		uploadFileTos3bucket(fileName, file);

		file.delete();

		logger.debug("File uploaded to " + fileName + " successully.");
		
		// Lambda function execution and concurrency restriction
		Thread.sleep(3000);
		return fileName;
	}
	
	synchronized public void deleteFile(String key) {
		deleteFileFromS3Bucket(key);
	} 

	public void sendExpiredFile(String awsUserId) {
		String key = expiredPrefix + "/" + awsUserId + ".expired";
		uploadFileTos3bucket(key, "This user is expired.");
	}
	
	public void sendQueryResultFile(String awsUserId) {
		String key = queryresultPrefix + "/" + awsUserId + "/dummy";
		uploadFileTos3bucket(key, "This is dummay file for creating query result folder.");
	}
	
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	private String generateFileName(MultipartFile multiPart, String username) {
		IMUser u = userService.findUserByUsername(username);
		
		String fname = multiPart.getOriginalFilename().replace(" ", "_");
		String tableName = getTableName(fname, username);
		
		if(tablenameWithUsername) {
			return prefix + "/" + u.getAwsUserid()  + "/" + u.getDbName()  + "/" + tableName + "/" + username + "_" + fname;
		}else {
			return prefix + "/" + u.getAwsUserid()  + "/" + u.getDbName()  + "/" + tableName + "/" + fname;
		}
	}
	
	public String getTableName(String fname, String username) {
		String tableName = fname.replaceFirst("[.][^.]+$", "");
		if(tablenameWithUsername) {
			tableName =  username + "_" + tableName;
		}
		return tableName;
	}

	private void uploadFileTos3bucket(String fileName, File file) {
		logger.debug("Upload file to S3 : " + fileName);
		s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
	}

	private void uploadFileTos3bucket(String fileName, String text) {
		logger.debug("Upload file to S3 : " + text);
		s3client.putObject(bucketName, fileName, text);
	}

	private void deleteFileFromS3Bucket(String fileName) {
		logger.debug("Delete file from S3: " + fileName);
		s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
	}

	public synchronized List<String> getAllTables(String dbName) {
		List<String> tables = new ArrayList<String>();
		
    	try {
    	    S3Object s3Object = s3client.getObject(bucketName, catalogPrefix + "/" + dbName);
    	    if (s3Object != null) {
    			    InputStream is = s3Object.getObjectContent();
    				BufferedReader br = new BufferedReader(new InputStreamReader(is));
    				for (String line = br.readLine(); line != null && !line.isEmpty(); line = br.readLine()) {
    					tables.add(line);
    				}
    				br.close();
    	    }else {
    	    	logger.warn("Failed to download table names for " + dbName);
    	    }
    	}catch(Exception e) {
    		logger.error("Failed to download table names for " + dbName);
    	}
	    
	    return tables;
	}
	
	public void sendTableCatalogRequestFile(String dbName) {
		String key = catalogPrefix + "/" + dbName + ".request";
		uploadFileTos3bucket(key, "Table list is required.");
	}
	
}
