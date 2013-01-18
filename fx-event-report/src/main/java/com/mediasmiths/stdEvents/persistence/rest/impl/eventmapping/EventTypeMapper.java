package com.mediasmiths.stdEvents.persistence.rest.impl.eventmapping;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;

import java.util.Map;

/**
 *
 * Manage the current collection of mapping from events to storage types.
 *
 * Author: Harmer
 */
public class EventTypeMapper
{

	@Inject
	protected Injector injector;

	@Inject
	@Named("event.reporter.eventtypemap")
	protected Map<String, Class<? extends EventMarshaller>> storeFormat;

	/**
	 *
	 * @param namespace
	 * @return the class
	 */
	public EventMarshaller get(String namespace)
	{
		Class<? extends EventMarshaller> marshaller = storeFormat.get(namespace);

		if (marshaller == null)
			return null;

		return injector.getInstance(marshaller);

	}

}
