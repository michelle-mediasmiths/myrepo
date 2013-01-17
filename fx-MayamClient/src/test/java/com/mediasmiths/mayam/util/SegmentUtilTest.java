package com.mediasmiths.mayam.util;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.Timecode;
import com.mayam.wf.attributes.shared.type.Timecode.InvalidTimecodeException;

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
			
			String expected = "1:\t00:00:00:00\t00:00:01:00\t(00:00:01:00)\ttitle\n";
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
		
		@Test
		public void testTotal() throws InvalidTimecodeException{
			
			Segment one = new Segment();
			one.setDuration(new Timecode("00:01:00:05"));
			one.setNumber(0);
			
			Segment two = new Segment();
			two.setDuration(new Timecode("00:01:01:05"));
			two.setNumber(1);
			
			Segment three = new Segment();
			three.setDuration(new Timecode("00:01:10:15"));
			three.setNumber(2);
			
			Segment four = new Segment();
			four.setDuration(new Timecode("01:01:00:05"));
			four.setNumber(3);
			
			List<Segment> list = new ArrayList<Segment>();
			list.add(one);
			list.add(two);
			list.add(three);
			list.add(four);
			
			String expected = "01:04:12:05";
			String actual = SegmentUtil.totalDuration(list);
			assertEquals(expected, actual);
			
			
		}
}


