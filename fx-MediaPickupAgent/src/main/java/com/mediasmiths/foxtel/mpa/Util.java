package com.mediasmiths.foxtel.mpa;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;

import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;

public final class Util {

	private Util(){
		//hide utility class constructor
	}
	
	public static MaterialType getMaterialTypeForMaterial(Material material) {

		if (isProgramme(material)) {
			return material.getTitle().getProgrammeMaterial();
		} else {
			return material.getTitle().getMarketingMaterial();
		}
	}
	
	public static boolean isProgramme(Material material){
		return isProgramme(material.getTitle());
	}
	
	public static boolean isProgramme(Title title){
		return title.getProgrammeMaterial() != null;
	}
	
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

}
