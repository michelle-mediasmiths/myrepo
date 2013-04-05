package com.mediasmiths.foxtel.channels.config;

import java.util.List;
import java.util.Set;

public interface ChannelProperties {
	boolean isValidNameForTag(String channelTag, String channelName);
	boolean isValidFormatForTag(String channelTag, String channelFormat);
	boolean isTagValid(String channelTag);
	String channelGroupForChannel(String channelTag);
	String exportPathForChannelGroup(String channelGroupName);
	Set<String> groupsForChannels(List<String> channelTags);
}
