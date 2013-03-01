package com.mediasmiths.mq.handlers.button.export;

import java.util.Date;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.mayam.MayamButtonType;

public class CaptionProxy extends ExportProxyButton
{

	@Inject
	@Named("export.caption.path.prefix")
	private String outputPath;
	@Inject
	@Named("export.caption.extention")
	private String outputExtension;
	
	
	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.CAPTION_PROXY;
	}

	@Override
	public String getName()
	{
		return "Caption Proxy";
	}

	@Override
	protected String getTranscodeDestination(AttributeMap materialAttributes)
	{
		return outputPath;
	}

	@Override
	protected int getPriority(Date firstTx)
	{
		if (firstTx == null)
		{
			return 2; // no tx date set, assume it is a long time from now
		}
		else
		{
			int priority = 2;

			long now = System.currentTimeMillis();
			long txTime = firstTx.getTime();
			long difference = txTime - now;

			if (difference > 0)
			{
				// tx date is in the future
				if (difference < THREE_DAYS)
				{
					priority = 6;
				}
				else if (difference < EIGHT_DAYS)
				{
					priority = 4;
				}
				else if (difference > SEVEN_DAYS)
				{
					priority = 2;
				}
			}
			else
			{
				// tx date is in the past
				if (Math.abs(difference) <= SEVEN_DAYS)
				{
					priority = 6; // go to the highest priority for this destination if the target date is no more than 7 days in the past
				}
				else
				{
					priority = 2;// else content goes to the lowest priority queue for that destination
				}
			}

			return priority;
		}
	}

	@Override
	protected TCOutputPurpose getPurpose()
	{
		return TCOutputPurpose.CAPTIONING;
	} 

	@Override
	protected String getOutputFileExtension()
	{
		return String.format(".%s",outputExtension);
	}

}
