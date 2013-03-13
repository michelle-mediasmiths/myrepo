package com.mediasmiths.mq.transferqueue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A persistent collection of TransferItem records, backed by a queue folder.<br />
 * Returns consistent immutable iterators that allow concurrent traversal while the backing list is being modified.
 */
@Singleton
public class TransferQueue
{
	private static final Logger log = Logger.getLogger(TransferQueue.class);
	private static final JAXBSerialiser SERIALISER = JAXBSerialiser.getInstance(TransferItem.class);

	private final List<TransferItem> list = new CopyOnWriteArrayList<>();
	private final File folder;


	@Inject
	public TransferQueue(@Named("TransferManager.queueFolder") File folder)
	{
		if (!folder.exists())
			throw new IllegalArgumentException("Transfer Manager Queue Folder does not exist! " + folder.getAbsolutePath());
		if (!folder.isDirectory())
			throw new IllegalArgumentException("Transfer Manager Queue Folder is not a folder! " + folder.getAbsolutePath());

		this.folder = folder;

		load();
	}


	/**
	 * Reload the list by reading the backing directory
	 */
	public synchronized void load()
	{
		list.clear();
		// load the contents of the directory

		for (File file : getFiles())
		{
			final TransferItem item = thaw(file);

			if (item != null)
				list.add(item);
		}
	}


	/**
	 * @return an immutable list containing the active items. this list will not reflect any calls to remove() after the call to
	 *         getItems
	 */
	public List<TransferItem> getItems()
	{
		return Collections.unmodifiableList(this.list);
	}


	/**
	 * Remove an item from the queue
	 *
	 * @param item
	 * 		the transfer item
	 */
	public synchronized void remove(TransferItem item)
	{
		if (item == null)
			throw new IllegalArgumentException("Cannot remove null item!");

		// remove from list
		list.remove(item);

		// remove from disk
		final File file = getFile(item);

		if (file.exists())
		{
			final boolean success = file.delete();

			if (!success)
				log.warn("Could not delete TransferItem file: " + file.getAbsolutePath());
		}
	}


	/**
	 * Add an item to the queue
	 *
	 * @param item
	 * 		the transfer item
	 */
	public synchronized void add(TransferItem item)
	{
		if (item == null)
			throw new IllegalArgumentException("Cannot add null item!");

		// add to list
		list.add(item);

		// serialise to disk
		freeze(item, getFile(item));
	}


	private void freeze(TransferItem item, File file)
	{
		SERIALISER.serialise(item, file);
	}


	private TransferItem thaw(File file)
	{
		return (TransferItem) SERIALISER.deserialise(file);
	}


	/**
	 * Retrieve all the files in the queue folder
	 *
	 * @return a list of files in the queue folder (or an empty list if there are no files in the folder)
	 */
	private List<File> getFiles()
	{
		final List<File> list = new ArrayList<>();

		final File[] files = folder.listFiles();

		if (files != null)
			for (File file : files)
				if (file.isFile())
					list.add(file);

		return list;
	}


	/**
	 * Determines the file for an item
	 *
	 * @param item
	 *
	 * @return
	 */
	private File getFile(TransferItem item)
	{
		return new File(folder, item.id + ".xml");
	}
}
