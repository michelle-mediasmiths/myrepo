package com.mediasmiths.mayam.accessrights;

import com.google.inject.Inject;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Criteria;

public class MayamAccessRightsController extends HibernateDao<MayamAccessRights, Long> {

	private static final Logger log = Logger.getLogger(MayamAccessRightsController.class);
	
	@Inject
	private MayamChannelGroupsController channelGroupsController;
	
	public MayamAccessRightsController()
	{
		super(MayamAccessRights.class);
	}
	
	@Transactional
	 public void create(String qcStatus, String qaStatus, boolean qcParallel, String assetType, String groupName, boolean read, boolean write, boolean admin, boolean purgeProtected, boolean adultOnly) 
	 {
		 
		 MayamAccessRights rights = new MayamAccessRights();
		 rights.setQcStatus(qcStatus);
		 rights.setQaStatus(qaStatus.toString());
		 rights.setQcParallel(qcParallel);
		 rights.setContentType(assetType);
		 rights.setGroupName(groupName);
		 rights.setReadAccess(read);
		 rights.setWriteAccess(write);
		 rights.setAdminAccess(admin);
		 rights.setRestricted(purgeProtected);
		 rights.setAdultOnly(adultOnly);
		 
		 save(rights);
	 }
	
	@Transactional
	 public List<MayamAccessRights> retrieve(String qcStatus, String qaStatus, Boolean qcParallel, String assetType, ArrayList <String> channels, boolean restrictedAccess, Boolean adultOnly) 
	 {
		
		log.debug("Rereiving mayam access rights");

		log.debug("qcStatus is " + qcStatus);
		log.debug("qaStatus is " + qaStatus);

		if (qcParallel == null)
		{
			log.debug("qcParallel is null");
		}
		else
		{
			log.debug("qcParallel is " + qcParallel.toString());
		}
		log.debug("assetType is " + assetType);

		if (channels == null)
		{
			log.debug("channels is null");
		}
		else
		{
			log.debug("channels is " + StringUtils.join(channels.toArray()));
		}
		log.debug("restrictedAccess is " + restrictedAccess);

		if (adultOnly == null)
		{
			log.debug("adultOnly is null");
		}
		else
		{
			log.debug("adultOnly is " + adultOnly.toString());
		}
		
		  Criteria criteria = createCriteria();
		  if (qcStatus != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("qcStatus", qcStatus)).add(Restrictions.eq("qcStatus", "*")));
		  }
		  if (qaStatus != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("qaStatus", qaStatus)).add(Restrictions.eq("qaStatus", "*")));
		  }
		  if (qcParallel != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("qcParallel", qcParallel)).add(Restrictions.isNull("qcParallel")));
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("contentType", assetType)).add(Restrictions.eq("contentType", "*")));
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("restricted", restrictedAccess)).add(Restrictions.isNull("restricted")));
		  }
		  if (adultOnly != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("adultOnly", adultOnly)).add(Restrictions.isNull("adultOnly")));
		  }
		  else{
			  criteria.add(Restrictions.eq("adultOnly", Boolean.FALSE));
		  }
		  if (channels != null) {
			 
			  for (int i = 0; i < channels.size(); i++)
			  {
				  List<MayamChannelGroups> groups = channelGroupsController.retrieve(channels.get(i), null);
				  
				  if(groups != null){
				  log.trace(String.format("%d groups returned for channel %s",groups.size(), channels.get(i)));
				  }
				  
				  Disjunction restrictions = Restrictions.disjunction();
				  restrictions.add(Restrictions.eq("channelOwner", "*"));
				  if (groups != null) 
				  {
					  for (int j = 0; j < groups.size(); j++) 
					  {
						  restrictions.add(Restrictions.eq("channelOwner", groups.get(j).getChannelOwner()));
					  }
				  }
				  criteria.add(restrictions);
			  }

		  }

		  return new ArrayList<MayamAccessRights>(getList(criteria));
	 }  
}
