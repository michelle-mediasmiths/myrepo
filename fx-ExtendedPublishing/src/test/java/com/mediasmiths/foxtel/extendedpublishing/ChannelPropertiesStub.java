package com.mediasmiths.foxtel.extendedpublishing;

import com.mediasmiths.foxtel.channels.config.ChannelProperties;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChannelPropertiesStub implements ChannelProperties
{

	@Override
	public boolean isValidNameForTag(String channelTag, String channelName)
	{
		return true;
	}

	@Override
	public boolean isTagValid(String channelTag)
	{
		if ("FOX".equals(channelTag))
		{
			return true;
		}
		return false;
	}

	@Override
	public String channelGroupForChannel(String channelTag)
	{
		if ("FOX".equals(channelTag))
		{
			return "GE";
		}

		return "unknown";
	}

	@Override
	public String exportPathForChannelGroup(String channelGroupName)
	{
		if ("GE".equals(channelGroupName))
		{
			return "General Entertainment";
		}

		return "unknown";
	}

	@Override
	public Set<String> groupsForChannels(List<String> channelTags)
	{
		Set<String> ret = new HashSet<String>();

		for (String tag : channelTags)
		{
			ret.add(channelGroupForChannel(tag));
		}
		return ret;

	}

	@Override
	public Collection<? extends String> groupsForEmail(List<String> channels, boolean isAo)
	{
		Set<String> result = groupsForChannels(channels);
		
		if(isAo){
			result.add("AO");
		}
		return result;
	}

	@Override
	public String nameForTag(String channelTag)
	{
		if ("FOX".equals(channelTag))
		{
			return "FOX 8";
		}

		return null;
	}

}
