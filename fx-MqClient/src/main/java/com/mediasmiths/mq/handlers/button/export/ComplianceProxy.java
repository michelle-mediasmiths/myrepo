package com.mediasmiths.mq.handlers.button.export;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.mayam.MayamButtonType;

public class ComplianceProxy extends ExportProxyButton
{

	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.COMPLIANCE_PROXY;
	}

	@Override
	public String getName()
	{
		return "Compliance Proxy";
	}

	@Override
	protected String getOutputFolder(AttributeMap materialAttributes)
	{
		//TODO: of course this needs to be configurable and the output should include the channel groups name (see folder locations diagram)
		return "/storage/corp/exports/compliance";
	}

	@Override
	protected int getPriority(AttributeMap materialAttributes)
	{
		return 1; 
	}

	@Override
	protected TCOutputPurpose getPurpose()
	{
		return TCOutputPurpose.CAPTIONING;
	}

}
