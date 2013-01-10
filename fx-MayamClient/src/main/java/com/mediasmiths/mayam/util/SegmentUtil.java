package com.mediasmiths.mayam.util;

import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.SampleCount;
import com.mediasmiths.std.types.Timecode;

public class SegmentUtil
{
	public static String segmentToString(com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s)
	{
		return String.format("Number:{%d},Title:{%s},SOM:{%s},EOM:{%s},Duration:{%s}\n",s.getSegmentNumber(),s.getSegmentTitle(), s.getSOM(),s.getEOM(),s.getDuration());
	}
	
	public static com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment stringToSegment(String s) throws IllegalArgumentException
	{
		return new com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment();
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
			Timecode durationTimecode =  Timecode.getInstance(duration, Framerate.HZ_25);
			long startSamples = start.getSampleCount().getSamples();
			long durationSamples = durationTimecode.getSampleCount().getSamples();
			long endSamples = startSamples+durationSamples;
			Timecode endTimecode = Timecode.getInstance(new SampleCount(endSamples, Framerate.HZ_25));
			eom = endTimecode.toSMPTEString();
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
}
