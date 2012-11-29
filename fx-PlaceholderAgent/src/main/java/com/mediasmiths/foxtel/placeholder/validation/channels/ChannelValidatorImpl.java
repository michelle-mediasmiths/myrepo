package com.mediasmiths.foxtel.placeholder.validation.channels;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class ChannelValidatorImpl implements ChannelValidator {

	final Properties configProperties;
	final Properties formatProperties;
	private static Logger logger = Logger.getLogger(ChannelValidatorImpl.class);
	
	@Inject
	public ChannelValidatorImpl() throws IOException
	{
		
		
		configProperties = new Properties();
		try {
			configProperties.load(getClass().getClassLoader().getResourceAsStream("channelConfig.properties"));
		} catch (FileNotFoundException e) {
			logger.error("Failed to find channelConfig.properties!", e);
			throw e;
		} catch (IOException e) {
			logger.error("Failed to read from channelConfig.properties!", e);
			throw e;
		}
		 
		formatProperties = new Properties();
		try {
			formatProperties.load(getClass().getClassLoader().getResourceAsStream("channelFormat.properties"));
		} catch (FileNotFoundException e) {
			logger.error("Failed to find channelFormat.properties!", e);
			throw e;
		} catch (IOException e) {
			logger.error("Failed to read from channelFormat.properties!", e);
			throw e;
		}
		 
	}
	
	public ChannelValidatorImpl(Properties configProperties, Properties formatProperties)
	{
		this.configProperties = configProperties;
		this.formatProperties = formatProperties;	 
	}
	
	@Override
	public boolean isValidNameForTag(String channelTag, String channelName) {
		String expectedName = configProperties.getProperty(channelTag);
		return channelName.equals(expectedName);
	}

	@Override
	public boolean isValidFormatForTag(String channelTag, String channelFormat) {
		String expectedFormat = formatProperties.getProperty(channelTag);
		return channelFormat.equals(expectedFormat);
	}

	@Override
	public boolean isTagValid(String channelTag)
	{
		return configProperties.containsKey(channelTag);
	}

}
