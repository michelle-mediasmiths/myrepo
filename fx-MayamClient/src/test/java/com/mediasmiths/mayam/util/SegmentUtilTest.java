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
			
			String expected = "1_00:00:00:00_00:00:01:00_00:00:01:00_title\n";
			String actual = SegmentUtil.segmentToString(s);
			System.out.println(actual);
			assertEquals(expected, actual);
			
		}
		
		@Test
		public void testStringToSegment(){
			
			String input = "1_00:00:00:00_00:00:01:00_00:00:01:00_title\n";
			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment actual = SegmentUtil.stringToSegment(input);
			assertEquals("00:00:00:00", actual.getSOM());
			assertEquals(1, actual.getSegmentNumber());
			assertEquals("title", actual.getSegmentTitle());
			assertEquals("00:00:01:00", actual.getEOM());
			
		}
		
		@Test
		public void testStringToSegmnetList(){
							
			String input = "N____SOM_______DURATION_____EOM_______TITLE\n1_00:00:00:00_00:00:01:00_00:00:01:00_title\n2_00:00:00:00_00:00:01:00_00:00:01:00_title2\n3_00:00:00:00_00:00:01:00_00:00:01:00_title3\n";
			List<com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment> actual = SegmentUtil.stringToSegmentList(input);
			
			assertEquals("00:00:00:00", actual.get(0).getSOM());
			assertEquals(1, actual.get(0).getSegmentNumber());
			assertEquals("title", actual.get(0).getSegmentTitle());
			assertEquals("00:00:01:00", actual.get(0).getEOM());
			
			assertEquals("00:00:00:00", actual.get(1).getSOM());
			assertEquals(2, actual.get(1).getSegmentNumber());
			assertEquals("title2", actual.get(1).getSegmentTitle());
			assertEquals("00:00:01:00", actual.get(1).getEOM());
			
			assertEquals("00:00:00:00", actual.get(2).getSOM());
			assertEquals(3, actual.get(2).getSegmentNumber());
			assertEquals("title3", actual.get(2).getSegmentTitle());
			assertEquals("00:00:01:00", actual.get(2).getEOM());
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


