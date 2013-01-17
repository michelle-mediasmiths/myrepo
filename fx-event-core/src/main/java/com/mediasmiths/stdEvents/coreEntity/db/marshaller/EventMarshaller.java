package com.mediasmiths.stdEvents.coreEntity.db.marshaller;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

/*
 * Called when a new event is saved to the database to decide which type the new event is and save its payload in the appropriate table
 */
public interface EventMarshaller
{
	public void save (EventEntity event);
	
	public String getNamespace(EventEntity event);
	
	public String getInfo(EventEntity event);
}
