package com.mediasmiths.mq.handlers.button;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClientException;

public class ExportMarkersButton extends ButtonClickHandler
{

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
