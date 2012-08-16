package com.medismiths.foxtel.mpa.config;

public class MediaPickupAgentConfiguration
{

	private String mediaExchangeXSDPath = "MediaExchange_V1.2.xsd";
	private String mediaFolderPath = "/tmp";

	public String getMediaFolderPath()
	{
		return mediaFolderPath;
	}

	public void setMediaFolderPath(String mediaFolderPath)
	{
		this.mediaFolderPath = mediaFolderPath;
	}

	public String getMediaExchangeXSD()
	{
		return mediaExchangeXSDPath;
	}

	public void setMediaExchangeXSD(String mediaExchangeXSD)
	{
		this.mediaExchangeXSDPath = mediaExchangeXSD;
	}

}
