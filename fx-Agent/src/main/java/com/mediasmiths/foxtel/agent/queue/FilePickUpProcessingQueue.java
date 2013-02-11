package com.mediasmiths.foxtel.agent.queue;

import java.io.File;
import java.util.List;

/**
 *
 * Provides an abstraction of a file processing queue.
 * The queue is set to be active or passive.
 *
 * Author: Harmer
 */
public interface FilePickUpProcessingQueue
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
	public File take();

	/**
	 * Add a file to the list of files that are ready for processing.
	 *
	 * @param filePath the path to add to the processing queue
	 */
	public void add(String filePath);

	/**
	 *
	 * @return the list of pending files in the processing queue
	 */
	public List<String> getPending();

	/**
	 *
	 * @return the length of the queue file queue
	 */
	public long size();

	/**
	 * remove all file that are currently in the pending queue
	 */
	public void clear();

	/**
	 * signal FilePickupProcessingQueue should shutdown
	 */
	public void shutdown();
}
