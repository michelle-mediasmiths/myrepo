package com.mediasmiths.foxtel.channels.config;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.std.io.PropertyFile;

public class ChannelPropertiesImpl implements ChannelProperties {

	@Inject
	@Named("channel.names")
	private PropertyFile channelNames;
	@Inject
	@Named("channel.formats")
	private PropertyFile channelFormats;
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
	
	public ChannelPropertiesImpl(PropertyFile channelNames, PropertyFile channelFormats)
	{
		this.channelNames = channelNames;
		this.channelFormats = channelFormats;	 
	}
	
	@Override
	public boolean isValidNameForTag(String channelTag, String channelName) {
		String expectedName = channelNames.get(channelTag);
		return channelName.equals(expectedName);
	}

	@Override
	public boolean isValidFormatForTag(String channelTag, String channelFormat) {
		String expectedFormat = channelFormats.get(channelTag);
		return channelFormat.equals(expectedFormat);
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

}
