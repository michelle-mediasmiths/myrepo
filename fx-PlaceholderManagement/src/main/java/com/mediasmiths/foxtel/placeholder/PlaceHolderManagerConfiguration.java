package com.mediasmiths.foxtel.placeholder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PlaceHolderManagerConfiguration {

	static Logger logger = Logger.getLogger(PlaceHolderManagerConfiguration.class);
	
	private final Properties properties;
	
	
	private static PlaceHolderManagerConfiguration instance;
	public static PlaceHolderManagerConfiguration getInstance() throws IOException{
		if(instance==null){
			instance = new PlaceHolderManagerConfiguration();
		}
		
		return instance;
	}
	
	public PlaceHolderManagerConfiguration() throws IOException{
		this("placeholdermanagement.properties");
	}
	
	public PlaceHolderManagerConfiguration(String resource) throws IOException{
		
		InputStream propertiesStream = this.getClass().getClassLoader().getResourceAsStream(resource);
		properties = new Properties();
		properties.load(propertiesStream);
		
		StringBuilder sb = new StringBuilder("Using Properties \n");
		
		for(Entry<Object,Object> entry : properties.entrySet()){
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		
		logger.info(sb.toString());
		
	}
	
	/**
	 * Returns the path the placeholder manager should watch for incoming messages
	 * @return
	 */
	public String getMessagePath() {
		return properties.getProperty("placeholder.path.message");
	}

	/**
	 * Returns the path where failed requests should be moved to	
	 * @return
	 */
	public String getFailurePath() {
		return properties.getProperty("placeholder.path.failure");
	}
	
	/**
	 * Returns the path to place processed placeholder messages
	 * @return
	 */
	public String getArchivedMessagesPath() {
		return properties.getProperty("placeholder.path.archive");
	}
	
	/**
	 * Returns the path the PlaceHolderManagement sercvice should write message receipts to
	 * @return
	 */
	public String getReceiptPath(){
		return properties.getProperty("placeholder.path.receipt");
	}
}
