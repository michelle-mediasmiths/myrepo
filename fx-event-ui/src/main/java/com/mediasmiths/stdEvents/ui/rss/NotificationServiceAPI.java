package com.mediasmiths.stdEvents.ui.rss;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.jboss.resteasy.plugins.providers.atom.Feed;

@Path("/notification")
public interface NotificationServiceAPI
{
	@GET
	@Path("/feed/atom.xml")
	@Produces("application/atom+xml")
	public Feed getFeed();

	@GET
	@Path("feed/as_string")
	@Produces("text/plain")
	public List<String> asString(Feed feed);

	@GET
	@Path("feed/get_feed_string")
	@Produces("text/plain")
	public List<String> getFeedAsString();

}
