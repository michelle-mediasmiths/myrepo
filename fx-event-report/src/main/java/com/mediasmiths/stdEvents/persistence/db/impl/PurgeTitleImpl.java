package com.mediasmiths.stdEvents.persistence.db.impl;

import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;
import com.mediasmiths.stdEvents.events.db.entity.placeholder.PurgeTitle;
import com.mediasmiths.stdEvents.events.db.marshaller.PlaceholderMarshaller;

public class PurgeTitleImpl implements PlaceholderMarshaller<PurgeTitle>
{

	@Override
	public PurgeTitle get(PlaceholderMessage message)
	{
		PurgeTitle purge = new PurgeTitle();
		String str = message.getActions();
		purge.setTitleID(str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9))));
		return purge;
	}

}
