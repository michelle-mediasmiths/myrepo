package com.mediasmiths.foxtel.channels.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.std.io.PropertyFile;

public class ChannelPropertiesImpl implements ChannelProperties {

	private final static Logger log = Logger.getLogger(ChannelPropertiesImpl.class);
	
	@Inject
	@Named("channel.names")
	private PropertyFile channelNames;
	@Inject
	@Named("channel.groups")
	private PropertyFile channelGroupsMap;
	@Inject
	@Named("groups.export.paths")
	private PropertyFile channelGroupsExportPaths;
	
	@Inject
	public ChannelPropertiesImpl() throws IOException
	{
	}
	
	@Override
	public boolean isValidNameForTag(String channelTag, String channelName) {
		String expectedName = channelNames.get(channelTag);
		return channelName.equals(expectedName);
	}

	@Override
	public boolean isTagValid(String channelTag)
	{
		return channelNames.containsKey(channelTag);
	}

	@Override
	public String channelGroupForChannel(String channelTag)
	{
		return channelGroupsMap.get(channelTag);
	}

	@Override
	public String exportPathForChannelGroup(String channelGroupName)
	{
		return channelGroupsExportPaths.get(channelGroupName);
	}

	/**
	 * returns the set of channel groups that the supplied channels are associated with
	 */
	@Override
	public Set<String> groupsForChannels(List<String> channelTags)
	{
		
		Set<String> groups = new HashSet<String>();
		
		if(channelTags==null || channelTags.isEmpty()){
			return groups;
		}
		
		for (String channel : channelTags)
		{

			String group = channelGroupForChannel(channel);
			log.trace(String.format("channel %s group %s", channel, group));

			if (group != null)
			{
				groups.add(group);
			}

		}
		
		return groups;
		
	}

}
