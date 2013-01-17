package com.mediasmiths.stdEvents.events.db.marshaller;

import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;

/*
 * When given a placeholder message, it translates into the appropriate PlaceholderMessage type
 */
public interface PlaceholderMarshaller <T>
{
	public T get (PlaceholderMessage message);
}
