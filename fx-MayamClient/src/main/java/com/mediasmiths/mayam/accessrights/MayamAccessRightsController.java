package com.mediasmiths.mayam.accessrights;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory; 

public class MayamAccessRightsController {

	private final static Logger log = Logger.getLogger(MayamMaterialController.class);
	
	private Configuration config;
	private SessionFactory factory;
	private Session session;
	private ServiceRegistry serviceRegistry;
	
	public MayamAccessRightsController()
	{
		 config = new Configuration();
		 config.addAnnotatedClass(MayamAccessRights.class);
		 serviceRegistry = new ServiceRegistryBuilder().configure().buildServiceRegistry();
		 MetadataSources metadataSources = new MetadataSources(serviceRegistry);
		 metadataSources.addAnnotatedClass(MayamAccessRights.class);
		 factory = metadataSources.buildMetadata().buildSessionFactory();
		 session = factory.openSession();
	}
	
	 public void create(MayamTaskListType taskType, TaskState taskState, MayamAssetType assetType, String groupName, boolean read, boolean write, boolean admin) 
	 {
		 session.beginTransaction();
		 MayamAccessRights rights = new MayamAccessRights();
		 rights.setTaskType(taskType.toString());
		 rights.setAssetType(assetType.toString());
		 rights.setTaskState(taskState.toString());
		 rights.setGroupName(groupName);
		 rights.setReadAccess(read);
		 rights.setWriteAccess(write);
		 rights.setAdminAccess(admin);
		 session.save(rights);
		 session.getTransaction().commit();
	 }
	 
	 public ArrayList<MayamAccessRights> retrieve(MayamTaskListType taskType, TaskState taskState, MayamAssetType assetType) 
	 {   
		  session.beginTransaction();  
		  Criteria criteria = session.createCriteria(MayamAccessRights.class);
		  if (taskType != null) {
			  criteria.add(Restrictions.eq("taskType", taskType.toString()));
		  }
		  if (taskState != null) {
			  criteria.add(Restrictions.eq("taskState", taskState.toString()));
		  }
		  if (assetType != null) {
			  criteria.add(Restrictions.eq("assetType", assetType.toString())); 
		  }

		  List allRights = criteria.list();
		  ArrayList <MayamAccessRights> rightsList = new ArrayList <MayamAccessRights> ();
		  
		  for (int i = 0; i < allRights.size(); i++) {  
			  rightsList.add((MayamAccessRights) allRights.get(i));  
		  }
		  
		  return rightsList;
	 }  
	   
}
