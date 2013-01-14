package com.mediasmiths.mayam.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media.Segments;
import com.mediasmiths.foxtel.generated.ruzz.SegmentListType;
import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.SampleCount;
import com.mediasmiths.std.types.Timecode;

public class SegmentUtil
{
	private static Logger log = Logger.getLogger(SegmentUtil.class);
	
	private static Pattern stringToSeg = Pattern.compile("Number:\\{([0-9]*)\\},Title:\\{(.*)\\},SOM:\\{(.*)\\},EOM:\\{(.*)\\},Duration:\\{(.*)\\}\n");
	
	public static String segmentToString(com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s)
	{
		return String.format("Number:{%d},Title:{%s},SOM:{%s},EOM:{%s},Duration:{%s}\n",s.getSegmentNumber(),s.getSegmentTitle(), s.getSOM(),s.getEOM(),s.getDuration());
	}
	
	public static com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment stringToSegment(String str) throws IllegalArgumentException
	{
		com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s =  new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
		
		Matcher m  = stringToSeg.matcher(str);
		
		if(m.matches()){
			int number = Integer.parseInt(m.group(1));
			String title = m.group(2);
			String som = m.group(3);
			String eom = m.group(4);
			String duration = m.group(5);
			
			s.setSegmentNumber(number);
			s.setSegmentTitle(title);
			s.setSOM(som);
			s.setDuration(duration);
			return s;
		}
		
		throw new IllegalArgumentException("could not parse supplied string as segment info: "+str);
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
			long durationSamples = (end.getSampleCount().getSamples() - start.getSampleCount().getSamples());
			Timecode durationTimecode = Timecode.getInstance(new SampleCount(durationSamples, Framerate.HZ_25));
			duration = durationTimecode.toSMPTEString();
		}
		
		//filled will hae some, eom and duration populated, should not be written to file as it will no longer be schema conformating (duration and eom are an xs:choice at the same level);
		com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment filled = new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
		filled.setDuration(duration);
		filled.setEOM(eom);
		filled.setSOM(som);
		filled.setSegmentNumber(s.getSegmentNumber());
		filled.setSegmentTitle(s.getSegmentTitle());
		
		return filled;
	}

	private static String calculateEOM(String duration, Timecode start)
	{
		String eom;
		Timecode durationTimecode =  Timecode.getInstance(duration, Framerate.HZ_25);
		long startSamples = start.getSampleCount().getSamples();
		long durationSamples = durationTimecode.getSampleCount().getSamples();
		long endSamples = startSamples+durationSamples;
		Timecode endTimecode = Timecode.getInstance(new SampleCount(endSamples, Framerate.HZ_25));
		eom = endTimecode.toSMPTEString();
		return eom;
	}
	
	public static String totalDuration(List<Segment> segments){
		
		Timecode total = Timecode.getInstance(new SampleCount(0l, Framerate.HZ_25));
		
		for(Segment s : segments){
			log.trace(String.format("Segment %s duration %s", s.getNumber(), s.getDuration().toSmpte()));
			total = total.add(new SampleCount(s.getDuration().getTotalFrames(), Framerate.HZ_25));
		}
		
		return total.toSMPTEString();
		
	}

	public static Segments convertMayamSegmentListToMediaExchangeSegments(SegmentList segmentList)
	{
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
				meSeg.setTitle(s.getTitle());

				ret.getSegment().add(meSeg);
			}
		}

		return ret;
	}
	
	public static com.mediasmiths.foxtel.generated.ruzz.SegmentListType.Segment convertMayamSegmentToRuzzSegment(Segment s){
		
		com.mediasmiths.foxtel.generated.ruzz.SegmentListType.Segment rzSeg = new com.mediasmiths.foxtel.generated.ruzz.SegmentListType.Segment();
		
		String som = s.getIn().toSmpte();
		String duration = s.getDuration().toSmpte();
		Timecode startTC = Timecode.getInstance(som,Framerate.HZ_25);
		String eom = calculateEOM(duration, startTC);
		
		rzSeg.setSOM(som);
		rzSeg.setEOM(eom);
		rzSeg.setDuration(duration);
		rzSeg.setNumber(s.getNumber());
		rzSeg.setTitle(s.getTitle());
		
		return rzSeg;
	}
}
