package com.mediasmiths.foxtel.agent.queue;

import java.io.File;

public interface IFilePickup
{

	/**
	 *
	 * @param isActive true if the processing queue should be active and false otherwise
	 */
	public void setState(boolean isActive);

	/**
	 *
	 * @return the next file available in the processing queue
	 */
	public PickupPackage take();

	/**
	 * signal FilePickupProcessingQueue should shutdown
	 */
	public void shutdown();

	/**
	 * returns a list of directories watched by this FilePickupProcessingQueue
	 */
	public File[] getWatchedDirectories();

}
