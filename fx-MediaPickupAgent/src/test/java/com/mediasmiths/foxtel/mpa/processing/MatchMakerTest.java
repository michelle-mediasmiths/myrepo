package com.mediasmiths.foxtel.mpa.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Collection;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Test;

import com.mediasmiths.foxtel.mpa.ProgrammeMaterialTest;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;
import com.mediasmiths.foxtel.mpa.processing.MatchMaker;
import com.mediasmiths.foxtel.mpa.processing.UnmatchedFile;

public class MatchMakerTest {
	
	private MatchMaker toTest;
	
	@Before
	public void before(){
		toTest = new MatchMaker();
	}
	
	private static File FOOXML = new File("/foo.xml");
	private static File FOOMXF = new File("/foo.mxf");
	
	private static File BARXML = new File("/bar.xml");
	private static File BARMXF = new File("/bar.mxf");
	
	
	@Test
	public void testSwapExtension(){
		assertEquals("/my/file/path/blah.txt", toTest.swapExtensionFor("/my/file/path/blah.xml", "txt"));
	}
	
	
	/**
	 * Files arrive in order:
	 * 
	 * foo.xml
	 * foo.mxf
	 * @throws DatatypeConfigurationException 
	 */
	@Test
	public void mxfthenxml() throws DatatypeConfigurationException{
	
		MaterialEnvelope env = getEnvelope(FOOXML);		
		assertNull(toTest.matchXML(env));
		assertEquals(env, toTest.matchMXF(FOOMXF));
		
	}
	

	
	/**
	 * Files arrive in order
	 * 
	 * foo.mxf
	 * foo.xml
	 * @throws DatatypeConfigurationException 
	 */
	@Test
	public void xmlthenmxf() throws DatatypeConfigurationException{
		
		MaterialEnvelope env = getEnvelope(FOOXML);		
		assertNull(toTest.matchMXF(FOOMXF));
		assertEquals(FOOMXF.getAbsolutePath(), toTest.matchXML(env));
		
		
	}
	
	/**
	 * Files arrive in order
	 * 
	 * foo.mxf
	 * bar.xml
	 * foo.xml
	 * bar.mxf
	 * @throws DatatypeConfigurationException 
	 */
	@Test
	public void mixedorderarrival() throws DatatypeConfigurationException{
		
		MaterialEnvelope fooenv = getEnvelope(FOOXML);
		MaterialEnvelope barenv = getEnvelope(BARXML);
		
		assertNull(toTest.matchMXF(FOOMXF));
		assertNull(toTest.matchXML(barenv));
		assertEquals(FOOMXF.getAbsolutePath(), toTest.matchXML(fooenv));
		assertEquals(barenv, toTest.matchMXF(BARMXF));
		
	}
	
	/**
	 * foo.xml arrives and then nothing for a while, foo.xml should be discarded when purge is called
	 * @throws DatatypeConfigurationException 
	 * @throws InterruptedException 
	 */
	@Test
	public void lonelyfilesexpire() throws DatatypeConfigurationException, InterruptedException{
		
		final long ONE_MILLISECOND = 1l;
		final long ONE_SECOND = 1000l;
	
		MaterialEnvelope env = getEnvelope(FOOXML);
		MaterialEnvelope barenv = getEnvelope(BARXML);
		
		assertNull(toTest.matchMXF(FOOMXF));
		assertNull(toTest.matchXML(barenv));
		
		Thread.currentThread().sleep(ONE_SECOND);
		
		Collection<MaterialEnvelope> purgeUnmatchedMessages = toTest.purgeUnmatchedMessages(ONE_MILLISECOND);
		assertEquals(1, purgeUnmatchedMessages.size());
		assertEquals(barenv, purgeUnmatchedMessages.iterator().next());
		
		Collection<UnmatchedFile> purgeUnmatchedMXFs = toTest.purgeUnmatchedMXFs(ONE_MILLISECOND);
		assertEquals(1, purgeUnmatchedMXFs.size());
		assertEquals(new UnmatchedFile(FOOMXF.getAbsolutePath()), purgeUnmatchedMXFs.iterator().next());
		
		assertNull(toTest.matchXML(env));
		assertNull(toTest.matchMXF(BARMXF));
		
	}
	private MaterialEnvelope getEnvelope(File forXML) throws DatatypeConfigurationException{
		return new MaterialEnvelope(forXML, ProgrammeMaterialTest.getMaterialNoPackages("TITLE", "MATERIAL"));
	}

}
