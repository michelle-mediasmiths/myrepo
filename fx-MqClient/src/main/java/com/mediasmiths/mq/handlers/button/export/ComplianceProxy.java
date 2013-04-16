package com.mediasmiths.mq.handlers.button.export;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.mayam.MayamButtonType;
import org.apache.log4j.Logger;

public class ComplianceProxy extends ExportProxyButton
{

	@Inject
	@Named("export.compliance.foldername")
	private String outputFolderName;
	
	@Inject
	@Named("export.compliance.path.prefix")
	private String outputPrefix;
	
	@Inject
	@Named("export.compliance.extention")
	private String outputExtension;
	
	private final static Logger log = Logger.getLogger(ComplianceProxy.class);
	
	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.COMPLIANCE_PROXY;
	}

	public static final String buttonName = "Compliance Proxy";

	@Override
	public String getName()
	{
		return buttonName;
	}

	@Override
	protected String getTranscodeDestination(AttributeMap materialAttributes)
	{
		String exportLocation = getExportLocationForFirstChannel(materialAttributes);
		return String.format("%s/%s/%s",outputPrefix,exportLocation,outputFolderName);
	}

	@Override
	protected TCOutputPurpose getPurpose()
	{
		return TCOutputPurpose.MPG4;
	}

	@Override
	protected String getOutputFileExtension()
	{
		return String.format(".%s",outputExtension);
	}

	@Override
	protected TranscodeJobType getJobType()
	{
		return TranscodeJobType.COMPLIANCE_PROXY;
	}

}
