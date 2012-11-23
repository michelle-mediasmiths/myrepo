package com.mediasmiths.mayam.accessrights;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import java.util.ArrayList;
import java.util.List;
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
			  criteria.add(Restrictions.eq("qcStatus", qcStatus));
		  }
		  if (qaStatus != null) {
			  criteria.add(Restrictions.eq("qaStatus", qaStatus));
		  }
		  if (qcParallel != null) {
			  criteria.add(Restrictions.eq("qcParallel", qcParallel));
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.eq("contentType", assetType)); 
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.eq("restricted", purgeProtected)); 
		  }
		  if (adultOnly != null) {
			  criteria.add(Restrictions.eq("adultOnly", adultOnly)); 
		  }
		  if (channels != null) {
			  MayamChannelGroupsController channelGroupsController = new MayamChannelGroupsController();
			  for (int i = 0; i < channels.size(); i++)
			  {
				  List<MayamChannelGroups> groups = channelGroupsController.retrieve(channels.get(i), null);
				  for (int j = 0; j < groups.size(); j++) 
				  {
					  criteria.add(Restrictions.eq("channelOwner", groups.get(j).getChannelOwner()));
				  }
			  }

		  }

		  return new ArrayList<MayamAccessRights>(getList(criteria));
	 }  
}
