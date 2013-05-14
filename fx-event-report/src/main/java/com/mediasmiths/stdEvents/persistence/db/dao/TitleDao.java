package com.mediasmiths.stdEvents.persistence.db.dao;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;

@ImplementedBy(com.mediasmiths.stdEvents.persistence.db.impl.TitleDaoImpl.class)
public interface TitleDao extends Dao<Title, String>
{

	void createOrUpdateTitle(EventEntity event);

}
