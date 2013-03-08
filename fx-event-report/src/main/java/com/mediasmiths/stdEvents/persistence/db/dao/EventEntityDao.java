package com.mediasmiths.stdEvents.persistence.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

public interface EventEntityDao extends Dao<EventEntity, Long>
{
	public List<EventEntity> findByNamespace(String namespace);

	public List<EventEntity> findByEventName(String eventName);

	public List<EventEntity> findUnique(String namespace, String eventName);
	
	public void printXML(List<EventEntity> events);
	
	public void saveFile (String eventString);

	public ArrayList<EventEntity> toBeanArray (List<EventEntity> events);
	
	public List<EventEntity> namespacePaginated(String namespace, int start, int max);
	
	public List<EventEntity> findUniquePaginated(String namespace, String eventName, int start, int max);

}
