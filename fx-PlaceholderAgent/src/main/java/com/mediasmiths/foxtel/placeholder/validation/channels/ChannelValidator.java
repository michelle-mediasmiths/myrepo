package com.mediasmiths.foxtel.placeholder.validation.channels;

public interface ChannelValidator {
	boolean isValidNameForTag(String channelTag, String channelName);
	boolean isValidFormatForTag(String channelTag, String channelFormat);
	boolean isTagValid(String channelTag);
}
