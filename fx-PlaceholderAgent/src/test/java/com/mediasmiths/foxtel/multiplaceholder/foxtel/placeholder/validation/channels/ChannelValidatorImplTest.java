package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.validation.channels;

import com.mediasmiths.foxtel.channels.config.ChannelPropertiesImpl;
import com.mediasmiths.std.io.PropertyFile;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChannelValidatorImplTest {
	ChannelPropertiesImpl validator;
	PropertyFile channelProperties;
	PropertyFile formatProperties;
	
	public ChannelValidatorImplTest() {
		super();
	}
	
	@Before 
	public void setup()
	{
		channelProperties = mock(PropertyFile.class);
		formatProperties = mock(PropertyFile.class);
		validator = new ChannelPropertiesImpl(channelProperties, formatProperties);
	}
	
	@Test
	public void isValidNameForTagSuccess() 
	{
		when(channelProperties.get(anyString())).thenReturn("success");
		assertTrue(validator.isValidNameForTag("channelTag", "success"));
	}
	
	@Test
	public void isValidNameForTagFail() 
	{
		when(channelProperties.get(anyString())).thenReturn("failure");
		assertFalse(validator.isValidNameForTag("channelTag", "success"));	
	}
	
	@Test
	public void isValidFormatForTagSuccess() 
	{
		when(formatProperties.get(anyString())).thenReturn("success");
		assertTrue(validator.isValidFormatForTag("channelTag", "success"));	
	}
	
	@Test
	public void isValidFormatForTagFail() 
	{
		when(formatProperties.get(anyString())).thenReturn("failure");
		assertFalse(validator.isValidFormatForTag("channelTag", "success"));	
	}
}
