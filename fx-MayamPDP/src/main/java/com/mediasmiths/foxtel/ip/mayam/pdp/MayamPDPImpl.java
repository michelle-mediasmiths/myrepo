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
	public  Map<String, String> segmentMismatch(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.REQ_NUMBER.toString(), Attribute.HOUSE_ID.toString());

	    Map<String, String> returnMap = new HashMap<>();
	    returnMap.put(PDPAttributes.STATUS.toString(), "Success");
	    
	    //Segmentation check
	    String requestedNumber = attributeMap.get(Attribute.REQ_NUMBER.toString());
	    int numberOfSegmentsRequested = Integer.parseInt(requestedNumber);
	    String presentationID = attributeMap.get(Attribute.HOUSE_ID.toString());
	    
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
		    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
		    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.SEGMENT_NUMBER_MISMATCH.toString());
		    	returnMap.put(PDPAttributes.LOGGING.toString(), "Presentation ID : " + presentationID + ", user has requested " + numberOfSegmentsRequested + " segemenst, while placeholder contained " + segmentsSize);
		    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "The number of segments submitted does not match that requested by the channel. Are you sure you wish to proceed <OK, cancel>");
		    }
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Success");
	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.TECHNICAL_FAULT.toString());
	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Unable to retrieve Segment List for Presentation ID : " + presentationID);
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "A technical fault has occurred while retrieving segemnt list");
	    }
	    
		return returnMap;
	}
	
	@Override
	public  Map<String, String> segmentClassificationCheck(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, null, null);

	    Map<String, String> returnMap = new HashMap<String, String>();
	    returnMap.put(PDPAttributes.STATUS.toString(), "Success");
	    
	    String classification = attributeMap.get(Attribute.CONT_CLASSIFICATION.toString());
	    if (classification == null || classification.equals(""))
	    {
	    	String presentationID = attributeMap.get(Attribute.HOUSE_ID.toString());
	    	
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.CLASSIFICATION_FAILURE.toString());
	    	returnMap.put(PDPAttributes.LOGGING.toString(), "The Tx Package has not been classified: " + presentationID);
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "The TX Package has not been classified. Please contact the channel owner and ensure that this is provided <OK>");
	    }
	    
		return returnMap;
	}
	
	@Override
	public  Map<String, String> uningestProtected(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.PURGE_PROTECTED.toString());

	    Map<String, String> returnMap = new HashMap<String, String>();
	    returnMap.put(PDPAttributes.STATUS.toString(), "Success");
	    
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());
	    String assetType = attributeMap.get(Attribute.ASSET_TYPE.toString());
	    String protectedString = attributeMap.get(Attribute.PURGE_PROTECTED.toString());
	    Boolean purgeProtected = Boolean.parseBoolean(protectedString);
	    
	    if (purgeProtected == null || purgeProtected)
	    {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.PROTECTED.toString());
	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Protected asset cannot be uningested: " + houseID);
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "WARNING: " + assetType + " " + houseID + "is protected and cannot be uningested <OK>");
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Warning");
	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Warning user that : " + houseID + " and associated metadata will be deleted");
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "WARNING: You are about to delete this media and all associated metadata, are you sure you want to proceed? This cannot be undone. <OK, Cancel>");
	    }
	    
		return returnMap;
	}

	@Override
	public  Map<String, String> delete(final Map<String, String> attributeMap)
	{
		validateAttributeMap(attributeMap, Attribute.PURGE_PROTECTED.toString(), Attribute.HOUSE_ID.toString(), Attribute.ASSET_TYPE.toString());

		Map<String, String> returnMap = new HashMap<>();
		returnMap.put(PDPAttributes.STATUS.toString(), "Success");


		return returnMap;
	}

	@Override
	public  Map<String, String> deleteProtected(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.PURGE_PROTECTED.toString(), Attribute.HOUSE_ID.toString(), Attribute.ASSET_TYPE.toString());

	    Map<String, String> returnMap = new HashMap<>();
	    returnMap.put(PDPAttributes.STATUS.toString(), "Success");
	    
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());
	    String assetType = attributeMap.get(Attribute.ASSET_TYPE.toString());
	    String protectedString = attributeMap.get(Attribute.PURGE_PROTECTED.toString());
	    Boolean purgeProtected = Boolean.parseBoolean(protectedString);
	    
	    if (purgeProtected == null || purgeProtected)
	    {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.PROTECTED.toString());
	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Protected asset cannot be uningested: " + houseID);
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "WARNING: " + assetType + " " + houseID + "is protected and cannot be deleted <OK>");
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Warning");
	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Warning user that : " + houseID + " and associated metadata will be deleted");
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "WARNING: You are about to delete this media and all associated metadata, are you sure you want to proceed? This cannot be undone. <OK, Cancel>");
	    }
	    
		return returnMap;
	}
	
	@Override
	public  Map<String, String> exportMarkers(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString(), Attribute.ASSET_ID.toString());

	    Map<String, String> returnMap = new HashMap<>();
	    returnMap.put(PDPAttributes.STATUS.toString(), "Success");
	    
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()); 
	    String assetID = attributeMap.get(Attribute.ASSET_ID.toString());
	    MarkerList markers = null;
		String revisionId = null;
		try
		{
			revisionId = RevisionUtil.findHighestRevision(assetID, client);
			markers = client.assetApi().getMarkers(AssetType.ITEM, assetID, revisionId);
			
			if (markers == null || markers.size() == 0)
			{
				returnMap.clear();
		    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
		    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.TECHNICAL_FAULT.toString());
		    	returnMap.put(PDPAttributes.LOGGING.toString(), "No export markers exist for " + houseID);
		    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "No export markers exist for " + houseID + " <OK>");
			}
		}
		catch (RemoteException e)
		{
			returnMap.clear();
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.TECHNICAL_FAULT.toString());
	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Exception thrown in Mayam while searching for markers for : " + houseID);
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "WARNING: A technical error occurred while retrieving markers for " + houseID + " <OK>");
		}
	    
		return returnMap;
	}
	
	@Override
	public  Map<String, String> ingest(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());

		return doesTaskExist(houseID, MayamTaskListType.INGEST);
	}
	
	@Override
	public  Map<String, String> txDelivery(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());

		return doesTaskExist(houseID, MayamTaskListType.TX_DELIVERY);
	}
	
	@Override
	public  Map<String, String> autoQC(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());

		return doesTaskExist(houseID, MayamTaskListType.QC_VIEW);
	}
	
	@Override
	public  Map<String, String> preview(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());

		return doesTaskExist(houseID, MayamTaskListType.PREVIEW);
	}
	
	@Override
	public  Map<String, String> unmatched(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());

		return doesTaskExist(houseID, MayamTaskListType.UNMATCHED_MEDIA);
	}
	
	@Override
	public  Map<String, String> complianceEdit(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());
	    String parentHouseID = attributeMap.get(Attribute.PARENT_HOUSE_ID.toString());
	    
	    Map<String, String> returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_EDIT);
	    if (returnMap != null)
	    {
	    	String status = returnMap.get(PDPAttributes.STATUS);
	    	if (status.equals("Success"))
	    	{
	    		if (parentHouseID == null || parentHouseID.equals(""))
	    		{
	    	    	returnMap.clear();
	    	    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
	    	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.COMPILE_FLAG_NOT_SET.toString());
	    	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Compile flag is not set for " + houseID + ". Compliance Edit task cannot be created");
	    	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "Compile flag is not set for " + houseID + ". Compliance Edit task cannot be created <OK>");
	    		}
	    	}
	    }
	    
	    return returnMap;
	}
	
	@Override
	public  Map<String, String> complianceLogging(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
	    String houseID = attributeMap.get(Attribute.HOUSE_ID.toString());
	    String parentHouseID = attributeMap.get(Attribute.PARENT_HOUSE_ID.toString());
	    
	    Map<String, String> returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_LOGGING);
	    if (returnMap != null)
	    {
	    	String status = returnMap.get(PDPAttributes.STATUS);
	    	if (status.equals("Success"))
	    	{
	    		if (parentHouseID == null || parentHouseID.equals(""))
	    		{
	    	    	returnMap.clear();
	    	    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
	    	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.COMPILE_FLAG_NOT_SET.toString());
	    	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Compile flag is not set for " + houseID + ". Compliance Logging task cannot be created");
	    	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "Compile flag is not set for " + houseID + ". Compliance Logging task cannot be created <OK>");
	    		}
	    	}
	    }
		return doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_LOGGING);
	}

	@Override
	public  Map<String, String> protect(final Map<String, String> attributeMap)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public  Map<String, String> unprotect(final Map<String, String> attributeMap)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}



	@Override
	public  Map<String, String> proxyfileCheck(final Map<String, String> attributeMap)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}




	/**
	 *
	 * @param attributeMap the incoming Mayam Attribute map
	 * @param attributeNamesRequired the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(Map<String, String> attributeMap, String... attributeNamesRequired)
	{
		if (attributeMap == null || attributeMap.keySet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributeNamesRequired == null || attributeNamesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDP Internal Error: There must be some input map names from Mayam");

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
	private Map<String, String> formResult(boolean success, String feedBackMessage)
	{
		Map<String, String> result = new HashMap<>();


		return result;
	}
	
	private Map<String, String> doesTaskExist(String houseID, MayamTaskListType task)
	{
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put(PDPAttributes.STATUS.toString(), "Success");
		
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
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.TECHNICAL_FAULT.toString());
	    	returnMap.put(PDPAttributes.LOGGING.toString(), "Exception thrown in Mayam while searching for existing tasks for : " + houseID);
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "WARNING: A technical error occurred while checking if tasks already exist for " + houseID + " <OK>");
		}
		
		if (result != null && result.getTotalMatches() > 0)
		{
	    	returnMap.clear();
	    	returnMap.put(PDPAttributes.STATUS.toString(), "Failure");
	    	returnMap.put(PDPAttributes.FAILURE_CODE.toString(), PDPErrorCodes.TASK_ALREADY_EXISTS.toString());
	    	returnMap.put(PDPAttributes.LOGGING.toString(), task.toString() + " task already exists for : " + houseID + ", will not create new task");
	    	returnMap.put(PDPAttributes.UI_MESSAGE.toString(), "An " + task.toString() + " task already exists for : " + houseID  + ", cannot create new task. <OK>");
		}
		
		return returnMap;
	}


}
