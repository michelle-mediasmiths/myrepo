package com.mediasmiths.mayam.accessrights;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetAccess;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.AssetAccess.EntityType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.util.Triplet;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import java.util.ArrayList;
import java.util.HashMap;
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
	
	public static Attribute[] attributesAffectingAccessRights = new Attribute[] { Attribute.CONT_RESTRICTED_ACCESS,
		Attribute.CHANNELS, Attribute.CONT_MAT_TYPE, Attribute.CONT_CATEGORY, Attribute.CONT_RESTRICTED_MATERIAL,
		Attribute.QC_PREVIEW_RESULT, Attribute.QC_PARALLEL_ALLOWED, Attribute.QC_STATUS};

	
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
			 
			  Disjunction restrictions = Restrictions.disjunction();
			  restrictions.add(Restrictions.eq("channelOwner", "*"));
			  for (int i = 0; i < channels.size(); i++)
			  {
				  List<MayamChannelGroups> groups = channelGroupsController.retrieve(channels.get(i), null);
				  
				  if(groups != null){
				  log.trace(String.format("%d groups returned for channel %s",groups.size(), channels.get(i)));
				  }
				  
				  if (groups != null) 
				  {
					  for (int j = 0; j < groups.size(); j++) 
					  {
						  restrictions.add(Restrictions.eq("channelOwner", groups.get(j).getChannelOwner()));
					  }
				  }
			  }
			  criteria.add(restrictions);
		  }

		  return new ArrayList<MayamAccessRights>(getList(criteria));
	 }  
	
	public List<MayamAccessRights> retrieveAllRightsForAsset(AttributeMap attributeMap)
	{
		AssetType assetType = attributeMap.getAttribute(Attribute.ASSET_TYPE);
		String houseId = attributeMap.getAttribute(Attribute.HOUSE_ID);

		boolean retrictedAccess = false;

		if (attributeMap.getAttribute(Attribute.CONT_RESTRICTED_ACCESS) != null)
		{
			retrictedAccess = ((Boolean) attributeMap.getAttribute(Attribute.CONT_RESTRICTED_ACCESS)).booleanValue();
		}

		StringList channels = attributeMap.getAttribute(Attribute.CHANNELS);
		ArrayList<String> channelList = new ArrayList<String>();
		if (channels != null)
		{
			for (int i = 0; i < channels.size(); i++)
			{
				channelList.add(channels.get(i));
			}
		}

		String contentFormat = attributeMap.getAttributeAsString(Attribute.CONT_MAT_TYPE);
		String contentCategory = attributeMap.getAttributeAsString(Attribute.CONT_CATEGORY);

		log.warn(String.format(
				"content format is %s , category is %s for asset with id %s",
				contentFormat,
				contentCategory,
				houseId));

		String contentType = null;
		if (assetType != null && assetType.equals(MayamAssetType.TITLE.getAssetType()))
		{
			contentType = "Title";
		}
		else if (contentFormat != null)
		{
			if (contentFormat.toUpperCase().equals(MayamContentTypes.PROGRAMME))
			{
				contentType = "Programme";
			}
			else if (contentFormat.toUpperCase().equals(MayamContentTypes.EPK))
			{
				contentType = "EPK";
			}
			else if (contentFormat.toUpperCase().equals(MayamContentTypes.EDIT_CLIPS))
			{
				contentType = "Edit Clip";
			}
			else if (contentFormat.toUpperCase().equals(MayamContentTypes.UNMATCHED))
			{
				contentType = "Unmatched";
			}
		}
		else if (contentCategory != null)
		{
			if (contentCategory.toUpperCase().equals("PUBLICITY"))
			{
				contentType = "Publicity";
			}
		}

		Boolean adultOnly = attributeMap.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);
		Boolean qcParallel = attributeMap.getAttribute(Attribute.QC_PARALLEL_ALLOWED);
		QcStatus qcStatus = attributeMap.getAttribute(Attribute.QC_STATUS);

		String qaStatus = attributeMap.getAttribute(Attribute.QC_PREVIEW_RESULT);
	
		String qaStatusString = "";
		String qcStatusString = "";
				
		if (qaStatus != null)
		{
			qaStatusString = qaStatus.toString();
			qaStatusString = "Undefined";
			if (qaStatus.equals(MayamPreviewResults.PREVIEW_FAIL)) 
			{
				qaStatusString = "Fail";
			}
			else if (qaStatus.equals(MayamPreviewResults.PREVIEW_PASSED_BUT_REORDER)|| qaStatus.equals(MayamPreviewResults.PREVIEW_PASSED))
			{
				qaStatusString = "Pass";
			}
		}
		
		if (qcStatus != null)
		{
			qcStatusString = "Undefined";
			if (qcStatus == QcStatus.FAIL) 
			{
				qcStatusString = "Fail";
			}
			else if (qcStatus == QcStatus.PASS || qcStatus == QcStatus.PASS_MANUAL)
			{
				qcStatusString = "Pass";
			}
		}
		
		return retrieve(
				qcStatusString,
				qaStatusString,
				qcParallel,
				contentType,
				channelList,
				retrictedAccess,
				adultOnly);
	}
	
	public AttributeMap updateAccessRights(AttributeMap attributeMap)
	{
		List<MayamAccessRights> allRights = retrieveAllRightsForAsset(attributeMap);
		String houseId = attributeMap.getAttribute(Attribute.HOUSE_ID);
		
		if(allRights == null){
			log.warn("allrights is null");
		}
		else{
			log.debug("allrights size: "+allRights.size());
			
			StringBuilder sb = new StringBuilder();
			for (MayamAccessRights mayamAccessRights : allRights)
			{
				sb.append(mayamAccessRights.toString());
				sb.append("\n");
			}
			log.debug("Allrights "+sb.toString());
		}

		if (allRights != null && allRights.size() > 0)
		{
			AssetAccess accessRights = attributeMap.getAttribute(Attribute.ASSET_ACCESS);
			
			if(accessRights==null){
				log.warn("Access rights in attribute map was null");
				accessRights=new AssetAccess();
			}
			
			accessRights.getMedia().clear();
			accessRights.getStandard().clear();
			
			//set owner to _none
			AssetAccess.ControlList.Entry noneUser = new AssetAccess.ControlList.Entry();
			noneUser.setEntityType(EntityType.USER);
			noneUser.setEntity("_none");
			accessRights.getStandard().add(noneUser);
			
			HashMap<String, Triplet<Boolean, Boolean, Boolean>> groupMap = new HashMap<String, Triplet<Boolean, Boolean, Boolean>>();
			
			for (int i = 0; i < allRights.size(); i++)
			{
				if (groupMap.containsKey(allRights.get(i).getGroupName()))
				{
					Triplet <Boolean, Boolean, Boolean> rights = groupMap.get(allRights.get(i).getGroupName());
					rights.a = rights.a || allRights.get(i).getReadAccess();
					rights.b = rights.b || allRights.get(i).getWriteAccess();
					rights.c = rights.c || allRights.get(i).getAdminAccess();
					groupMap.put(allRights.get(i).getGroupName(), rights);
				}
				else {
					Triplet <Boolean, Boolean, Boolean> rights = new Triplet<Boolean, Boolean, Boolean>(allRights.get(i).getReadAccess(), allRights.get(i).getWriteAccess(), allRights.get(i).getAdminAccess());
					groupMap.put(allRights.get(i).getGroupName(), rights);
				}
			}
			
			String[] allKeys = groupMap.keySet().toArray(new String[0]);
			
			for (int i = 0; i < allKeys.length; i++)
			{
				AssetAccess.ControlList.Entry entry = new AssetAccess.ControlList.Entry();
				AssetAccess.ControlList.Entry mediaEntry = new AssetAccess.ControlList.Entry();
					
				entry.setEntityType(EntityType.GROUP);
				entry.setEntity(allKeys[i]);
				
				mediaEntry.setEntityType(EntityType.GROUP);
				mediaEntry.setEntity(allKeys[i]);
					
				Triplet <Boolean, Boolean, Boolean> rights = groupMap.get(allKeys[i]);
					
				entry.setRead(rights.a);
				entry.setWrite(rights.b);
					
				mediaEntry.setRead(rights.c);
				mediaEntry.setWrite(rights.c);
					
				accessRights.getMedia().add(mediaEntry);
				accessRights.getStandard().add(entry);
			}
			
			log.info("Access Rights for " + houseId + " updated to : " + accessRights.toString());
			attributeMap.setAttribute(Attribute.ASSET_ACCESS, accessRights);
		}
		else {
			log.info("No suitable Access Rights found for task : " + houseId);
		}
		return attributeMap;
	}
}
