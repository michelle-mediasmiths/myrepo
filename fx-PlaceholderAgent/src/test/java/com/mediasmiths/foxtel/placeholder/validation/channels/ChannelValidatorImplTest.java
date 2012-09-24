package com.mediasmiths.foxtel.placeholder.validation.channels;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class ChannelValidatorImplTest {
	ChannelValidatorImpl validator;
	Properties channelProperties;
	Properties formatProperties;
	
	public ChannelValidatorImplTest() {
		super();
	}
	
	@Before 
	public void setup()
	{
		channelProperties = mock(Properties.class);
		formatProperties = mock(Properties.class);
		validator = new ChannelValidatorImpl(channelProperties, formatProperties);
	}
	
	@Test
	public void isValidNameForTagSuccess() 
	{
		when(channelProperties.getProperty(anyString())).thenReturn("success");
		assertTrue(validator.isValidNameForTag("channelTag", "success"));
	}
	
	@Test
	public void isValidNameForTagFail() 
	{
		when(channelProperties.getProperty(anyString())).thenReturn("failure");
		assertFalse(validator.isValidNameForTag("channelTag", "success"));	
	}
	
	@Test
	public void isValidFormatForTagSuccess() 
	{
		when(formatProperties.getProperty(anyString())).thenReturn("success");
		assertTrue(validator.isValidFormatForTag("channelTag", "success"));	
	}
	
	@Test
	public void isValidFormatForTagFail() 
	{
		when(formatProperties.getProperty(anyString())).thenReturn("failure");
		assertFalse(validator.isValidFormatForTag("channelTag", "success"));	
	}
}