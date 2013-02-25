package com.mediasmiths.foxtel.ip.mayam.pdp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.MarkerList;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.util.RevisionUtil;

import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Map;

/**
 * Mayam sends JSON that looks like this. A collection of mapped variables.
 *
 * {
  "aggregator" : "SIT",
  "asset_access" : {
    "standard" : [ {
      "entityType" : "GROUP",
      "entity" : "GS-MAM_FULL_BOPS",
      "read" : true,
      "write" : true,
      "admin" : false
    } ],
    "media" : [ {
      "entityType" : "GROUP",
      "entity" : "GS-MAM_FULL_BOPS",
      "read" : true,
      "write" : true,
      "admin" : false
    } ]
  }
 *
 */
public class MayamPDPImpl implements MayamPDP
{

	private final TasksClient client;
	private final MayamTaskController taskController;

	@Inject
	public MayamPDPImpl(@Named(MayamClientModule.SETUP_TASKS_CLIENT)TasksClient client, MayamTaskController taskController) 
	{
		this.client=client;
		this.taskController=taskController;
	}

	@Override
	public  Map<String, Object> segmentMismatch(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.REQ_NUMBER.toString(), Attribute.HOUSE_ID.toString());

	    Map<String, Object> returnMap = new HashMap<>();
	    returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.OK);
	    
	    //Segmentation check
	    String requestedNumber = attributeMap.get(Attribute.REQ_NUMBER.toString()).toString();
	    int numberOfSegmentsRequested = Integer.parseInt(requestedNumber);
	    String presentationID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();
	    
	    SegmentList segmentList = null;
	    try {
	    	segmentList = client.segmentApi().getSegmentListBySiteId(presentationID);
	    }
	    catch (RemoteException e) {
	    	segmentList = null;
	    }
	    
	    if (segmentList != null && segmentList.getEntries() != null)
	    {
		    int segmentsSize = segmentList.getEntries().size();
		    if (numberOfSegmentsRequested != segmentsSize) 
		    {
		    	returnMap.clear();
		    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
		    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Presentation ID : " + presentationID + ", user has requested " + numberOfSegmentsRequested + " segements, while placeholder contained " + segmentsSize);
		    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "The number of segments submitted does not match that requested by the channel. Are you sure you wish to proceed?");
		    }
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Unable to retrieve Segment List for Presentation ID : " + presentationID);
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "A technical fault has occurred while retrieving segemnt list");
	    }
	    
		return returnMap;
	}
	
	@Override
	public  Map<String, Object> segmentClassificationCheck(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, null, null);

	    Map<String, Object> returnMap = new HashMap<String, Object>();
	    returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.OK);
	    
	    String classification = attributeMap.get(Attribute.CONT_CLASSIFICATION.toString()).toString();
	    if (classification == null || classification.equals(""))
	    {
	    	String presentationID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();
	    	
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "The Tx Package has not been classified: " + presentationID);
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "The TX Package has not been classified. Please contact the channel owner and ensure that this is provided <OK>");
	    }
	    
		return returnMap;
	}
	
	@Override
	public  Map<String, Object> uningestProtected(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.PURGE_PROTECTED.toString());

	    Map<String, Object> returnMap = new HashMap<String, Object>();
	    returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.OK);
	    
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();
	    String assetType = attributeMap.get(Attribute.ASSET_TYPE.toString()).toString();
	    String protectedString = attributeMap.get(Attribute.PURGE_PROTECTED.toString()).toString();
	    Boolean purgeProtected = Boolean.parseBoolean(protectedString);
	    
	    if (purgeProtected == null || purgeProtected)
	    {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Protected asset cannot be uningested: " + houseID);
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "WARNING: " + assetType + " " + houseID + "is protected and cannot be uningested <OK>");
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.CONFIRM);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Warning user that : " + houseID + " and associated metadata will be deleted");
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "WARNING: You are about to delete this media and all associated metadata, are you sure you want to proceed? This cannot be undone. <OK, Cancel>");
	    }
	    
		return returnMap;
	}

	@Override
	public  Map<String, Object> delete(final Map<String, Object> attributeMap)
	{
		validateAttributeMap(attributeMap, Attribute.PURGE_PROTECTED.toString(), Attribute.HOUSE_ID.toString(), Attribute.ASSET_TYPE.toString());

		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.OK);


		return returnMap;
	}

	@Override
	public  Map<String, Object> deleteProtected(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.PURGE_PROTECTED.toString(), Attribute.HOUSE_ID.toString(), Attribute.ASSET_TYPE.toString());

	    Map<String, Object> returnMap = new HashMap<>();
	    returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.OK);
	    
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();
	    String assetType = attributeMap.get(Attribute.ASSET_TYPE.toString()).toString();
	    String protectedString = attributeMap.get(Attribute.PURGE_PROTECTED.toString()).toString();
	    Boolean purgeProtected = Boolean.parseBoolean(protectedString);
	    
	    if (purgeProtected == null || purgeProtected)
	    {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Protected asset cannot be uningested: " + houseID);
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "WARNING: " + assetType + " " + houseID + "is protected and cannot be deleted <OK>");
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.CONFIRM);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Warning user that : " + houseID + " and associated metadata will be deleted");
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "WARNING: You are about to delete this media and all associated metadata, are you sure you want to proceed? This cannot be undone. <OK, Cancel>");
	    }
	    
		return returnMap;
	}
	
	@Override
	public  Map<String, Object> exportMarkers(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString(), Attribute.ASSET_ID.toString());

	    Map<String, Object> returnMap = new HashMap<>();
	    returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.OK);
	    
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();
	    String assetID = attributeMap.get(Attribute.ASSET_ID.toString()).toString();
	    MarkerList markers = null;
		String revisionId = null;
		try
		{
			revisionId = RevisionUtil.findHighestRevision(assetID, client);
			markers = client.assetApi().getMarkers(AssetType.ITEM, assetID, revisionId);
			
			if (markers == null || markers.size() == 0)
			{
				returnMap.clear();
		    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
		    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "No export markers exist for " + houseID);
		    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "No export markers exist for " + houseID + " <OK>");
			}
		}
		catch (RemoteException e)
		{
			returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Exception thrown in Mayam while searching for markers for : " + houseID);
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "WARNING: A technical error occurred while retrieving markers for " + houseID + " <OK>");
		}
	    
		return returnMap;
	}
	
	@Override
	public  Map<String, Object> ingest(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.INGEST);
	}
	
	@Override
	public  Map<String, Object> txDelivery(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.TX_DELIVERY);
	}
	
	@Override
	public  Map<String, Object> autoQC(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.QC_VIEW);
	}
	
	@Override
	public  Map<String, Object> preview(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.PREVIEW);
	}
	
	@Override
	public  Map<String, Object> unmatched(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.UNMATCHED_MEDIA);
	}
	
	@Override
	public  Map<String, Object> complianceEdit(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();
	    String parentHouseID = attributeMap.get(Attribute.PARENT_HOUSE_ID.toString()).toString();
	    
	    Map<String, Object> returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_EDIT);
	    if (returnMap != null)
	    {
	    	Object status = returnMap.get(PDPAttributes.OP_STAT).toString();
	    	if (status.equals(StatusCodes.CONFIRM))
	    	{
	    		if (parentHouseID == null || parentHouseID.equals(""))
	    		{
	    	    	returnMap.clear();
	    	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Compile flag is not set for " + houseID + ". Compliance Edit task cannot be created");
	    	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "Compile flag is not set for " + houseID + ". Compliance Edit task cannot be created <OK>");
	    		}
	    	}
	    }
	    
	    return returnMap;
	}
	
	@Override
	public  Map<String, Object> complianceLogging(final Map<String, Object> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();
	    String parentHouseID = attributeMap.get(Attribute.PARENT_HOUSE_ID.toString()).toString();
	    
	    Map<String, Object> returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_LOGGING);
	    if (returnMap != null)
	    {
	    	Object status = returnMap.get(PDPAttributes.OP_STAT).toString();
	    	if (status.equals(StatusCodes.OK))
	    	{
	    		if (parentHouseID == null || parentHouseID.equals(""))
	    		{
	    	    	returnMap.clear();
	    	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Compile flag is not set for " + houseID + ". Compliance Logging task cannot be created");
	    	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "Compile flag is not set for " + houseID + ". Compliance Logging task cannot be created <OK>");
	    		}
	    	}
	    }
		return doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_LOGGING);
	}

	@Override
	public  Map<String, Object> protect(final Map<String, Object> attributeMap)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public  Map<String, Object> unprotect(final Map<String, Object> attributeMap)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}



	@Override
	public  Map<String, Object> proxyfileCheck(final Map<String, Object> attributeMap)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}




	/**
	 *
	 * @param attributeMap the incoming Mayam Attribute map
	 * @param attributeNamesRequired the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(Map<String, Object> attributeMap, String... attributeNamesRequired)
	{
		if (attributeMap == null || attributeMap.keySet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributeNamesRequired == null || attributeNamesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDPSetUp Internal Error: There must be some input map names from Mayam");

		for (String mapItemName: attributeNamesRequired)
		{
			if (!attributeMap.keySet().contains(mapItemName))
				throw new WebServiceException("Mayam Attribute " + mapItemName + " must be set in this call to the PDP");
		}

		return true;
	}


	/**
	 *
	 * @param success permit the result
	 * @param feedBackMessage message (can be null) to be displayed
	 *
	 * @return the standard attribute map result for Mayam.
	 */
	private Map<String, Object> formResult(boolean success, String feedBackMessage)
	{
		Map<String, Object> result = new HashMap<>();


		return result;
	}
	
	private Map<String, Object> doesTaskExist(String houseID, MayamTaskListType task)
	{
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.OK);
		
		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, task.getText());
		criteria.getFilterEqualities().setAttribute(Attribute.HOUSE_ID, houseID);
		FilterResult result = null;
		
		try
		{
			result = client.taskApi().getTasks(criteria, 10, 0);
		}
		catch(RemoteException e) {
			returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), "Exception thrown in Mayam while searching for existing tasks for : " + houseID);
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "WARNING: A technical error occurred while checking if tasks already exist for " + houseID + " <OK>");
		}
		
		if (result != null && result.getTotalMatches() > 0)
		{
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
	    	returnMap.put(PDPAttributes.ERROR_MSG.toString(), task.toString() + " task already exists for : " + houseID + ", will not create new task");
	    	returnMap.put(PDPAttributes.FORM_MSG.toString(), "Task already exists. No new task created <OK>");
		}
		
		return returnMap;
	}


}
