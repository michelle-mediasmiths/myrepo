package com.mediasmiths.mayam;

import java.util.HashMap;
import java.util.Map;

public class MayamAudioEncoding {
	public static final Map<String, String> mayamAudioEncodings = new HashMap<String, String>()
	{
		private static final long serialVersionUID = 1L;

		{
			put("DOLBY_E", "DOLBY_E");
			put("PCM", "LINEAR");
			put("LINEAR", "PCM");
		}
	};
}
