package com.medismiths.foxtel.mpa.config;

public class MediaPickupAgentConfiguration
{

	//file path to xsd describing schema
	private String mediaExchangeXSDPath = "MediaExchange_V1.2.xsd";
	//folder to watch for incoming media
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
