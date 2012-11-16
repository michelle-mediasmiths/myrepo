package com.mediasmiths.mayam.accessrights;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Criteria;

public class MayamChannelGroupsController extends HibernateDao<MayamChannelGroups, Long> {

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
		  Criteria criteria = createCriteria();
		  if (channel != null) {
			  criteria.add(Restrictions.eq("channelName", channel));
		  }
		  if (owner != null) {
			  criteria.add(Restrictions.eq("channelOwner", owner));
		  }

		  return new ArrayList<MayamChannelGroups>(getList(criteria));
	 }  
}