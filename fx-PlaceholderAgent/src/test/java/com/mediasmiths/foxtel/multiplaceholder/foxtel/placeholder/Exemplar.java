package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder;

import com.mediasmiths.foxtel.generated.mediaexchange.AudioTrackType;
import org.junit.Ignore;

import java.util.HashMap;
import java.util.Map;

@Ignore
public class Exemplar
{

	private static Map<String, AudioTrackType> mayamChannelMapping = new HashMap<String,AudioTrackType>();


	static
	{
		mayamChannelMapping.put("STEREO_LEFT", AudioTrackType.LEFT);
		mayamChannelMapping.put("STEREO_RIGHT", AudioTrackType.RIGHT);
		mayamChannelMapping.put("FRONT_LEFT", AudioTrackType.LEFT);
		mayamChannelMapping.put("FRONT_RIGHT", AudioTrackType.RIGHT);
		mayamChannelMapping.put("CENTER", AudioTrackType.MONO);
		mayamChannelMapping.put("LFE", AudioTrackType.MONO);
		mayamChannelMapping.put("SURROUND_LEFT", AudioTrackType.LEFT);
		mayamChannelMapping.put("SURROUND_RIGHT", AudioTrackType.RIGHT);

	}

	/**
	 *
	 * As per MAM-202 Jonas states that
	 *
	 * example  data we can see STEREO_LEFT, STEREO_RIGHT, FRONT_LEFT, FRONT_RIGHT, CENTER, LFE, SURROUND_LEFT and SURROUND_RIGHT
	 *
	 * @param audioTrackName
	 * @return
	 */
	private static AudioTrackType mapMayamAudioTrackEncodingtoMediaExhchangeEncoding(String audioTrackName)
	{
		AudioTrackType type = mayamChannelMapping.get(audioTrackName);

		if (type == null)
		{
			System.out.println("Do not have a mapping for channel name" + audioTrackName);
			type = AudioTrackType.MONO;
		}

		return type;
	}


	public static void main(String[] args)
	{
		System.out.println(mapMayamAudioTrackEncodingtoMediaExhchangeEncoding("STEREO_LEFT"));
		System.out.println(mapMayamAudioTrackEncodingtoMediaExhchangeEncoding("STEREO_RIGHT"));
		System.out.println(mapMayamAudioTrackEncodingtoMediaExhchangeEncoding("FRONT_LEFT"));
		System.out.println(mapMayamAudioTrackEncodingtoMediaExhchangeEncoding("FRONT_RIGHT"));
		System.out.println(mapMayamAudioTrackEncodingtoMediaExhchangeEncoding("CENTER"));
		System.out.println(mapMayamAudioTrackEncodingtoMediaExhchangeEncoding("LFE"));
		System.out.println(mapMayamAudioTrackEncodingtoMediaExhchangeEncoding("SURROUND_LEFT"));
		System.out.println(mapMayamAudioTrackEncodingtoMediaExhchangeEncoding("SURROUND_RIGHT"));

	}
}
