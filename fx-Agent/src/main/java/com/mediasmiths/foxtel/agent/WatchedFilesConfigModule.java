package com.mediasmiths.foxtel.agent;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;

import java.io.File;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class WatchedFilesConfigModule extends AbstractModule
{

	Logger logger = Logger.getLogger(WatchedFilesConfigModule.class);


	@Override
	protected void configure()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Singleton
	@Provides
	@Named("filepickup.watched.directories")
	public File[] provideWatchedDirectories(@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders)
	{
		
		if (watchFolders == null || watchFolders.isEmpty())
		{
			logger.error("No Directory Watched Paths defined in filepickup.watched.directoryNames");

			throw new IllegalArgumentException("No Directory Watched Paths defined in filepickup.watched.directoryNames");
		}


		File[] watchedPaths = new File[watchFolders.size()];

		for (int i=0; i<watchFolders.size(); i++)
		{
			watchedPaths[i] = new File(watchFolders.get(i).getSource());
			if (!acceptableFilePermissions(watchedPaths[i]))
			{
				logger.error("Watched path is not usable: " + watchedPaths[i]);

				throw new IllegalArgumentException("Watched path is not usable: " + watchedPaths[i]);

			}
		}

		return watchedPaths;

	}

	/**
	 *
	 * @param fileRef of the file being considered
	 * @return true if the file has permissions that will enable its correct processing by a FileProcessing instance
	 */
	private boolean acceptableFilePermissions(final File fileRef)
	{
		return fileRef.exists() && fileRef.isDirectory() && fileRef.canRead() && fileRef.canWrite();
	}


}
