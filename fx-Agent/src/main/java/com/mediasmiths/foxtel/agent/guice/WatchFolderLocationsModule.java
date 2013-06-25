package com.mediasmiths.foxtel.agent.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolder;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.validation.ConfigValidationFailureException;
import com.mediasmiths.std.io.PropertyFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;

public class WatchFolderLocationsModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(WatchFolderLocationsModule.class);

	@Singleton
	@Provides
	@Named("watchfolder.locations")
	public WatchFolders provideWatchFolderLocations(
			@Named("watchfolder.count") Integer count,
			@Named("service.properties") PropertyFile conf,
			@Named("watchfolder.dst.required") boolean destinationsRequired)
			throws ConfigValidationFailureException {
		logger.trace("watchfolder.count " + count);

		final WatchFolders locations = new WatchFolders();

		for (int i = 0; i < count; i++) { //there is probably a prettier way to load an array\list\set of properties than this, but I dont know what it is!
			
			//get source path
			String propName = "watchfolder.locations[" + i + "].src.path";
			String val = conf.get(propName);
			logger.trace(String.format("%s=%s", propName, val));
			WatchFolder watchFolder = new WatchFolder(val);

			validateFolder(val);

			//get destination path if required
			String destPropName = "watchfolder.locations[" + i + "].dst.path";

			if (destinationsRequired) {
				String dstFolder = conf.get(destPropName);
				logger.trace(String.format("%s=%s", destPropName, dstFolder));

				validateFolder(dstFolder);
				watchFolder.setDelivery(dstFolder);
			}
			
			//check if this folder is for ao processing
			String aoflagName = "watchfolder.locations[" + i + "].ao";
			if(conf.containsKey(aoflagName)){
	
				String aoflagval = conf.get(aoflagName);
				logger.trace(String.format("%s=%s", aoflagName, aoflagval));
				if(aoflagval.toLowerCase().equals("true")){
					logger.info(String.format("considering media coming from %s to be ao", val));
					watchFolder.setAO(true);
				}
			}
			
			//check if this folder is a ruzz ingest watch folder
			String ruzzIngestFlagName = "watchfolder.locations[" + i + "].ruzz";
			if(conf.containsKey(ruzzIngestFlagName)){
				
				String ruzzflagval = conf.get(ruzzIngestFlagName);
				logger.trace(String.format("%s=%s", ruzzIngestFlagName, ruzzflagval));
				if(ruzzflagval.toLowerCase().equals("true")){
					logger.info(String.format("considering media coming from %s to be ruzz ingest", val));
					watchFolder.setRuzz(true);
				}
			}
			
			String stabilityTimeParamName = "watchfolder.locations[" + i + "].stability_time";
			if(conf.containsKey(stabilityTimeParamName)){
				
				String stabilityTimestr = conf.get(stabilityTimeParamName);
				logger.trace(String.format("%s=%s", stabilityTimeParamName, stabilityTimestr));
				
				long stabilityTime = Long.parseLong(stabilityTimestr);
				watchFolder.setStabilityTime(stabilityTime);
			}

			String nameParamName = "watchfolder.locations[" + i + "].name";
			if (conf.containsKey(nameParamName))
			{

				String nameString = conf.get(nameParamName);
				logger.trace(String.format("%s=%s", nameParamName, nameString));
				watchFolder.setName(nameString);
			}
			else
			{
				logger.info("No name set for " + val + " using basename");
				watchFolder.setName(FilenameUtils.getBaseName(val));
			}


			locations.add(watchFolder);
		}

		return locations;
	}

	private void validateFolder(String path)
			throws ConfigValidationFailureException {
		if (!haveReadWritePermissions(path)) {
			String error = String.format(
					"Do not have read write permissions on %s", path);
			logger.error(error);
			throw new ConfigValidationFailureException(error);
		}
	}

	private boolean haveReadWritePermissions(String folderPath) {
		logger.info(String.format("checking permissions of folder %s", folderPath));
		File folder = new File(folderPath);
		return (folder.canRead() && folder.canWrite());
	}

	@Override
	protected void configure() {
	}

}
