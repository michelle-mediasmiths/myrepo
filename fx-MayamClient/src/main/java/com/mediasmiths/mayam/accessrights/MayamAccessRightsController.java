package com.mediasmiths.mayam.accessrights;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Criteria;

public class MayamAccessRightsController extends HibernateDao<MayamAccessRights, Long> {

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
	 public List<MayamAccessRights> retrieve(String qcStatus, String qaStatus, Boolean qcParallel, String assetType, ArrayList <String> channels, boolean purgeProtected, Boolean adultOnly) 
	 {
		  Criteria criteria = createCriteria();
		  if (qcStatus != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("qcStatus", qcStatus)).add(Restrictions.eq("qcStatus", "*")));
		  }
		  if (qaStatus != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("qaStatus", qaStatus)).add(Restrictions.eq("qaStatus", "*")));
		  }
		  if (qcParallel != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("qcParallel", qcParallel)).add(Restrictions.eq("qcParallel", null)));
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("contentType", assetType)).add(Restrictions.eq("contentType", "*")));
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("restricted", purgeProtected)).add(Restrictions.eq("restricted", null)));
		  }
		  if (adultOnly != null) {
			  criteria.add(Restrictions.disjunction().add(Restrictions.eq("adultOnly", adultOnly)).add(Restrictions.eq("adultOnly", null)));
		  }
		  if (channels != null) {
			  MayamChannelGroupsController channelGroupsController = new MayamChannelGroupsController();
			  for (int i = 0; i < channels.size(); i++)
			  {
				  List<MayamChannelGroups> groups = channelGroupsController.retrieve(channels.get(i), null);
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
