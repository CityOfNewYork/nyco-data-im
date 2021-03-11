package gov.nyc.nyco.intellimatch.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import gov.nyc.nyco.intellimatch.models.IMReport;

/**
 * SSH service, connect to IIS server, execute command on IIS server.
 * 
 */
@Service
public class SSHClientService {

	final static IMAuditLogService logger = new IMAuditLogService(SSHClientService.class);
	
    @Value("${ssh.host}")
    private String sftpHost;

    @Value("${ssh.port:22}")
    private int sftpPort;

    @Value("${ssh.user}")
    private String sftpUser;

    @Value("${ssh.privateKey:#{null}}")
    private Resource sftpPrivateKey;

    @Value("${ssh.privateKeyPassphrase:}")
    private String sftpPrivateKeyPassphrase;

    @Value("${ssh.password:#{null}}")
    private String sftpPasword;
    
	@Value("${mdmservice.enviroment.iis_inpath}")
	private String iisInPath;
    
    private Session session = null;

	
    public SSHClientService() {
    }
    
    public void connect() throws JSchException {
    	logger.debug("Connecting to sftpHost : " + sftpHost);
    	logger.debug("sftpPort : " + sftpPort);
    	logger.debug("sftpUser : " + sftpUser);

    	JSch jsch = new JSch();

        session = jsch.getSession(sftpUser, sftpHost, sftpPort);
        session.setPassword(sftpPasword);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
    }
    
    public void upload(InputStream source, String fname, String path) throws JSchException, SftpException {
    	// create if path not exist ignore exception
    	logger.debug("path : " + path);
    	createDir(path);
    	 
    	connect();
        ///////////////////////////////////////////////////////////////////////////
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.cd(path);
        sftpChannel.put(source, fname);
        sftpChannel.exit();
        ///////////////////////////////////////////////////////////////////////////
        disconnect();
    }

    public void upload(byte[] source, String fname, String path) throws JSchException, SftpException {
    	upload(new ByteArrayInputStream(source), fname, path);
    }

    public void upload(MultipartFile file, String fname, String path) throws JSchException, SftpException, IOException {
    	upload(new ByteArrayInputStream(file.getBytes()), fname, path);
    }
    
    public String download(String fullpath) throws JSchException, SftpException, IOException {
    	connect();
    	
    	StringBuffer sb = new StringBuffer();
    	
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        // read file
        InputStream is = sftpChannel.get(fullpath);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			sb.append(line + "\n");
		}
		br.close();
		
        sftpChannel.exit();
        disconnect();
        
        return sb.toString();
    }
    
    public void delete(String path) throws JSchException, SftpException {
    	connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.rm(path);
        sftpChannel.exit();
        disconnect();
    }
    
    public void createDir(String path) {
    	String dir = path;
    	try {
    		if(dir.startsWith("/C:")) {
    			dir = dir.replace("/C:", "C:");
    			dir = dir.replaceAll("/", "\\\\");
    		}
    		
			execCommand("mkdir -p " + dir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    synchronized public List<String> execCommand(String cmd)  throws JSchException, IOException {
    	StringBuffer sbResult = new StringBuffer();
    	
    	// set System.err to memory string
    	OutputStream errOs = new ByteArrayOutputStream(1024);
    	PrintStream printOut = new PrintStream(errOs);
    	System.setErr(printOut);
    	
    	// connect
    	connect();
    	
    	// open chhannel for command
    	Channel channel=session.openChannel("exec");
        ((ChannelExec)channel).setCommand(cmd);
        channel.setInputStream(null);
        ((ChannelExec)channel).setErrStream(System.err);
    	
        // open input stream and execute command
        InputStream in=channel.getInputStream();
        channel.connect();
        byte[] tmp=new byte[1024];
        while(true){
        	while(in.available()>0){
        		int i=in.read(tmp, 0, 1024);
        		if(i<0)break;
        		
        		String str = new String(tmp, 0, i);
        		//System.out.print(str);
        		
        		logger.debug(str);
        		sbResult.append(str);
        	}
        	if(channel.isClosed()){
        		//System.out.println("exit-status: "+channel.getExitStatus());
        		logger.debug("exit-status : " + channel.getExitStatus());
        		break;
        	}
        	try{Thread.sleep(1000);}catch(Exception ee){}
        }
    	
        channel.disconnect();
        disconnect();
        
    	System.err.flush();
    	System.err.close();
    	
        logger.debug("Job completed : " + cmd);
    	logger.debug("errOs : "+ errOs.toString());
    	logger.debug("sbResult.toString()  : "+ sbResult.toString());
    	
    	// collect output and error
    	List<String> result = new ArrayList<String>();
    	result.add(sbResult.toString());
    	result.add(errOs.toString());
    	
    	return result;
    }
    
    synchronized public List<IMReport> getFileList(String inPath, Integer agncyId, String name, boolean loadData) throws JSchException, SftpException, IOException{
    	List<IMReport> rpts = new ArrayList<IMReport>();
    	
    	String path = inPath + "/" + agncyId;
    	
    	logger.debug("list file at " + path);

    	if(name != null && !name.isEmpty()) {
    		path = path + "/" + name;
    	}
    	
        logger.debug("list file at " + path);

    	// connect
    	connect();
    	
    	Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
       
        @SuppressWarnings("rawtypes")
		Vector fileList = sftpChannel.ls(path);
        for (int i = 0; i < fileList.size(); i++) {
            ChannelSftp.LsEntry lsEntry = (ChannelSftp.LsEntry) fileList.get(i);
            String fname = lsEntry.getFilename();
            if(!lsEntry.getAttrs().isDir()) {
            	IMReport rpt = new IMReport();
            	rpt.setAgncyId(agncyId);
            	rpt.setFname(fname);
            	rpt.setPath(path);
            	rpt.setSize(lsEntry.getAttrs().getSize());
            	
            	String mimeType = URLConnection.guessContentTypeFromName(fname);
            	rpt.setType(mimeType);
            	
            	if(loadData) {
            		StringBuffer sb = new StringBuffer();
            		String fullpath = path;
            		if(name == null || name.isEmpty()) {
            			fullpath = fullpath + "/" + fname;
            		}
            		
                    // read file
                    InputStream is = sftpChannel.get(fullpath);
            		BufferedReader br = new BufferedReader(new InputStreamReader(is));
            		for (String line = br.readLine(); line != null; line = br.readLine()) {
            			sb.append(line + "\n");
            			logger.debug("line : " + line);
            		}
            		br.close();
            		rpt.setData(sb.toString().getBytes());
            	}
            	
            	rpts.add(rpt);
            	logger.debug("getFilename is file " + fname);
            	logger.debug("mimeType is  " + mimeType);
            }else {
            	logger.debug("getFilename is folder " + fname);
            }
        }
    	
        sftpChannel.exit();
        disconnect();
        
        return rpts;
        
    }
    
    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }
}


