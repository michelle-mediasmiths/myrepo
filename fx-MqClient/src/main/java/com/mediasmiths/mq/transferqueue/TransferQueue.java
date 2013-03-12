package com.mediasmiths.mq.transferqueue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class TransferQueue
{
	private static final JAXBSerialiser SERIALISER = JAXBSerialiser.getInstance(TransferItem.class);

	private final List<TransferItem> list = new CopyOnWriteArrayList<>();
	private final File folder;


	@Inject
	public TransferQueue(@Named("TransferManager.queueFolder") File folder)
	{
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


	public synchronized void remove(TransferItem item)
	{
		// remove from list
		list.remove(item);

		// remove from disk
		getFile(item).delete();
	}


	public synchronized void add(TransferItem item)
	{
		// add to list
		list.add(item);

		// serialise to disk
		freeze(item, getFile(item));
	}


	protected void freeze(TransferItem item, File file)
	{
		SERIALISER.serialise(item, file);
	}


	protected TransferItem thaw(File file)
	{
		return (TransferItem) SERIALISER.deserialise(file);
	}


	protected List<File> getFiles()
	{
		final List<File> list = new ArrayList<>();

		final File[] files = folder.listFiles();

		if (files != null)
			for (File file : files)
				if (file.isFile())
					list.add(file);

		return list;
	}


	private File getFile(TransferItem item)
	{
		return new File(folder, item.id + ".xml");
	}
}
