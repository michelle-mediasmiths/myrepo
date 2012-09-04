package com.medismiths.foxtel.mpa;

import java.io.File;

import com.mediasmiths.foxtel.generated.MediaExchange.Programme;

public class PendingImport {

	public File getXmlFile() {
		return xmlFile;
	}

	public File getMediaFile() {
		return mediaFile;
	}

	public Programme getProgramme() {
		return programme;
	}

	private final File xmlFile;
	private final File mediaFile;
	private final Programme programme;
	
	public PendingImport(File xmlFile, File mediaFile, Programme programme){
		this.xmlFile = xmlFile;
		this.mediaFile = mediaFile;
		this.programme = programme;		
	}
	
}
