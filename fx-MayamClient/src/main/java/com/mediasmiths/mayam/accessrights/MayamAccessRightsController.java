package com.mediasmiths.mayam.accessrights;

import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
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
	 public void create(MayamTaskListType taskType, TaskState taskState, MayamAssetType assetType, String groupName, boolean read, boolean write, boolean admin, boolean purgeProtected) 
	 {
		 
		 MayamAccessRights rights = new MayamAccessRights();
		 rights.setTaskType(taskType.toString());
		 rights.setCategory(assetType.toString());
		 rights.setTaskState(taskState.toString());
		 rights.setGroupName(groupName);
		 rights.setReadAccess(read);
		 rights.setWriteAccess(write);
		 rights.setAdminAccess(admin);
		 rights.setRestricted(purgeProtected);
		 
		 save(rights);
	 }
	
	@Transactional
	 public List<MayamAccessRights> retrieve(MayamTaskListType taskType, TaskState taskState, MayamAssetType assetType, ArrayList <String> channels, boolean purgeProtected) 
	 {
		  Criteria criteria = createCriteria();
		  if (taskType != null) {
			  criteria.add(Restrictions.eq("taskType", taskType.toString()));
		  }
		  if (taskState != null) {
			  criteria.add(Restrictions.eq("taskState", taskState.toString()));
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.eq("category", assetType.toString())); 
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.eq("restricted", purgeProtected)); 
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
