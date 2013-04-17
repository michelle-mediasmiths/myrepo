package com.mediasmiths.mayam.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.generated.mediaexchange.AudioTrackType;

/**
 * Configuration of Audio Visual Properties transitioning from Mayam to WFE
 */
public class MayamAudioVisualModule extends AbstractModule
{

	@Override
	protected void configure()
	{

	}


	@Provides
	@Named("channel.audio.config")
	AudioTrackType[] provideAudioLayout(@Named("channel.audio.maxchannels") int channelCount,
	                                    @Named("channel.audio.left") int leftChannel,
	                                    @Named("channel.audio.right") int rightChannel,
	                                    @Named("channel.audio.dolby1") int dolby1Channel,
	                                    @Named("channel.audio.dolby2") int dolby2Channel)
	{

		AudioTrackType[] audioTracks = new AudioTrackType[channelCount];

		for (int i = 0; i < channelCount; i++)
		{
			audioTracks[i] = null;
		}

		audioTracks[leftChannel] = AudioTrackType.LEFT;
		audioTracks[rightChannel] = AudioTrackType.RIGHT;
		audioTracks[dolby1Channel] = AudioTrackType.DOLBY_E;
		audioTracks[dolby2Channel] = AudioTrackType.DOLBY_E;

		return audioTracks;
	}


}
