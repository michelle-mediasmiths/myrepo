package com.mediasmiths.mayam.util;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.generated.mediaexchange.AudioTrackType;

/**
 * Used to hold the properties in mapping from Ardome values to output XML forms.
 */
public class ChannelPropertiesConfiguration
{



	@Inject
    @Named("channel.audio.config")
	public AudioTrackType[] audioTrackLayout;

	public ChannelPropertiesConfiguration()
	{
	}

}
