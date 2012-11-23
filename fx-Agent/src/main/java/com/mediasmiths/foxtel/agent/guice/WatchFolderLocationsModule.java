package com.mediasmiths.foxtel.agent.guice;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mediasmiths.std.io.PropertyFile;

public class WatchFolderLocationsModule extends AbstractModule {


	private static Logger logger = Logger
			.getLogger(WatchFolderLocationsModule.class);

	
	@Provides
	@Named("watchfolder.locations")
	public List<String> provideWatchFolderLocations(@Named("watchfolder.count") Integer count,
			@Named("service.properties") PropertyFile conf)
	{
		logger.trace("watchfolder.count"+count);
		
		final List<String> locations= new ArrayList<String>();

		for (int i = 0; i < count; i++)
		{
			
			String propName = "watchfolder.locations[" + i + "].path";
			String val = conf.get(propName);
			logger.trace(String.format("%s=%s", propName,val));
			locations.add(val);
		}

		return locations;
	}

	@Override
	protected void configure() {
	}
	
}
