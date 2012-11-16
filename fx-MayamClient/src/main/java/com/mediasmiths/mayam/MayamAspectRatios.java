package com.mediasmiths.mayam;

import java.util.HashMap;
import java.util.Map;

public class MayamAspectRatios {
	public static final Map<String, String> mayamAspectRatioMappings = new HashMap<String, String>()
	{
		private static final long serialVersionUID = 1L;

		{
			// 16:9 Full Frame
			put("16F16", "ff");
			
			// 16:9 Pillar Boxed
			put("12P16", "pb");
			
			// 16:9 Resized
			put("12R16", "rz");
			
			// 16:9 Displayed in 4:3 Letter Box
			put("16L12", "lb");
			
			// 16:9 Displayed in 4:3 Centre Cut
			put("16C12", "cc");		
		}
	};
}
