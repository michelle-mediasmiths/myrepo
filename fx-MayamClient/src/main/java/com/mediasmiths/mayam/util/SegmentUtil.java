package com.mediasmiths.mayam.util;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.Timecode.InvalidTimecodeException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.foxtel.generated.materialexport.PresentationInformationType.SegmentationInformation;
import com.mediasmiths.foxtel.generated.materialexport.SegmentType;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media.Segments;
import com.mediasmiths.foxtel.generated.mediaexchange.SegmentListType;
import com.mediasmiths.mayam.FullProgrammePackageInfo;
import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.SampleCount;
import com.mediasmiths.std.types.Timecode;
import com.mediasmiths.std.types.TimecodeRange;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SegmentUtil
{
	private static Logger log = Logger.getLogger(SegmentUtil.class);

	public static String materialExchangeSegmentToString(com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s)
	{
		s = fillEomAndDurationOfSegment(s);		
		return segmenttoString(s.getSegmentNumber(),s.getSOM(),s.getDuration(),s.getEOM(),s.getSegmentTitle());
	}
	
	public static String segmenttoString(int segmentNumber, String som, String duration, String eom, String title){
		return String.format("%d_%s_%s_%s_%s\n",segmentNumber,som,duration,eom,title);
	}

	private static Pattern stringToSeg = Pattern.compile("([0-9]*)_(.*)_(.*)_(.*)_(.*)\n");
	
	public static com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment stringToSegment(String str) throws IllegalArgumentException
	{
		com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s =  new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();


		
		Matcher m  = stringToSeg.matcher(str);

		if(m.matches()){
			int number = Integer.parseInt(m.group(1));
			String title = m.group(5);
			String som = m.group(2);
			String eom = m.group(4);
			String duration = m.group(3);

			s.setSegmentNumber(number);
			s.setSegmentTitle(title);
			s.setSOM(som);
			s.setEOM(eom);
			return s;
		}

		throw new IllegalArgumentException("could not parse supplied string as segment info: "+str);
	}
	
	public static List<com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment> stringToSegmentList(String str) throws IllegalArgumentException
	{
		
		List<com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment> ret = new ArrayList<com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment>();
		//ignore the titleline
		Matcher m  = stringToSeg.matcher(str.substring(str.indexOf('\n')));
		
		while (m.find())
		{
			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			int number = Integer.parseInt(m.group(1));
			String title = m.group(5);
			String som = m.group(2);
			String eom = m.group(4);
			String duration = m.group(3);

			s.setSegmentNumber(number);
			s.setSegmentTitle(title);
			s.setSOM(som);
			s.setEOM(eom);
			ret.add(s);
		}
		
		return ret;
		
	}
	
	public static int stringToNumberOfSegments(String str) throws IllegalArgumentException
	{
		Matcher m  = stringToSeg.matcher(str.substring(str.indexOf('\n')));
		int segmentCount = 0;
		
		while (m.find())
		{
			com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
			int number = Integer.parseInt(m.group(1));
			String title = m.group(5);
			String som = m.group(2);
			String eom = m.group(4);
			String duration = m.group(3);

			segmentCount ++;
		}
		
		return segmentCount;
	}
	
	/**
	 * Takes a segment and produces a copy with both eom and duration populated (eom and duration are a choice in the schema, returned object will not be schema compliant, not for marshalling)
	 * @param s
	 * @return
	 */
	public static com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment fillEomAndDurationOfSegment(com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s)
	{
		String som = s.getSOM();
		String eom = s.getEOM();
		String duration = s.getDuration();
		
		Timecode start =  Timecode.getInstance(som, Framerate.HZ_25); 
		
		if(eom == null){
			eom = calculateEOM(duration, start);
		}
		if(duration == null){
			Timecode end =  Timecode.getInstance(eom, Framerate.HZ_25);
			duration = getDurationSMTPE(start,end);
		}
		
		//filled will hae some, eom and duration populated, should not be written to file as it will no longer be schema conforming (duration and eom are an xs:choice at the same level);
		com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment filled = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
		filled.setDuration(duration);
		filled.setEOM(eom);
		filled.setSOM(som);
		filled.setSegmentNumber(s.getSegmentNumber());
		filled.setSegmentTitle(s.getSegmentTitle());
		
		return filled;
	}

	public static String calculateEOM(String duration, Timecode start)
	{
		final String eom;
		Timecode durationTimecode =  Timecode.getInstance(duration, Framerate.HZ_25);

		return start.add(durationTimecode.getSampleCount().subtract(new SampleCount(1, start.getFramerate()))).toSMPTEString();

	}
	
	public static String totalDuration(List<Segment> segments){
		
		Timecode total = Timecode.getInstance(new SampleCount(0l, Framerate.HZ_25));
		
		for(Segment s : segments){
			log.trace(String.format("Segment %s duration %s", s.getNumber(), s.getDuration().toSmpte()));
			total = total.add(new SampleCount(s.getDuration().getTotalFrames(), Framerate.HZ_25));
		}
		
		return total.toSMPTEString();
		
	}
	public static boolean segmentationOverlap(SegmentList segmentList)
	{
		Boolean status = false;

		try
		{
			if (segmentList != null && segmentList.getEntries() != null )
			{
				outerloop:
					for(int i = 0; i<=segmentList.getEntries().size() - 2; i++)
					{
						int j = i + 1;

						final List<Segment> s = segmentList.getEntries();
						Segment a = s.get(i);
						Segment b = s.get(j);

						String aSom = a.getIn().toSmpte();
						String bSom = b.getIn().toSmpte();
						Timecode aStart = Timecode.getInstance(aSom, Framerate.HZ_25);
						Timecode bStart = Timecode.getInstance(bSom, Framerate.HZ_25);

						String aDuration = a.getDuration().toSmpte();
						String bDuration = b.getDuration().toSmpte();
						Timecode aDur = Timecode.getInstance(aDuration, Framerate.HZ_25);
						Timecode bDur = Timecode.getInstance(bDuration, Framerate.HZ_25);

						String aEom = SegmentUtil.calculateEOM(aDuration, aStart);
						String bEom = SegmentUtil.calculateEOM(bDuration, bStart);
						Timecode aEnd = Timecode.getInstance(aEom, Framerate.HZ_25);
						Timecode bEnd = Timecode.getInstance(bEom, Framerate.HZ_25);

						if (aEnd.lt(bStart))
						{
							status = true;
						}
						else
						{
							status = false;
							break outerloop;
						}
					}
			}
		}
		catch(Throwable e)
		{
			log.error("Found null pointer exception", e);
		}
		return status;
	}


	public static Segment convertMaterialExchangeSegmentToMayamSegment(com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment in) throws InvalidTimecodeException{
		
		
		com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment filled = fillEomAndDurationOfSegment(in);
		
		String som = filled.getSOM();
		String eom = filled.getEOM();
		String title = filled.getSegmentTitle();
		
		if(title == null){
			title = "";
		}
		
		String duration= filled.getDuration();
		int number = in.getSegmentNumber();
		
		return Segment.create().in(new com.mayam.wf.attributes.shared.type.Timecode(som)).duration(new com.mayam.wf.attributes.shared.type.Timecode(duration)).number(number).title(title).build();
		
	}
	
	public static Segments convertMayamSegmentListToMediaExchangeSegments(FullProgrammePackageInfo pack)
	{
		SegmentList segmentList = pack.getSegmentList();
		String title = pack.getTitleAttributes().getAttributeAsString(Attribute.SERIES_TITLE);
		Segments ret = new Segments();

		if (segmentList != null && segmentList.getEntries() != null)
		{
			for (Segment s : segmentList.getEntries())
			{
				Programme.Media.Segments.Segment meSeg = new Programme.Media.Segments.Segment();

				String som = s.getIn().toSmpte();
				String duration = s.getDuration().toSmpte();
				Timecode startTC = Timecode.getInstance(som, Framerate.HZ_25);
				String eom = calculateEOM(duration, startTC);

				meSeg.setSOM(som);
				meSeg.setEOM(eom);
				meSeg.setDuration(duration);
				meSeg.setNumber(s.getNumber());
				meSeg.setTitle(title);

				ret.getSegment().add(meSeg);
			}
		}
		
		//sort by segment number
		Collections.sort(ret.getSegment(), new Comparator<SegmentListType.Segment>()
		{

			@Override
			public int compare(
					com.mediasmiths.foxtel.generated.mediaexchange.SegmentListType.Segment o1,
					com.mediasmiths.foxtel.generated.mediaexchange.SegmentListType.Segment o2)
			{
				return Integer.compare(o1.getNumber(), o2.getNumber());
			}

		});

		return ret;
	}
	
	public static SegmentationInformation convertMayamSegmentListToMaterialExportSegments(FullProgrammePackageInfo pack)
	{
		SegmentList segmentList = pack.getSegmentList();
		String title = pack.getTitleAttributes().getAttributeAsString(Attribute.SERIES_TITLE);
		SegmentationInformation ret = new SegmentationInformation();

		if (segmentList != null && segmentList.getEntries() != null)
		{
			for (Segment s : segmentList.getEntries())
			{
				SegmentType meSeg = new SegmentType();

				String som = s.getIn().toSmpte();
				String duration = s.getDuration().toSmpte();
				Timecode startTC = Timecode.getInstance(som, Framerate.HZ_25);
				String eom = calculateEOM(duration, startTC);

				meSeg.setSegmentStart(som);
				meSeg.setSegmentEnd(eom);
				meSeg.setSegmentNumber(s.getNumber());
				meSeg.setSegmentTitle(title);
				ret.getSegment().add(meSeg);			
			}
		}
		
		//sort by segment number
		Collections.sort(ret.getSegment(), new Comparator<SegmentType>()
		{

			@Override
			public int compare(
					SegmentType o1,
					SegmentType o2)
			{
				return Integer.compare(o1.getSegmentNumber(), o2.getSegmentNumber());
			}

		});

		return ret;
	}
	
	public static com.mediasmiths.foxtel.generated.outputruzz.SegmentListType.Segment convertMayamSegmentToRuzzSegment(Segment s, String title){
		
		com.mediasmiths.foxtel.generated.outputruzz.SegmentListType.Segment rzSeg = new com.mediasmiths.foxtel.generated.outputruzz.SegmentListType.Segment();
		
		String som = s.getIn().toSmpte();
		String duration = s.getDuration().toSmpte();
		Timecode startTC = Timecode.getInstance(som,Framerate.HZ_25);
		String eom = calculateEOM(duration, startTC);
		
		rzSeg.setSOM(som);
		rzSeg.setEOM(eom);
		rzSeg.setDuration(duration);
		rzSeg.setNumber(s.getNumber());
		rzSeg.setTitle(title);
		
		return rzSeg;
	}

	public static Segment convertRuzzSegmentToMayamSegment(com.mediasmiths.foxtel.generated.ruzz.SegmentationType.Segment ruzzSeg) throws InvalidTimecodeException
	{
		return Segment.create().in(new com.mayam.wf.attributes.shared.type.Timecode(ruzzSeg.getSOM())).duration(new com.mayam.wf.attributes.shared.type.Timecode(ruzzSeg.getEOM())).number(ruzzSeg.getSegmentNumber()).title(ruzzSeg.getSegmentTitle()).build();
	}

	public static SegmentList convertRuzzSegmentTypeToMayamSegmentList(com.mediasmiths.foxtel.generated.ruzz.SegmentationType seglist) throws InvalidTimecodeException
	{

		SegmentList.SegmentListBuilder listbuilder = SegmentList.create();

		for (com.mediasmiths.foxtel.generated.ruzz.SegmentationType.Segment seg : seglist.getSegment())
		{
		     listbuilder.segment(convertRuzzSegmentToMayamSegment(seg));
		}

		return listbuilder.build();

	}
	
	public static String ruzzSegmentTypeToHumanString(com.mediasmiths.foxtel.generated.ruzz.SegmentationType seglist)
	{
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("N____SOM_______DURATION_____EOM_______TITLE\n");
		
		for (com.mediasmiths.foxtel.generated.ruzz.SegmentationType.Segment seg : seglist.getSegment())
		{
			sb.append(ruzzSegmentToString(seg));
		}
		
		return sb.toString();
	}

	protected static String ruzzSegmentToString(com.mediasmiths.foxtel.generated.ruzz.SegmentationType.Segment seg)
	{
		
		int segmentNumber = seg.getSegmentNumber();
		String som = seg.getSOM();
		String eom = seg.getEOM();
		String title = seg.getSegmentTitle();
		Timecode start =  Timecode.getInstance(som, Framerate.HZ_25); 
		Timecode end =  Timecode.getInstance(eom, Framerate.HZ_25);
		String duration = getDurationSMTPE(start,end);
		
		return segmenttoString(segmentNumber,som,duration,eom,title);
	}
	
	private static String getDurationSMTPE(Timecode start, Timecode end)
	{
		TimecodeRange range = new TimecodeRange(start,end);

		final SampleCount duration = range.getDuration().add(new SampleCount(1, Framerate.HZ_25));

		Timecode durationTimecode = Timecode.getInstance(duration);

		return durationTimecode.toSMPTEString();
	}

	public static String originalConformToHumanString(SegmentationType originalConform)
	{
		List<com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment> segment = originalConform.getSegment();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("N____SOM_______DURATION_____EOM_______TITLE\n");
		
		for (com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s: segment)
		{
			sb.append(materialExchangeSegmentToString(s));
		}
		
		return sb.toString();
	}

	public static String presentationToHumanString(Presentation presentation)
	{
		List<Package> packages = presentation.getPackage();

		StringBuilder sb = new StringBuilder();
		
		for (Package packge : packages)
		{
			sb.append(packge.getPresentationID());
			sb.append("\n");
			sb.append("N____SOM_______DURATION_____EOM_______TITLE\n");
			
			List<com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment> segments = packge.getSegmentation().getSegment();
			
			for (com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment segment : segments)
			{
				sb.append(materialExchangeSegmentToString(segment));
			}
			
			sb.append("\n");
		}
		
		return sb.toString();
	}

	public static String getDuration(String som, String eom)
	{
		log.debug(String.format("GetDuration som %s eom %s",som,eom));
		Timecode in = Timecode.getInstance(som, Framerate.HZ_25);
		Timecode out = Timecode.getInstance(eom, Framerate.HZ_25);
		Timecode duration = out.subtract(in.getSampleCount());
		log.debug(String.format("duration %s",duration));
		return duration.toSMPTEString();
	}
	
	
}
