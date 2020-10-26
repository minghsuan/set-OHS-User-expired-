
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/***@author vanish
 *  @since 2020.05.08
 *  @serial clean the OHS's expired Accounts. 
 *  */
public class Passwd {
	public static String FILE_NAME ="D:/JobTask/password.file";
	public static String FILE_LOCATION ="D:/Vanish/weblogic/Account Inventory/";
	public static String FILE_USER_NAME ="D:/JobTask/PwdExpired.txt";
	public static String FILE_EXE ="C:/Users/XXXX/Desktop/httpd-2.4.43-win64-VS16/Apache24/bin";
	public static String FILE_LOG4J="D:/JobTask/log4j.properties";
	public static final String ENCODING ="UTF-8";
	
	static LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);

	private static File file = new File(FILE_LOG4J);
	
	private static HashMap<String,String> userMpExpired = new HashMap<String,String>();
	
	static {
		context.setConfigLocation(file.toURI());
	}
	private static final Logger logger =  LogManager.getLogger(Passwd.class);
	public static void main(String[] args) {
		
		try {
		
		  initPropValues();	
		  readUserExpired();
		  String[] files = FILE_NAME.split(";");
		  
		  for(String fileName : files){
			  fileName = FILE_LOCATION+fileName;
			  updateBUUser(fileName);
		  }
		  
		  writeToUserExpired();
		
		}catch (IOException e) {
			logger.error(e);
		  
		} finally {
			
		}

	}
	
	public static void updateBUUser(String FileName){

		BufferedReader reader = null;
		try {
		  reader = new BufferedReader(new InputStreamReader(new FileInputStream(FileName), ENCODING)); 
		
		  ArrayList<String> deletedUser = new ArrayList<String>();
		  String str = null;
		  String id = null;

		  String expiredDate = getDueDate();
		  while ((str = reader.readLine()) != null) {
			id = str.substring(0,str.indexOf(":"));
			String userExpired = userMpExpired.get(id);
			if(userExpired==null ||"".equals(userExpired)){
				logger.info("AddUser:"+id+"$"+expiredDate + " from "+FileName);
				userMpExpired.put(id, expiredDate);
			}else if(afterToday(userExpired)) {
				deletedUser.add(id);
				userMpExpired.remove(id);
				logger.info("RemoveUser:"+id+"$"+userExpired+ " from "+FileName);
			}
			
			//pwd=getAlphaNumericString(8);
			//script =FILE_EXE+"/htpasswd.exe -b "+FILE_NAME+" "+id+" "+pwd;
			//> htpasswd.exe -b </path/to/users.htpasswd> <user_name> <password>
			//System.out.println(script);
			//updatePassword(id,pwd);
		    //System.out.println(str);
		  }
		  
		
		  removeUser(FileName,deletedUser);
		  
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} finally {
		  try {
		    reader.close();
		  } catch (IOException e) {
			  logger.error(e);
		  }
		}

	
	}
	public static String initPropValues() throws IOException {
		
		InputStream inputStream=null;
		String result = "";
		 
		try {
			Properties prop = new Properties();
			String propFileName = "pwdConfig.properties";
 
			inputStream = Passwd.class.getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
			Date time = new Date(System.currentTimeMillis());
 
			// get the property value and print it out
			
			FILE_NAME = prop.getProperty("PWD_File");
			FILE_USER_NAME = prop.getProperty("PWD_ALLUSER_File");
			FILE_EXE = prop.getProperty("PASSWD_DIR");
			FILE_LOCATION = prop.getProperty("PWD_File_LOCATION");
			
			result = "prop List = " + FILE_NAME + ", " + FILE_USER_NAME + ", " + FILE_EXE+ ", " + FILE_LOCATION;
			logger.info(result + "\nProgram Ran on " + time );
		} catch (Exception e) {
			logger.error("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
	
	public static String getCurrentTimeDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");     
		java.util.Date today = new Date();     
		Calendar c = Calendar.getInstance(); 
		c.setTime(today); 
		today = c.getTime();
		return String.valueOf(sdf.format(today)); 
	}
	
	
	public static String getDueDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");     
		java.util.Date today = new Date();     
		Calendar c = Calendar.getInstance(); 
		c.setTime(today); 
		c.add(Calendar.DATE, 180);
		today = c.getTime();
		return String.valueOf(sdf.format(today)); 
	}
	
	public static boolean afterToday(String expiredDate){
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		Date d1 = null;
		Date d2 = null;
		String nowTime = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date());
		try {
			d1 = df.parse(expiredDate);
			d2 = df.parse(nowTime);
		} catch (ParseException e) {
			logger.error(e);
		 return false;
		}
		
		return d2.after(d1); 
	}
	
	
	
	protected static void readUserExpired(){
		BufferedReader reader = null;
		try {
			userMpExpired.clear();
			  reader = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_USER_NAME), ENCODING)); 
			  String str = null;
			  String id = null;
			  String expiredDate=null;
			  
			  while ((str = reader.readLine()) != null) {
				id = str.substring(0,str.indexOf("$"));
				expiredDate = str.substring(str.indexOf("$")+1,str.length());
				logger.info(id);
				userMpExpired.put(id, expiredDate);
			  }
			  
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			} finally {
			  try {
			    reader.close();
			  } catch (IOException e) {
				  logger.error(e);
			  }
			}
	}
	
	protected static void writeToUserExpired(){
		BufferedWriter writer =  null;
		try {
			//userMpExpired.clear();
			 writer = new BufferedWriter(new FileWriter(FILE_USER_NAME, false));
			  String id = null;
			  String expiredDate=null;
			  
			  Iterator it = userMpExpired.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        id = (String) pair.getKey();
			        expiredDate = (String) pair.getValue();
			        writer.append(id+"$"+expiredDate);
					writer.append('\n');
			        it.remove(); // avoids a ConcurrentModificationException
			    }
			  
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			} finally {
			  try {
				  writer.close();
			  } catch (IOException e) {
				  logger.error(e);
			  }
			}
	}
	
	public static void removeUser(String filename,ArrayList<String> list){
		if(!list.isEmpty()){
			for(String id : list){
				try {
					removePassword(filename,id);
					logger.info(getCurrentTimeDate() +"- remove User:"+id);
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}
	
	/***@deprecated */
	protected static void updatePassword (String id,String pwd) throws IOException{
		  List<String> cmds = Arrays.asList("cmd.exe", "/C", "start", FILE_EXE+"/htpasswd.exe", "-b", FILE_NAME, id, pwd);
		  ProcessBuilder builder = new ProcessBuilder((java.util.List<String>) cmds);
		  builder.directory(new File(FILE_LOCATION));
		        builder.redirectErrorStream(true);
		       
		        Process p = builder.start();
		        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), ENCODING) );
		        String line;
		        while (true) {
		            line = r.readLine();
		            if (line == null) { break; }
		            System.out.println(line);
		        }
		 
	}
	
	protected static void removePassword (String filename,String id) throws IOException{
		  List<String> cmds = Arrays.asList("cmd.exe", "/C", "start", FILE_EXE+"/htpasswd.exe", "-D", filename, id);
		  ProcessBuilder builder = new ProcessBuilder((java.util.List<String>) cmds);
		  builder.directory(new File(FILE_LOCATION));
		        builder.redirectErrorStream(true);
		       
		        Process p = builder.start();
		        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), ENCODING) );
		        String line;
		        while (true) {
		            line = r.readLine();
		            if (line == null) { break; }
		            logger.info(line);
		        }
		 
	}
	
	/***@deprecated */
	static String getAlphaNumericString(int n) 
    { 
  
        // length is bounded by 256 Character 
        byte[] array = new byte[256]; 
        new Random().nextBytes(array); 
  
        String randomString 
            = new String(array, Charset.forName(ENCODING)); 
  
        // Create a StringBuffer to store the result 
        StringBuffer r = new StringBuffer(); 
  
        // Append first 20 alphanumeric characters 
        // from the generated random String into the result 
        for (int k = 0; k < randomString.length(); k++) { 
  
            char ch = randomString.charAt(k); 
  
            if (((ch >= 'a' && ch <= 'z') 
                 || (ch >= 'A' && ch <= 'Z') 
                 || (ch >= '0' && ch <= '9')) 
                && (n > 0)) { 
  
                r.append(ch); 
                n--; 
            } 
        } 
  
        // return the resultant string 
        return r.toString(); 
    } 

}
