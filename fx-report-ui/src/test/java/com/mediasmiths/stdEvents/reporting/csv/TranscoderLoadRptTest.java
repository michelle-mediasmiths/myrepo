package com.mediasmiths.stdEvents.reporting.csv;

import com.mediasmiths.stdEvents.coreEntity.db.entity.TranscodeJob;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TranscoderLoadRptTest
{
	private TranscoderLoadRpt toTest = new TranscoderLoadRpt();

	@Test
	public void testGetMaxConcurrentTranscodes(){


		List<TranscodeJob> jobs = new ArrayList<TranscodeJob>();

		jobs.add(finishedJob(1l,1000l));
		jobs.add(finishedJob(2l,90l));
		jobs.add(finishedJob(100l,900l));
		jobs.add(finishedJob(200l,950l));
		jobs.add(finishedJob(1001l,10000l));

		assertEquals(3, toTest.getMaxConcurrentTranscodes(jobs));
	}


	private TranscodeJob finishedJob(long start, long end)
	{
		TranscodeJob j = new TranscodeJob();
		j.setStatus("Completed");

		j.setCreated(new Date(start));
		j.setUpdated(new Date(end));
		return j;
	}
}
