package com.mediasmiths.foxtel.tc.priorities;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.tc.priorities.guice.TranscodePrioritiesModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;

public class TranscodePrioritiesTest
{

	private final static Logger log = Logger.getLogger(TranscodePrioritiesTest.class);
	private TranscodePriorities toTest;

	@Before
	public void before()
	{

		// setup guice injector
		final List<Module> moduleList = new ArrayList<Module>();
		moduleList.add(new TranscodePrioritiesModule());

		Injector injector = GuiceInjectorBootstrap.createInjector(new GuiceSetup()
		{

			@Override
			public void registerModules(List<Module> modules, PropertyFile config)
			{
				modules.addAll(moduleList);
			}

			@Override
			public void injectorCreated(Injector injector)
			{
			}
		});
		// get instance of class to test
		toTest = injector.getInstance(TranscodePriorities.class);
	}

	// intervention priorities
	@Test
	public void testInterventionPriorities()
	{

		assertEquals(
				Integer.valueOf(10),
				toTest.getPriorityForTranscodeJob(TranscodeJobType.TX, new Date(), new Date(), Integer.valueOf(10)));
		assertEquals(
				Integer.valueOf(9),
				toTest.getPriorityForTranscodeJob(TranscodeJobType.TX, new Date(), new Date(), Integer.valueOf(9)));

	}

	// ///////////////////////////////////
	// / new jobs tx date in far future //
	// //////////////////////////////////

	@Test
	public void testNewComplianceTXFarFuture()
	{

		DateTime now = new DateTime();
		DateTime nextMonth = now.plusMonths(1);

		Date txDate = nextMonth.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.COMPLIANCE_PROXY, txDate);
		Integer expected = Integer.valueOf(1);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewPublicityTXFarFuture()
	{

		DateTime now = new DateTime();
		DateTime nextMonth = now.plusMonths(1);

		Date txDate = nextMonth.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.PUBLICITY_PROXY, txDate);
		Integer expected = Integer.valueOf(1);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewCaptionTXFarFuture()
	{

		DateTime now = new DateTime();
		DateTime nextMonth = now.plusMonths(1);

		Date txDate = nextMonth.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.CAPTION_PROXY, txDate);
		Integer expected = Integer.valueOf(2);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewTXTXFarFuture()
	{

		DateTime now = new DateTime();
		DateTime nextMonth = now.plusMonths(1);

		Date txDate = nextMonth.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.TX, txDate);
		Integer expected = Integer.valueOf(2);

