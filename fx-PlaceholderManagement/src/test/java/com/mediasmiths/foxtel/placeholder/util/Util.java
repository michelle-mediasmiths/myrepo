package com.mediasmiths.foxtel.placeholder.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

public class Util {

	
	public static String prepareTempFolder(String description) throws IOException {
		//create a random folder		
		String path = FileUtils.getTempDirectoryPath() + IOUtils.DIR_SEPARATOR + RandomStringUtils.randomAlphabetic(10) + IOUtils.DIR_SEPARATOR + description;
				
		File dir = new File(path);
		
		if(dir.exists()){
			FileUtils.cleanDirectory(dir);
		}
		else{
			dir.mkdirs();
		}
		
		return path;
	}
}
