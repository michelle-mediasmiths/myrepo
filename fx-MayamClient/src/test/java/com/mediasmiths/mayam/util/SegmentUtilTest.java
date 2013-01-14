package com.mediasmiths.mayam.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
			
			String expected = "Number:{1},Title:{title},SOM:{00:00:00:00},EOM:{00:00:01:00},Duration:{00:00:01:00}\n";
			String actual = SegmentUtil.segmentToString(s);
			assertEquals(expected, actual);
			
		}
		
		@Test
		public void testStringToSegment(){
			
			
			String str = "Number:{1},Title:{title},SOM:{00:00:00:00},EOM:{00:00:01:00},Duration:{00:00:01:00}\n";
			
			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment expected = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			
			
			expected.setSOM("00:00:00:00");
			expected.setDuration("00:00:01:00");
			expected.setSegmentNumber(1);
			expected.setSegmentTitle("title");		
			
			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment actual = SegmentUtil.stringToSegment(str);
			
			assertEquals(expected.getSegmentNumber(), actual.getSegmentNumber());
			assertEquals(expected.getSegmentTitle(), actual.getSegmentTitle());
			assertEquals(expected.getSOM(), actual.getSOM());			
			assertEquals(expected.getDuration(), actual.getDuration());
			
			
		}
		
		@Test
		public void testNaturalBreaksStringToSegments(){
			
			String str = "Number:{1},Title:{title},SOM:{00:00:00:00},EOM:{00:00:01:00},Duration:{00:00:01:00}\n"
			+"Number:{2},Title:{title},SOM:{00:00:01:00},EOM:{00:00:10:00},Duration:{00:00:09:00}\n";
			
			String[] segs = str.split("\n");
			
			
			assertEquals(SegmentUtil.stringToSegment(segs[0]+"\n").getDuration(),"00:00:01:00");
			assertEquals(SegmentUtil.stringToSegment(segs[1]+"\n").getSegmentNumber(),2);
			
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