		assertEquals(expected, actual);

	}

	// ///////////////////////////////////
	// / new jobs tx date in near future //
	// //////////////////////////////////

	@Test
	public void testNewComplianceTXNearFuture()
	{

		DateTime now = new DateTime();
		DateTime nextMinute = now.plusMinutes(1);

		Date txDate = nextMinute.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.COMPLIANCE_PROXY, txDate);
		Integer expected = Integer.valueOf(1);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewPublicityTXNearFuture()
	{

		DateTime now = new DateTime();
		DateTime nextMinute = now.plusMinutes(1);

		Date txDate = nextMinute.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.PUBLICITY_PROXY, txDate);
		Integer expected = Integer.valueOf(1);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewCaptionTXNearFuture()
	{

		DateTime now = new DateTime();
		DateTime nextMinute = now.plusMinutes(1);

		Date txDate = nextMinute.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.CAPTION_PROXY, txDate);
		Integer expected = Integer.valueOf(6);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewTXTXNearFuture()
	{

		DateTime now = new DateTime();
		DateTime nextMinute = now.plusMinutes(1);

		Date txDate = nextMinute.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.TX, txDate);
		Integer expected = Integer.valueOf(8);

		assertEquals(expected, actual);

	}

	// ///////////////////////////////////
	// / new jobs tx date in near future //
	// //////////////////////////////////

	@Test
	public void testNewComplianceTXRecentPast()
	{

		DateTime now = new DateTime();
		DateTime lastMinute = now.minusMinutes(1);

		Date txDate = lastMinute.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.COMPLIANCE_PROXY, txDate);
		Integer expected = Integer.valueOf(5);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewPublicityTXRecentPast()
	{

		DateTime now = new DateTime();
		DateTime lastMinute = now.minusMinutes(1);

		Date txDate = lastMinute.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.PUBLICITY_PROXY, txDate);
		Integer expected = Integer.valueOf(5);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewCaptionTXRecentPast()
	{

		DateTime now = new DateTime();
		DateTime lastMinute = now.minusMinutes(1);

		Date txDate = lastMinute.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.CAPTION_PROXY, txDate);
		Integer expected = Integer.valueOf(6);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewTXTXRecentPast()
	{

		DateTime now = new DateTime();
		DateTime lastMinute = now.minusMinutes(1);

		Date txDate = lastMinute.toDate();

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.TX, txDate);
		Integer expected = Integer.valueOf(8);

		assertEquals(expected, actual);

	}
	
	//test what happens when no tx date is speicified, should act like tx is far away
	
	@Test
	public void testNewComplianceTXNull()
	{

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.COMPLIANCE_PROXY, null);
		Integer expected = Integer.valueOf(1);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewPublicityTXNull()
	{

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.PUBLICITY_PROXY, null);
		Integer expected = Integer.valueOf(1);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewCaptionTXNull()
	{
		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.CAPTION_PROXY, null);
		Integer expected = Integer.valueOf(2);

		assertEquals(expected, actual);

	}

	@Test
	public void testNewTXTXNull()
	{

		Integer actual = toTest.getPriorityForNewTranscodeJob(TranscodeJobType.TX, null);
		Integer expected = Integer.valueOf(2);

		assertEquals(expected, actual);

	}

	// publicity and compliance, jobs queued more than 10 mins
	@Test
	public void testPublicityQueuedForTenMinsTXTomorrow()
	{

		DateTime now = new DateTime();
		DateTime tomorrow = now.plusDays(1);

		Date txDate = tomorrow.toDate();
		Date created = now.minusMinutes(11).toDate();

		Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.PUBLICITY_PROXY, txDate, created, 1);
		Integer expected = Integer.valueOf(3);

		assertEquals(expected, actual);
	}

	@Test
	public void testComplianceQueuedForTenMinsTXTomorrow()
	{

		DateTime now = new DateTime();
		DateTime tomorrow = now.plusDays(1);

		Date txDate = tomorrow.toDate();
		Date created = now.minusMinutes(11).toDate();

		Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.COMPLIANCE_PROXY, txDate, created, 1);
		Integer expected = Integer.valueOf(3);

		assertEquals(expected, actual);
	}

	// publicity and compliance, jobs queued more than 20 mins
	@Test
	public void testPublicityQueuedForTwentyMinsTXTomorrow()
	{

		DateTime now = new DateTime();
		DateTime tomorrow = now.plusDays(1);

		Date txDate = tomorrow.toDate();
		Date created = now.minusMinutes(21).toDate();

		Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.PUBLICITY_PROXY, txDate, created, 1);
		Integer expected = Integer.valueOf(5);

		assertEquals(expected, actual);
	}

	@Test
	public void testComplianceQueuedForTwentyMinsTXTomorrow()
	{

		DateTime now = new DateTime();
		DateTime tomorrow = now.plusDays(1);

		Date txDate = tomorrow.toDate();
		Date created = now.minusMinutes(21).toDate();

		Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.COMPLIANCE_PROXY, txDate, created, 1);
		Integer expected = Integer.valueOf(5);

		assertEquals(expected, actual);
	}
	
	//tx priorities
	
	@Test
	public void testTXTXDateLessThan12Hours(){
		
		DateTime now = new DateTime();
		DateTime later = now.plusHours(11);
		
		Date txDate = later.toDate();
		Date created = now.toDate();
		
		Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.TX, txDate, created, 1);
		Integer expected = Integer.valueOf(8);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testTXTXDateLessThan24Hours(){
		
		DateTime now = new DateTime();
		DateTime later = now.plusHours(14);
		
		Date txDate = later.toDate();
		Date created = now.toDate();
		
		Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.TX, txDate, created, 1);
		Integer expected = Integer.valueOf(7);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testTXTXDateLessThan72Hours(){
		
		DateTime now = new DateTime();
		DateTime later = now.plusHours(50);
		
		Date txDate = later.toDate();
		Date created = now.toDate();
		
		Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.TX, txDate, created, 1);
		Integer expected = Integer.valueOf(6);
		
		assertEquals(expected, actual);
	}

	@Test
	public void testTXTXDateLessThan8Days(){
		
		DateTime now = new DateTime();
		DateTime later = now.plusDays(7);
		
		Date txDate = later.toDate();
		Date created = now.toDate();
		
		Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.TX, txDate, created, 1);
		Integer expected = Integer.valueOf(4);
		
		assertEquals(expected, actual);
	}
	
	//caption priorities
	
		@Test
		public void testCaptionTXDateLessThan12Hours(){
			
			DateTime now = new DateTime();
			DateTime later = now.plusHours(11);
			
			Date txDate = later.toDate();
			Date created = now.toDate();
			
			Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.CAPTION_PROXY, txDate, created, 1);
			Integer expected = Integer.valueOf(6);
			
			assertEquals(expected, actual);
		}
		
		@Test
		public void testCaptionTXDateLessThan24Hours(){
			
			DateTime now = new DateTime();
			DateTime later = now.plusHours(14);
			
			Date txDate = later.toDate();
			Date created = now.toDate();
			
			Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.CAPTION_PROXY, txDate, created, 1);
			Integer expected = Integer.valueOf(6);
			
			assertEquals(expected, actual);
		}
		
		@Test
		public void testCaptionTXDateLessThan72Hours(){
			
			DateTime now = new DateTime();
			DateTime later = now.plusHours(50);
			
			Date txDate = later.toDate();
			Date created = now.toDate();
			
			Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.CAPTION_PROXY, txDate, created, 1);
			Integer expected = Integer.valueOf(6);
			
			assertEquals(expected, actual);
		}

		@Test
		public void testCaptionTXDateLessThan8Days(){
			
			DateTime now = new DateTime();
			DateTime later = now.plusDays(7);
			
			Date txDate = later.toDate();
			Date created = now.toDate();
			
			Integer actual = toTest.getPriorityForTranscodeJob(TranscodeJobType.CAPTION_PROXY, txDate, created, 1);
			Integer expected = Integer.valueOf(4);
			
			assertEquals(expected, actual);
		}
	
}
