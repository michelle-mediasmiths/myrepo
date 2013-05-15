package com.mediasmiths.foxtel.transcode;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;

public class TranscodeRulesImpl implements TranscodeRules
{
	@Inject(optional = true)
	@Named("transcode.output.intent.ires.sd.ores.sd.inputaudio.ge.eight.tracks.output.audio")
	String sdInputsdOutputSurround = TCAudioType.DOLBY_E.name();
	@Inject(optional = true)
	@Named("transcode.output.intent.ires.sd.ores.sd.inputaudio.lt.eight.tracks.output.audio")
	String sdInputsdOutputNotSurround = TCAudioType.STEREO.name();
	@Inject(optional = true)
	@Named("transcode.output.intent.ires.sd.ores.hd.inputaudio.ge.eight.tracks.output.audio")
	String sdInputhdOutputSurround = TCAudioType.DOLBY_E.name();
	@Inject(optional = true)
	@Named("transcode.output.intent.ires.sd.ores.hd.inputaudio.lt.eight.tracks.output.audio")
	String sdInputhdOutputNotSurround = TCAudioType.STEREO.name();
	@Inject(optional = true)
	@Named("transcode.output.intent.ires.hd.ores.hd.inputaudio.ge.eight.tracks.output.audio")
	String hdInputhdOutputSurround = TCAudioType.DOLBY_E.name();
	@Inject(optional = true)
	@Named("transcode.output.intent.ires.hd.ores.hd.inputaudio.lt.eight.tracks.output.audio")
	String hdInputhdOutputNotSurround = TCAudioType.STEREO.name();
	@Inject(optional = true)
	@Named("transcode.output.intent.ires.hd.ores.sd.inputaudio.ge.eight.tracks.output.audio")
	String hdInputsdOutputSurround = TCAudioType.DOLBY_E.name();
	@Inject(optional = true)
	@Named("transcode.output.intent.ires.hd.ores.sd.inputaudio.lt.eight.tracks.output.audio")
	String hdInputsdOutputNotSurround = TCAudioType.STEREO.name();

	@Override
	public TCAudioType audioTypeForTranscode(boolean inputIsSD, boolean inputIsSurround, boolean outputIsSD)
	{

		if (inputIsSD)
		{
			// sd input
			if (inputIsSurround)
			{
				if (outputIsSD)
				{
					// sd input sd output input has surround sound
					return audioTypeFor(sdInputsdOutputSurround);
				}
				else
				{
					// sd input hd output input has surround sound
					return audioTypeFor(sdInputhdOutputSurround);
				}
			}
			else
			{
				if (outputIsSD)
				{// sd input sd output input hasn't surround sound
					return audioTypeFor(sdInputsdOutputNotSurround);
				}
				else
				{
					// sd input hd output input hasn't surround sound
					return audioTypeFor(sdInputhdOutputNotSurround);
				}
			}
		}
		else
		{
			// hd input
			if (inputIsSurround)
			{
				if (outputIsSD)
				{
					// hd input sd output input has surround sound
					return audioTypeFor(hdInputsdOutputSurround);
				}
				else
				{
					// hd input hd output input has surround sound
					return audioTypeFor(hdInputhdOutputSurround);
				}
			}
			else
			{
				if (outputIsSD)
				{
					// hd input sd output input hasn't surround sound
					return audioTypeFor(hdInputsdOutputNotSurround);
				}
				else
				{
					// hd input hd output input hasn't surround sound
					return audioTypeFor(hdInputhdOutputNotSurround);
				}
			}
		}
	}

	private TCAudioType audioTypeFor(String s)
	{
		return TCAudioType.valueOf(s);
	}

}
