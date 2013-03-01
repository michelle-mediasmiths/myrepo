package com.mediasmiths.mq.handlers.button.export;

import java.util.Date;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.mayam.MayamButtonType;

public class PublicityProxy extends ExportProxyButton
{

	@Inject
	@Named("export.publicity.foldername")
	private String outputFolderName;
	
	@Inject
	@Named("export.publicity.path.prefix")
	private String outputPrefix;
	
	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.PUBLICITY_PROXY;
	}

	@Override
	public String getName() 
	{
		return "Publicity Proxy";
	}

	@Override
	protected String getTranscodeDestination(AttributeMap materialAttributes)
	{
		String exportLocation = getExportLocationForFirstChannel(materialAttributes);
		return String.format("%s/%s/%s",outputPrefix,exportLocation,outputFolderName);
	}


	@Override
	protected int getPriority(Date firstTx)
	{
		if (firstTx == null)
		{
			return 1; // no tx date set, assume it is a long time from now
		}
		else
		{
			int priority = 1;

			long now = System.currentTimeMillis();
			long txTime = firstTx.getTime();
			long difference = txTime - now;

			if (difference > 0)
			{
				// tx date is in the future
				priority = 1; 
			}
			else
			{
				// tx date is in the past
				if (Math.abs(difference) <= SEVEN_DAYS)
				{
					priority = 5; // go to the highest priority for this destination if the target date is no more than 7 days in the past
				}
				else
				{
					priority = 1;// else content goes to the lowest priority queue for that destination
				}
			}

			return priority;
		}
	}


	@Override
	protected TCOutputPurpose getPurpose()
	{
		return TCOutputPurpose.DVD;
	}
	@Override
	protected String getOutputFileExtension()
	{
		return ""; //output is dvd foder struture, the user supplied name is sufficient
	}
	
}
