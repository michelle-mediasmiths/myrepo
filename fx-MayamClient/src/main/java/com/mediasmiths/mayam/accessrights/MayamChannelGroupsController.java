package com.mediasmiths.mayam.accessrights;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Criteria;

public class MayamChannelGroupsController extends HibernateDao<MayamChannelGroups, Long> {

	private final static Logger log = Logger.getLogger(MayamChannelGroupsController.class);
	
	public MayamChannelGroupsController()
	{
		super(MayamChannelGroups.class);
	}
	
	@Transactional
	 public void create(String channel, String owner) 
	 {
		 MayamChannelGroups group = new MayamChannelGroups();
		 group.setChannelName(channel);
		 group.setChannelOwner(owner);
		 save(group);
	 }
	
	@Transactional
	public List<MayamChannelGroups> retrieve(String channel, String owner)
	{
		log.trace(String.format("MayamChannelGroups.retreive channel {%s} owner {%s}", channel, owner));

		Criteria criteria = createCriteria();
		if (channel != null)
		{
			criteria.add(Restrictions.eq("channelName", channel));
		}
		if (owner != null)
		{
			criteria.add(Restrictions.eq("channelOwner", owner));
		}

		List<MayamChannelGroups> list = getList(criteria);

		if (list == null)
		{
			log.warn("null list returned on channel group search");
		}
		return new ArrayList<MayamChannelGroups>(list);
	}
}