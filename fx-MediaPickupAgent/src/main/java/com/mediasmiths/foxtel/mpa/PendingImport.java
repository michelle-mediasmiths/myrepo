package com.mediasmiths.foxtel.mpa;

import java.io.File;

public class PendingImport {

	public File getMediaFile() {
		return mediaFile;
	}

	public MediaEnvelope getMaterialEnvelope() {
		return material;
	}

	private final File mediaFile;
	private final MediaEnvelope material;
	
	public PendingImport(File mediaFile, MediaEnvelope material){
		this.mediaFile = mediaFile;
		this.material = material;		
	}
	
}
