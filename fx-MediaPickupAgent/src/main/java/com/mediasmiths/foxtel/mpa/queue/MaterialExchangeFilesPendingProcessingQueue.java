package com.mediasmiths.foxtel.mpa.queue;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.queue.FilePickUpFromDirectories;
import com.mediasmiths.foxtel.agent.queue.MultiFilePickUp;

public class MaterialExchangeFilesPendingProcessingQueue extends MultiFilePickUp{

	@Inject
	public MaterialExchangeFilesPendingProcessingQueue(@Named("mex.watched.directories") File[] pickupDirectories)
	{
		super(pickupDirectories, "xml","mxf");
	}

}
