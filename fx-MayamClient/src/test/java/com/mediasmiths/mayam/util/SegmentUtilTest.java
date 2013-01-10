package com.mediasmiths.mayam.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SegmentUtilTest
{
		@Test
		public void testSegmentToString(){
			
			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			
			s.setSOM("00:00:00:00");
			s.setEOM("00:00:01:00");
			s.setDuration("00:00:01:00");
			s.setSegmentNumber(1);
			s.setSegmentTitle("title");		
			
			String expected = "Number:{1},Title:{title},SOM:{00:00:00:00},EOM:{00:00:01:00},Duration:{00:00:01:00}\n";
			String actual = SegmentUtil.segmentToString(s);
			assertEquals(expected, actual);
			
		}
		
		@Test
		public void testFillDuration(){
			
			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			
			s.setSOM("00:01:00:00");
			s.setEOM("00:02:01:14");
			s.setSegmentNumber(1);
			s.setSegmentTitle("title");		
			
			String expected = "00:01:01:14";			
			String actual = SegmentUtil.fillEomAndDurationOfSegment(s).getDuration();
			assertEquals(expected, actual);
		}
		
		@Test
		public void testFillEOM(){
			
			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			
			s.setSOM("00:01:00:01");
			s.setDuration("00:03:01:17");
			s.setSegmentNumber(1);
			s.setSegmentTitle("title");		
			
			String expected = "00:04:01:18";			
			String actual = SegmentUtil.fillEomAndDurationOfSegment(s).getEOM();
			assertEquals(expected, actual);
		}
}


