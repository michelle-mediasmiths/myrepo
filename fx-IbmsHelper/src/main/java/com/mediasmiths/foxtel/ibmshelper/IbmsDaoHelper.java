package com.mediasmiths.foxtel.ibmshelper;

import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.CreateUpdateTitle;

public class IbmsDaoHelper
{
	public CreateUpdateTitle createUpdateTitle;

	public IbmsDaoHelper()
	{
		this.createUpdateTitle = new CreateUpdateTitle();
	}

	public String createUpdateTitle()
	{
		// make calls within createupdateclass
		// writes titleplaceholder and materialplaceholder xml
		// Return xml? or save to table and return status

		return "temp";
	}

	public void updateRequestedStatus(String verionID, String requestID)
	{
	}

}
