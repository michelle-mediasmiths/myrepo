package com.mediasmiths.foxtel.channels.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ChannelProperties {
	boolean isValidNameForTag(String channelTag, String channelName);
	boolean isTagValid(String channelTag);
	String channelGroupForChannel(String channelTag);
	String exportPathForChannelGroup(String channelGroupName);
	Set<String> groupsForChannels(List<String> channelTags);
	Collection<? extends String> groupsForEmail(List<String> channels, boolean isAo);
	String nameForTag(String channelTag);
}
