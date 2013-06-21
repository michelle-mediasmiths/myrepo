package com.mediasmiths.mq.handlers.button.export;

import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.mayam.MayamButtonType;
import org.apache.log4j.Logger;

public class ComplianceProxy extends ExportProxyButton
{
	private final static Logger log = Logger.getLogger(ComplianceProxy.class);
	
	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.COMPLIANCE_PROXY;
	}

	@Override
	public String getName()
	{
		return getJobType().getText();
	}

	@Override
	protected TranscodeJobType getJobType()
	{
		return TranscodeJobType.COMPLIANCE_PROXY;
	}

}
