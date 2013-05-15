package com.mediasmiths.foxtel.transcode;

import static org.junit.Assert.*;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.name.Names;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.std.io.PropertyFile;

@RunWith(JukitoRunner.class)
public class TranscodeRulesTest
{
	public static class Module extends JukitoModule
	{
		protected void configureTest()
		{
			PropertyFile properties = PropertyFile.find("environment.properties");
			Names.bindProperties(this.binder(), properties.toProperties());
			bind(TranscodeRules.class).to(TranscodeRulesImpl.class);
		}
	}
	
	@Inject
	TranscodeRules toTest;
	
	@Test
	public void testSDinSDoutSurround(){
		assertEquals(TCAudioType.STEREO, toTest.audioTypeForTranscode(true, true, true));
	}
	
	@Test
	public void testHDinSDoutSurround(){
		assertEquals(TCAudioType.DOLBY_E, toTest.audioTypeForTranscode(false, true, true));
	}
	
	@Test
	public void testHDinHDoutSurround(){
		assertEquals(TCAudioType.DOLBY_E, toTest.audioTypeForTranscode(false, true, false));
	}
	
	@Test
	public void testSDinHDoutSurround(){
		assertEquals(TCAudioType.STEREO, toTest.audioTypeForTranscode(true, true, false));
	}
	
	@Test
	public void testSDinSDoutNotSurround(){
		assertEquals(TCAudioType.STEREO, toTest.audioTypeForTranscode(true, false, false));
	}
	
	@Test
	public void testHDinSDoutNotSurround(){
		assertEquals(TCAudioType.STEREO, toTest.audioTypeForTranscode(false, false, true));
	}
	
	@Test
	public void testHDinHDoutNotSurround(){
		assertEquals(TCAudioType.STEREO, toTest.audioTypeForTranscode(false, false, false));
	}
	
	@Test
	public void testSDinHDoutNotSurround(){
		assertEquals(TCAudioType.STEREO, toTest.audioTypeForTranscode(true, false, false));
	}
}
