package com.mediasmiths.foxtel.mpa.queue;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.queue.FilePickUpFromDirectories;

public class RuzzFilesPendingProcessingQueue extends FilePickUpFromDirectories{

	@Inject
	public RuzzFilesPendingProcessingQueue(@Named("ruzz.watched.directories") File[] pickupDirectories)
	{
		super(pickupDirectories);
	}

}
