package com.mediasmiths.stdEvents.reporting.csv;

import com.mediasmiths.foxtel.ip.common.events.FilePickupDetails;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WatchFolderAverageTest
{

	WatchFolderRpt toTest = new WatchFolderRpt();

	@Test
	public void testWatchFolderAverages(){
		List<FilePickupDetails> files = new ArrayList<FilePickupDetails>();

		FilePickupDetails 		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 1");
		f.setFilename("file1.xml");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(1001);
		files.add(f);

		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 1");
		f.setFilename("file2.xml");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(2001);
		files.add(f);


		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 1");
		f.setFilename("file1.mxf");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(3001);
		files.add(f);


		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 1");
		f.setFilename("file2.mxf");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(4001);
		files.add(f);

		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 2");
		f.setFilename("file1.xml");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(-1001);
		files.add(f);

		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 2");
		f.setFilename("file2.xml");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(2001);
		files.add(f);


		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 2");
		f.setFilename("file1.mxf");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(3001);
		files.add(f);


		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 2");
		f.setFilename("file2.mxf");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(8001);
		files.add(f);

		f = new FilePickupDetails();
		f.setAggregator("AGGREGATOR 3");
		f.setFilename("file2.xml");
		f.setTimeDiscovered(1);
		f.setTimeProcessed(8001);
		files.add(f);



		final WatchFolderRpt.WatchFolderStats stats = toTest.getStats(files);

		assertEquals("Folder: AGGREGATOR 1 all high: 00:00:04:000 low: 00:00:01:000 average: 00:00:02:500 sd: 00:00:01:290\r\n" +
		             "Folder: AGGREGATOR 1 mxf high: 00:00:04:000 low: 00:00:03:000 average: 00:00:03:500 sd: 00:00:00:707\r\n" +
		             "Folder: AGGREGATOR 1 xml high: 00:00:02:000 low: 00:00:01:000 average: 00:00:01:500 sd: 00:00:00:707\r\n" +
		             "Folder: AGGREGATOR 2 all high: 00:00:08:000 low: 00:00:02:000 average: 00:00:04:333 sd: 00:00:03:214\r\n" +
		             "Folder: AGGREGATOR 2 mxf high: 00:00:08:000 low: 00:00:03:000 average: 00:00:05:500 sd: 00:00:03:535\r\n" +
		             "Folder: AGGREGATOR 2 xml high: 00:00:02:000 low: 00:00:02:000 average: 00:00:02:000 sd: 00:00:00:000\r\n" +
		             "Folder: AGGREGATOR 3 all high: 00:00:08:000 low: 00:00:08:000 average: 00:00:08:000 sd: 00:00:00:000\r\n", stats.statsString);
	}
}
