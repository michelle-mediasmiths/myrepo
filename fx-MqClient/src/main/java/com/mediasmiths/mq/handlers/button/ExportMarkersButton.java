package com.mediasmiths.mq.handlers.button;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamButtonType;

public class ExportMarkersButton extends ButtonClickHandler
{

	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{
		materialController.exportMarkers(messageAttributes);
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
