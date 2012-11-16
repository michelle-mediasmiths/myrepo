package com.mediasmiths.mayam.accessrights;

import java.util.HashMap;
import java.util.Map;

public class MayamChannelGroups {
	public static final Map<String, String> channelGroupOwnerMap = new HashMap<String, String>()
	{
		private static final long serialVersionUID = 1L;

		{
			//General Entertainment
			put("FOX8", "GE");
			put("111 HITS", "GE");
			put("COMEDY", "GE");
			put("SOHO", "GE");
			put("ARENA", "GE");
			
			//Lifestyle
			put("LIFESTYLE", "LS");
			put("FOOD", "LS");
			put("HOME", "LS");
			put("YOU", "LS");
			
			//On Demand
			put("ON DEMAND", "OD");
			
			//Main Event
			put("MAIN EVENT", "ME");
			
			//Adults Only
			put("AO", "AO");
			
			//Factuals
			put("CRIME", "FC");
			put("BIO FACTUALS", "FC");
			put("A&E", "FC");
			put("HISTORY", "FC");
			
		}
	};
}
