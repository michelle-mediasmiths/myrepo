package com.mediasmiths.foxtel.cerify.medialocation;

public class MediaLocationNotFoundException extends Exception
{

	private static final long serialVersionUID = 1L;

	public MediaLocationNotFoundException(String mediaLocationName)
	{
		super(mediaLocationName);
	}

}
