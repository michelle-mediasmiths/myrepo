package com.mediasmiths.mq.handlers.button.export;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.mayam.MayamButtonType;
import org.apache.log4j.Logger;

public class CaptionProxy extends ExportProxyButton
{

	@Inject
	@Named("export.caption.path.prefix")
	private String outputPath;
	@Inject
	@Named("export.caption.extention")
	private String outputExtension;
	
	private final static Logger log = Logger.getLogger(CaptionProxy.class);	
	
	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.CAPTION_PROXY;
	}

	public static final String buttonName = "Caption Proxy";

	@Override
	public String getName()
	{
		return buttonName;
	}

	@Override
	protected String getTranscodeDestination(AttributeMap materialAttributes)
	{
		return outputPath;
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

	@Override
	protected TranscodeJobType getJobType()
	{
		return TranscodeJobType.CAPTION_PROXY;
	}

}
