package com.medismiths.foxtel.mpa;

import java.io.File;

import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.medismiths.foxtel.mpa.processing.MaterialMessageEnvelope;

public class PendingImport {

	public File getMediaFile() {
		return mediaFile;
	}

	public MaterialMessageEnvelope getMaterialEnvelope() {
		return material;
	}

	private final File mediaFile;
	private final MaterialMessageEnvelope material;
	
	public PendingImport(File mediaFile, MaterialMessageEnvelope material){
		this.mediaFile = mediaFile;
		this.material = material;		
	}
	
}
