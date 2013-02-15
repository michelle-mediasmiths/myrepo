package com.mediasmiths.foxtel.channels.config;

public interface ChannelProperties {
	boolean isValidNameForTag(String channelTag, String channelName);
	boolean isValidFormatForTag(String channelTag, String channelFormat);
	boolean isTagValid(String channelTag);
	String channelGroupForChannel(String channelTag);
	String exportPathForChannelGroup(String channelGroupName);
}
