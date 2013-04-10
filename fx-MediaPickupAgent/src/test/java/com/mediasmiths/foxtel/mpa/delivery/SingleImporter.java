package com.mediasmiths.foxtel.mpa.delivery;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.ip.event.EventService;

public class SingleImporter extends Importer
{

	@Inject
	public SingleImporter(
			@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptsToMake,
			EventService event)
	{
		super(watchFolders, deliveryAttemptsToMake, event);
	}


}
