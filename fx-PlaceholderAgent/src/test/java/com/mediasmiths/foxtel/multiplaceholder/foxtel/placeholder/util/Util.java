package com.mediasmiths.foxtel.multiplaceholder.foxtel.placeholder.util;

import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

public class Util {

	public static String prepareTempFolder(String description)
			throws IOException {
		// create a random folder
		String path = FileUtils.getTempDirectoryPath() + IOUtils.DIR_SEPARATOR
				+ RandomStringUtils.randomAlphabetic(10)
				+ IOUtils.DIR_SEPARATOR + description;

		File dir = new File(path);

		if (dir.exists()) {
			FileUtils.cleanDirectory(dir);
		} else {
			dir.mkdirs();
		}

		return path;
	}

	public static void deleteFiles(String... paths) {
		String env = System.getenv("FOXTEL_UNIT_TEST_TEMPFILES");
		if (!(env != null && env.toLowerCase(Locale.getDefault()).equals("keep"))) {
			for (String path : paths) {
				FileUtils.deleteQuietly(new File(path));
			}
		}
	}
	
	public static void deleteFiles(PickupPackage pp){
		Collection<File> allFiles = pp.getAllFiles();
		for (File file : allFiles)
		{
			file.delete();
		}
	
	}
}
