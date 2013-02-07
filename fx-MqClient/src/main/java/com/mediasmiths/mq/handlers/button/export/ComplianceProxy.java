package com.mediasmiths.mq.handlers.button.export;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.mayam.MayamButtonType;

public class ComplianceProxy extends ExportProxyButton
{

	@Inject
	@Named("export.compliance.transcode.location")
	private String transcodeDestination;
	
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
	protected String getTranscodeDestination(AttributeMap materialAttributes)
	{
		return transcodeDestination;
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
