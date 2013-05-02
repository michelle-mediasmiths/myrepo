package com.mediasmiths.foxtel.channels.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.std.io.PropertyFile;

public class ChannelConfigModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(ChannelProperties.class).to(ChannelPropertiesImpl.class);
	}

	@Provides
	@Singleton
	@Named("channel.names")
	public PropertyFile provideChannelNames(@Named("channel.names.config.location") String pathToPropertiesFile)
	{
		return PropertyFile.find(pathToPropertiesFile);
	}
	
	@Provides
	@Singleton
	@Named("channel.groups")
	public PropertyFile provideChannelGroupsMap(@Named("channel.groups.map.location") String pathToPropertiesFile)
	{

		return PropertyFile.find(pathToPropertiesFile);
	}
	
	@Provides
	@Singleton
	@Named("groups.export.paths")
	public PropertyFile provideChannelGroupsExportPaths(@Named("groups.export.path.map.config.location") String pathToPropertiesFile)
	{

		return PropertyFile.find(pathToPropertiesFile);
	}
}
