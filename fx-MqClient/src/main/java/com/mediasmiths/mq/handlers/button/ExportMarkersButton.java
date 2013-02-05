package com.mediasmiths.mq.handlers.button;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClientException;

public class ExportMarkersButton extends ButtonClickHandler
{

	private final static Logger log = Logger.getLogger(ExportMarkersButton.class);
	
	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{
		try
		{
			materialController.exportMarkers(messageAttributes);
		}
		catch (MayamClientException e)
		{
			log.error("error exporting markers",e);
		}
	}

	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.EXPORT_MARKERS;
	}

	@Override
	public String getName()
	{
		return "Export Markers Button";
	}

}
