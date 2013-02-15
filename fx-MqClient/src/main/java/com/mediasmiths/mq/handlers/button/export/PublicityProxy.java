package com.mediasmiths.mq.handlers.button.export;

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
	protected int getPriority(AttributeMap materialAttributes)
	{
		return 1; 
	}

	@Override
	protected TCOutputPurpose getPurpose()
	{
		return TCOutputPurpose.DVD;
	}

}
