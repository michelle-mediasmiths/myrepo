package com.mediasmiths.stdEvents.ui.rss;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AtomNotificationFeed
{
	public static final transient Logger logger = Logger.getLogger(AtomNotificationFeed.class);

	public Feed marshal(String id, String title, List<EventEntity> logs)
	{
		logger.info("Creating feed...");
		Feed feed = new Feed();
		logger.info("Setting title and id...");
		feed.setTitle(title);
		//URI uriId = URI.create(id);

		//feed.setId(uriId);

		logger.info("Adding log entries...");
		for (EventEntity log : logs)
		{
			feed.getEntries().add(atomRender(log));
		}

		feed.setUpdated(new Date());
		logger.info("Feed complete");

		return feed;
	}

	public Entry atomRender(final EventEntity event)
	{
		Entry atom = new Entry();

		logger.info("Creating entry...");
		//atom.setId(URI.create("event-id: " + event.id));
		atom.setTitle(Long.toString(event.id) + " " + new Date());
		Content content = new Content();
		content.setType(MediaType.TEXT_HTML_TYPE);
		content.setText("EventName: " + event.getEventName() + "Namespace: " + event.getNamespace() + "Payload: " + event.payload);
		atom.setContent(content);
		logger.info("Entry created");

		return atom;
	}

	public List<String> unmarshal(Feed feed) 
	{	
		String l = System.getProperty("line.seperator");
		List<String> result = new ArrayList<String>();

		List<Entry> entries = feed.getEntries();

		for (Entry entry : entries) {
			String current = entryUnmarshal(entry) + l;
			result.add(current);
		}
		return result;
	}

	public String entryUnmarshal (Entry entry) 
	{
		String title = entry.getTitle();
		Content content = entry.getContent();
		String type = content.getType().toString();
		String text = content.getText();

		String entryString = title + " " + type + " " + text;		

		return entryString;
	}
}
