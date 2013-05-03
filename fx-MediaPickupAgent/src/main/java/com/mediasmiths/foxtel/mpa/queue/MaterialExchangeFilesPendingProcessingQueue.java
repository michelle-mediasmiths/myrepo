package com.mediasmiths.foxtel.mpa.queue;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.queue.MultiFilePickUp;

public class MaterialExchangeFilesPendingProcessingQueue extends MultiFilePickUp{

	@Inject
	public MaterialExchangeFilesPendingProcessingQueue(@Named("mex.watched.directories") File[] pickupDirectories)
	{
		super(pickupDirectories, "xml","mxf");		
	}
	
	public void setStabilityTime(long l){
		
		Set<File> keys = stabilityTimes.keySet();
		
		for (File f : keys)
		{
			stabilityTimes.put(f,Long.valueOf(l));
		}
	}
	
}
