package com.mediasmiths.stdEvents.ui.rss;

import java.util.List;

import org.jboss.resteasy.plugins.providers.atom.Feed;

import com.google.inject.Inject;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;

public class NotificationServiceImpl implements NotificationServiceAPI
{
	@Inject
	public EventEntityDao eventDao;
	@Inject
	public AtomNotificationFeed atomFeed;

	@Override
	@Transactional(readOnly = true)
	public Feed getFeed()
	{
		List<EventEntity> notifications = eventDao.getAll();
		return atomFeed.marshal("all events", "EventEntity", notifications);
	}

	@Override
	@Transactional
	public List<String> asString(Feed feed)
	{
		List<String> feedString = atomFeed.unmarshal(feed);
		return feedString;
	}

	@Override
	@Transactional
	public List<String> getFeedAsString()
	{
		List<EventEntity> notifications = eventDao.getAll();
		Feed feed = atomFeed.marshal("all events", "EventEntity", notifications);
		List<String> feedString = atomFeed.unmarshal(feed);
		return feedString;
	}
}