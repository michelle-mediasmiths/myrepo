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
	private final static Logger log = Logger.getLogger(CaptionProxy.class);	
	
	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.CAPTION_PROXY;
	}


	@Override
	public String getName()
	{
		return getJobType().getText();
	}

	@Override
	protected TranscodeJobType getJobType()
	{
		return TranscodeJobType.CAPTION_PROXY;
	}

}
