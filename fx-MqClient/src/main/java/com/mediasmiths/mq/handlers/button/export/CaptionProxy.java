package com.mediasmiths.mq.handlers.button.export;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.mayam.MayamButtonType;

public class CaptionProxy extends ExportProxyButton
{

	@Inject
	@Named("export.caption.path.prefix")
	private String outputPath;
	
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
	protected int getPriority(AttributeMap materialAttributes)
	{
		return 3; 
	}

	@Override
	protected TCOutputPurpose getPurpose()
	{
		return TCOutputPurpose.CAPTIONING;
	}

}
