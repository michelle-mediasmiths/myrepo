package com.mediasmiths.foxtel.mpa;

public class PendingImport
{

	public MediaEnvelope getMaterialEnvelope()
	{
		return material;
	}

	private final MediaEnvelope material;

	public PendingImport(MediaEnvelope material)
	{
		this.material = material;
	}

}
