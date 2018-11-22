package com.ibm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.ibm.exception.ServiceException;

public final class ServiceUtils {
	
	private static final String WS_PROPERTIES_FILE  = "ws_connection.properties";
	
	/**
	 * @return
	 */
	public static String getFormattedCurrentTimestampToString(){
		String timestamp =  null;
		String formattedTs = null;
		timestamp = getCurrentTimestampToString();
		System.out.println(timestamp);
		formattedTs = timestamp.replace(" ", "T");
		formattedTs = formattedTs.replace("-", "T");
		formattedTs = formattedTs.replace(".", "T");
		formattedTs = formattedTs.replaceFirst(":", "-");
		System.out.println(formattedTs);
		
		return formattedTs;
	}
	
	/**
	 * Converts the return Xml to Document
	 * @param xmlStr
	 * @return
	 * @throws GDException
	 */
	public static Document convertStringToDocument(String responseXml) throws ServiceException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder = null;  
        Document doc = null;
        
        try  
        {  
            builder = factory.newDocumentBuilder();  
            doc = builder.parse( new InputSource( new StringReader( responseXml ) ) ); 
            
        } catch (Exception e) {  
        	throw new ServiceException(e.getMessage()); 
        } 
        return doc;
    }
	
	/**
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(String str) {
		if (str == null) {
			return true;
		} else if (str.trim().equals("")) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public static String getItemsForSoapConnection(String key){
		InputStream resourceStream = null;
		Properties props = null;
		try {
			props = new Properties();
			resourceStream = ServiceUtils.class.getClassLoader().getResourceAsStream(WS_PROPERTIES_FILE);
			props.load(resourceStream);
		} catch (IOException e) {
			
		}
		
		return props.getProperty(key);
	}
	
	/**
	 * @return
	 */
	public static String getCurrentTimestampToString(){
		 return new Timestamp(System.currentTimeMillis()).toString();
	}
	
	
	
	/**
	 * @param value
	 * @return
	 */
	public static String replaceBlanksWithUnderScore(String value){
		return value.replaceAll(" ", "_");
	}
}
