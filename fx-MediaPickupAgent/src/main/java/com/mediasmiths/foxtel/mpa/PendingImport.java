package com.mediasmiths.foxtel.mpa;

import java.io.File;

public class PendingImport {

	public File getMediaFile() {
		return mediaFile;
	}

	public MaterialEnvelope getMaterialEnvelope() {
		return material;
	}

	private final File mediaFile;
	private final MaterialEnvelope material;
	
	public PendingImport(File mediaFile, MaterialEnvelope material){
		this.mediaFile = mediaFile;
		this.material = material;		
	}
	
}
