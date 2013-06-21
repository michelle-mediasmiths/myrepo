package com.mediasmiths.mq.handlers.button.export;

import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.mayam.MayamButtonType;
import org.apache.log4j.Logger;

public class PublicityProxy extends ExportProxyButton
{	
	private final static Logger log = Logger.getLogger(PublicityProxy.class);
	
	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.PUBLICITY_PROXY;
	}

	@Override
	public String getName()
	{
		return getJobType().getText();
	}

	@Override
	protected TranscodeJobType getJobType()
	{
		return TranscodeJobType.PUBLICITY_PROXY;
	}
	
}
