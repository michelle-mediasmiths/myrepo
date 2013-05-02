package com.mediasmiths.foxtel.placeholder.validation.channels;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.mediasmiths.foxtel.channels.config.ChannelPropertiesImpl;
import com.mediasmiths.std.io.PropertyFile;

public class ChannelValidatorImplTest {
	ChannelPropertiesImpl validator;
	PropertyFile channelProperties;
	
	public ChannelValidatorImplTest() {
		super();
	}
	
	@Before 
	public void setup()
	{
		channelProperties = mock(PropertyFile.class);
		validator = new ChannelPropertiesImpl(channelProperties); 
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

}
