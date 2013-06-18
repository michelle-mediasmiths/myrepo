package com.mediasmiths.mayam.util;

import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.Timecode;
import com.mayam.wf.attributes.shared.type.Timecode.InvalidTimecodeException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
			String actual = SegmentUtil.materialExchangeSegmentToString(s);
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
		public void testRuzzSegmentToString(){
			com.mediasmiths.foxtel.generated.ruzz.SegmentationType.Segment s= new com.mediasmiths.foxtel.generated.ruzz.SegmentationType.Segment();

			s.setSOM("00:00:00:00");
			s.setEOM("00:00:01:00");
			s.setSegmentNumber(1);
			s.setSegmentTitle("title");

			String expected = "1_00:00:00:00_00:00:01:01_00:00:01:00_title\n";
			String actual = SegmentUtil.ruzzSegmentToString(s);
			System.out.println(actual);
			assertEquals(expected, actual);


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

			String expected = "00:01:01:15";
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

			String expected = "00:04:01:17";
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

		@Test
		public void testSegmentNotOverlapping() throws InvalidTimecodeException
		{
			Segment one = new Segment();
			one.setIn(new Timecode("00:15:00:05"));
			one.setDuration(new Timecode("00:05:00:00"));

			Segment two = new Segment();
			two.setIn(new Timecode("00:21:00:00"));
			two.setDuration(new Timecode("00:06:00:00"));

			Segment three = new Segment();
			three.setIn(new Timecode("00:28:00:00"));
			three.setDuration(new Timecode("00:03:00:00"));

			Segment four = new Segment();
			four.setIn(new Timecode("00:32:00:00"));
			four.setDuration(new Timecode("00:02:00:00"));

			List<Segment> list = new ArrayList<>();
			list.add(one);
			list.add(two);
			list.add(three);
			list.add(four);


			SegmentList segmentList = new SegmentList();
			segmentList.getEntries().addAll(list);

			//Boolean status = true;
			Boolean actual = SegmentUtil.noSegmentationOverlap(segmentList);
			assertTrue(actual);

		}

		@Test
		public void testSegmentOverlapping() throws InvalidTimecodeException
		{
			Segment one = new Segment();
			one.setIn(new Timecode("00:16:00:00"));
			one.setDuration(new Timecode("00:00:00:06"));

			Segment two = new Segment();
			two.setIn(new Timecode("00:16:00:04"));
			two.setDuration(new Timecode("00:00:00:09"));

			Segment three = new Segment();
			three.setIn(new Timecode("00:26:00:00"));
			three.setDuration(new Timecode("00:03:00:00"));

			Segment four = new Segment();
			four.setIn(new Timecode("00:34:00:00"));
			four.setDuration(new Timecode("00:02:00:00"));

			List<Segment> list = new ArrayList<>();
			list.add(one);
			list.add(two);
			list.add(three);
			list.add(four);


			SegmentList segmentList = new SegmentList();
			segmentList.getEntries().addAll(list);

			//Boolean status = true;
			Boolean actual = SegmentUtil.noSegmentationOverlap(segmentList);
			assertFalse(actual);

		}


	@Test
	public void testSegmentOverlappingCheckForSingleSegment() throws InvalidTimecodeException
	{

		//We expect the segmentOverlap check to pass a segment list with only one segment
		Segment one = new Segment();
		one.setIn(new Timecode("00:16:00:00"));
		one.setDuration(new Timecode("00:00:00:06"));

		SegmentList segmentList = new SegmentList();
		segmentList.getEntries().add(one);
		Boolean actual = SegmentUtil.noSegmentationOverlap(segmentList);
		assertTrue(actual);
	}


	@Test
	public void testSegmentOverlappingCheckForTwoNonOverlappingSegments() throws InvalidTimecodeException
	{
		Segment one = new Segment();
		one.setIn(new Timecode("00:16:00:00"));
		one.setDuration(new Timecode("00:00:00:01"));

		Segment two = new Segment();
		two.setIn(new Timecode("00:18:00:07"));
		two.setDuration(new Timecode("00:00:00:09"));

		SegmentList segmentList = new SegmentList();
		segmentList.getEntries().add(one);
		segmentList.getEntries().add(two);
		Boolean actual = SegmentUtil.noSegmentationOverlap(segmentList);
		assertTrue(actual);
	}

	@Test
	public void testSegmentOverlappingCheckForNonOverlappingSegmentsProvidedOutOfOrder() throws InvalidTimecodeException
	{
		//if the tasks api doesnt return segments ordered by their segment number then there will appear to be overlaps

		Segment one = new Segment();
		one.setIn(new Timecode("00:00:00:00"));
		one.setDuration(new Timecode("00:07:41:18"));
		one.setNumber(1);

		Segment two = new Segment();
		two.setIn(new Timecode("00:07:41:18"));
		two.setDuration(new Timecode("00:06:59:06"));
		two.setNumber(2);

		Segment three = new Segment();
		three.setIn(new Timecode("00:14:40:24"));
		three.setDuration(new Timecode("00:08:01:13"));
		three.setNumber(3);


		Segment four = new Segment();
		four.setIn(new Timecode("00:22:42:12"));
		four.setDuration(new Timecode("00:07:04:19"));
		four.setNumber(4);


		Segment five = new Segment();
		five.setIn(new Timecode("00:29:47:06"));
		five.setDuration(new Timecode("00:12:23:01"));
		five.setNumber(5);

		SegmentList segmentList = new SegmentList();
		segmentList.getEntries().add(two);
		segmentList.getEntries().add(three);
		segmentList.getEntries().add(four);
		segmentList.getEntries().add(five);
		segmentList.getEntries().add(one);
		Boolean actual = SegmentUtil.noSegmentationOverlap(segmentList);
		assertTrue(actual);
	}

	@Test
	public void testSegmentOverlappingCheckForTwoOverlappingSegments() throws InvalidTimecodeException
	{
		Segment one = new Segment();
		one.setIn(new Timecode("00:16:00:00"));
		one.setDuration(new Timecode("00:00:00:05"));

		Segment two = new Segment();
		two.setIn(new Timecode("00:16:00:04"));
		two.setDuration(new Timecode("00:00:00:09"));

		SegmentList segmentList = new SegmentList();
		segmentList.getEntries().add(one);
		segmentList.getEntries().add(two);
		Boolean actual = SegmentUtil.noSegmentationOverlap(segmentList);
		assertFalse(actual);
	}

		@Test
		public void testSegmentOverlappingByFrame() throws InvalidTimecodeException
		{
			Segment one = new Segment();
			one.setIn(new Timecode("00:16:00:05"));
			one.setDuration(new Timecode("00:00:00:01"));

			Segment two = new Segment();
			two.setIn(new Timecode("00:16:00:06"));
			two.setDuration(new Timecode("00:00:00:09"));

			Segment three = new Segment();
			three.setIn(new Timecode("00:27:00:00"));
			three.setDuration(new Timecode("00:03:00:00"));

			Segment four = new Segment();
			four.setIn(new Timecode("00:34:00:00"));
			four.setDuration(new Timecode("00:02:00:00"));

			List<Segment> list = new ArrayList<>();
			list.add(one);
			list.add(two);
			list.add(three);
			list.add(four);


			SegmentList segmentList = new SegmentList();
			segmentList.getEntries().addAll(list);

			//Boolean status = true;
			Boolean actual = SegmentUtil.noSegmentationOverlap(segmentList);
			assertTrue(actual);

		}

		@Test
		public void testPresentationToHumanString(){

			Presentation presentation = new Presentation();

			//package 1
			com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package package1 = new com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package();
			package1.setPresentationID("PACKAGE1");

			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s1 = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			s1.setSOM("00:01:00:01");
			s1.setDuration("00:03:01:17");
			s1.setSegmentNumber(1);
			s1.setSegmentTitle("title1");

			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s2 = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			s2.setSOM("00:04:00:01");
			s2.setEOM("00:07:10:17");
			s2.setSegmentNumber(2);
			s2.setSegmentTitle("title2");


			Segmentation segmentation1 = new Segmentation();
			segmentation1.getSegment().add(s1);
			segmentation1.getSegment().add(s2);

			package1.setSegmentation(segmentation1);

			//pacakge2
			com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package package2 = new com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package();
			package2.setPresentationID("PACKAGE2");

			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment p2s1 = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			p2s1.setSOM("00:01:00:00");
			p2s1.setDuration("00:03:01:16");
			p2s1.setSegmentNumber(1);
			p2s1.setSegmentTitle("pack2title1");

			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment p2s2 = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			p2s2.setSOM("00:04:01:16");
			p2s2.setEOM("00:05:10:17");
			p2s2.setSegmentNumber(2);
			p2s2.setSegmentTitle("pack2title2");


			Segmentation segmentation2 = new Segmentation();
			segmentation2.getSegment().add(p2s1);
			segmentation2.getSegment().add(p2s2);

			package2.setSegmentation(segmentation2);

			presentation.getPackage().add(package1);
			presentation.getPackage().add(package2);

			String actual = SegmentUtil.presentationToHumanString(presentation);
			System.out.println(actual);

			String expected = "PACKAGE1\nN____SOM_______DURATION_____EOM_______TITLE\n1_00:01:00:01_00:03:01:17_00:04:01:17_title1\n2_00:04:00:01_00:03:10:17_00:07:10:17_title2\n\nPACKAGE2\nN____SOM_______DURATION_____EOM_______TITLE\n1_00:01:00:00_00:03:01:16_00:04:01:15_pack2title1\n2_00:04:01:16_00:01:09:02_00:05:10:17_pack2title2\n\n";

			System.out.println(expected);
			//if this test is failing the following may be uncommented to see where the first differnece in the expected and actual strings are
			/*
			char[] actualChar = actual.toCharArray();
			char[] expectedChar = expected.toCharArray();
			
			assertEquals(expectedChar.length, actualChar.length);
			
			for(int i=0;i<expectedChar.length;i++){
				
				if(expectedChar[i] != actualChar[i]){
					System.out.println(String.format("first difference at char %d expected %c actual %c",i, expectedChar[i], actualChar[i]));
					System.out.println(actual.substring(0,  i));
					System.out.println(actualChar[i]);
					if(i<expectedChar.length-1){
					System.out.println(actual.substring(i));
					}
					break;
				}
			}
			
			*/
			assertEquals(expected, actual);
		}
	@Test
	public void testStringIDFromNatualBreaksString(){

		System.out.println("*********  testStringIDFromNatualBreaksString  ***********");

		//when the natural breaks field was populated with presenentation information there will be package ids
		String input ="PACKAGE1\nN____SOM_______DURATION_____EOM_______TITLE\n1_00:01:00:01_00:03:01:17_00:04:01:17_title1\n2_00:04:00:01_00:03:10:17_00:07:10:17_title2\n\nPACKAGE2\nN____SOM_______DURATION_____EOM_______TITLE\n1_00:01:00:00_00:03:01:16_00:04:01:15_pack2title1\n2_00:04:01:16_00:01:09:02_00:05:10:17_pack2title2\n\n";
		String expected = "N____SOM_______DURATION_____EOM_______TITLE\n1_00:01:00:01_00:03:01:17_00:04:01:17_title1\n2_00:04:00:01_00:03:10:17_00:07:10:17_title2\n\nN____SOM_______DURATION_____EOM_______TITLE\n1_00:01:00:00_00:03:01:16_00:04:01:15_pack2title1\n2_00:04:01:16_00:01:09:02_00:05:10:17_pack2title2\n\n";


		String actual = SegmentUtil.removePackageIDFromSegmentationNotesString(input);

		assertEquals(expected,actual);
	}

	@Test
	public void testStringIDFromNatualBreaksStringWasOriginalConform(){

		//when the natural breaks was populated from original conform information there will be no package id
		String input ="N____SOM_______DURATION_____EOM_______TITLE\n1_00:01:00:01_00:03:01:17_00:04:01:17_title1\n2_00:04:00:01_00:03:10:17_00:07:10:17_title2\n\n";
		String expected = "N____SOM_______DURATION_____EOM_______TITLE\n1_00:01:00:01_00:03:01:17_00:04:01:17_title1\n2_00:04:00:01_00:03:10:17_00:07:10:17_title2\n\n";


		String actual = SegmentUtil.removePackageIDFromSegmentationNotesString(input);

		assertEquals(expected,actual);
	}
	@Test
	public void testStringIDFromNatualBreaksStringEmptyOrNullInput(){
		assertEquals(null,SegmentUtil.removePackageIDFromSegmentationNotesString(null));
		assertEquals("",SegmentUtil.removePackageIDFromSegmentationNotesString(""));
	}


}


