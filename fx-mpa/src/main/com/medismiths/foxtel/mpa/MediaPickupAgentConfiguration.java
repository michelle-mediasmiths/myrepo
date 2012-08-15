package com.medismiths.foxtel.mpa;

import java.net.URI;

public class MediaPickupAgentConfiguration {

	private String mediaExchangeXSDPath = "MediaExchange_V1.2.xsd";

	public String getMediaExchangeXSD() {
		return mediaExchangeXSDPath;
	}

	public void setMediaExchangeXSD(String mediaExchangeXSD) {
		this.mediaExchangeXSDPath = mediaExchangeXSD;
	} 
	
}
