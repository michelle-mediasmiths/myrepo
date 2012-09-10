package com.medismiths.foxtel.mpa;

import java.io.File;

import com.mediasmiths.foxtel.generated.MaterialExchange.Material;

public class PendingImport {

	public File getXmlFile() {
		return xmlFile;
	}

	public File getMediaFile() {
		return mediaFile;
	}

	public Material getMaterial() {
		return material;
	}

	private final File xmlFile;
	private final File mediaFile;
	private final Material material;
	
	public PendingImport(File xmlFile, File mediaFile, Material material){
		this.xmlFile = xmlFile;
		this.mediaFile = mediaFile;
		this.material = material;		
	}
	
}
