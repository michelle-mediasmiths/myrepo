package com.mediasmiths.foxtel.ip.mayam.pdp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributeMapMapper;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.WebServiceException;
import java.util.Map;
import java.util.Set;

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
	@Named("foxtel.groups.nonao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> permissionsNonAO;

	@Inject
	@Named("foxtel.groups.ao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> permissionsAO;

	private final AttributeMapMapper mapper;
	
	@Inject
	public MayamPDPImpl(@Named(MayamClientModule.SETUP_TASKS_CLIENT)TasksClient client, MayamTaskController taskController,AttributeMapMapper mapper) 
	{
		this.client=client;
		this.taskController=taskController;
		this.mapper=mapper;
	}

	@Override
	public String ping()
	{
		return "<html>Ping</html>";
	}

	@Override
	public  String segmentMismatch(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.REQ_NUMBER, Attribute.HOUSE_ID);

	    AttributeMap returnMap = client.createAttributeMap();
	    returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK);
	    
	    //Segmentation check
	    String requestedNumber = attributeMap.getAttributeAsString(Attribute.REQ_NUMBER);
	    int numberOfSegmentsRequested = Integer.parseInt(requestedNumber);
	    String presentationID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
	    
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
		    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.CONFIRM.toString());
		    	returnMap.setAttribute(Attribute.FORM_MSG_ERROR, "The number of segments submitted does not match that requested by the channel. Are you sure you wish to proceed?");
		    }
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	returnMap.setAttribute(Attribute.ERROR_MSG, "A technical fault has occurred while retrieving segemnt list");
	    }
	    
		return mapper.serialize(returnMap);
	}
	
	@Override
	public  String segmentClassificationCheck(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, null, null);

	    AttributeMap returnMap = client.createAttributeMap();
	    returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK);
	    
	    String classification = attributeMap.getAttributeAsString(Attribute.CONT_CLASSIFICATION);
	    if (classification == null || classification.equals(""))
	    {
	    	String presentationID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
	    	
	    	returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	returnMap.setAttribute(Attribute.ERROR_MSG, "The TX Package has not been classified. Please contact the channel owner and ensure that this is provided");
	    }
	    
		return  mapper.serialize(returnMap);
	}

	@Override
	public String uningest(final String attributeMapStr)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public  String uningestProtected(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
		validateAttributeMap(attributeMap, Attribute.PURGE_PROTECTED);

	    AttributeMap returnMap = client.createAttributeMap();
	    returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK);
	    
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
	    String assetType = attributeMap.getAttributeAsString(Attribute.ASSET_TYPE);
	    String protectedString = attributeMap.getAttributeAsString(Attribute.PURGE_PROTECTED);
	    Boolean purgeProtected = Boolean.parseBoolean(protectedString);
	    
	    if (purgeProtected == null || purgeProtected)
	    {
	    	returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	returnMap.setAttribute(Attribute.ERROR_MSG, "WARNING: " + assetType + " " + houseID + "is protected and cannot be uningested");
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.CONFIRM);
	    	returnMap.setAttribute(Attribute.FORM_MSG_ERROR, "WARNING: You are about to delete this media and all associated metadata, are you sure you want to proceed? This cannot be undone.");
	    }
	    
		return  mapper.serialize(returnMap);
	}

	@Override
	public  String delete(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.PURGE_PROTECTED, Attribute.HOUSE_ID, Attribute.ASSET_TYPE);

	    AttributeMap returnMap = client.createAttributeMap();
	    returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK);
	    
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
	    String assetType = attributeMap.getAttributeAsString(Attribute.ASSET_TYPE);
	    String protectedString = attributeMap.getAttributeAsString(Attribute.PURGE_PROTECTED);
	    Boolean purgeProtected = Boolean.parseBoolean(protectedString);
	    
	    if (purgeProtected == null || purgeProtected)
	    {
	    	returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	returnMap.setAttribute(Attribute.ERROR_MSG, "WARNING: " + assetType + " " + houseID + "is protected and cannot be deleted");
	    }
	    else {
	    	returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.CONFIRM);
	    	returnMap.setAttribute(Attribute.FORM_MSG_ERROR, "WARNING: You are about to delete this media and all associated metadata, are you sure you want to proceed? This cannot be undone.");
	    }
	    
		return  mapper.serialize(returnMap);
	}
	
	@Override
	public  String exportMarkers(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID, Attribute.ASSET_ID);

	    AttributeMap returnMap = client.createAttributeMap();
	    returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK);
	    
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
	    String assetID = attributeMap.getAttributeAsString(Attribute.ASSET_ID);
	    MarkerList markers = null;
		String revisionId = null;
		try
		{
			revisionId = RevisionUtil.findHighestRevision(assetID, client);
			markers = client.assetApi().getMarkers(AssetType.ITEM, assetID, revisionId);
			
			if (markers == null || markers.size() == 0)
			{
				returnMap.clear();
		    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
		    	returnMap.setAttribute(Attribute.ERROR_MSG, "No export markers exist for " + houseID);
			}
		}
		catch (RemoteException e)
		{
			returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	returnMap.setAttribute(Attribute.ERROR_MSG, "WARNING: A technical error occurred while retrieving markers for " + houseID);
		}
	    
		return  mapper.serialize(returnMap);
	}

	@Override
	public String complianceProxy(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String captionsProxy(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String publicityProxy(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public  String ingest(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
		validateAttributeMap(attributeMap, Attribute.HOUSE_ID);
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

		return  mapper.serialize(doesTaskExist(houseID, MayamTaskListType.INGEST));
	}
	
	@Override
	public  String txDelivery(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID);
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

		return  mapper.serialize(doesTaskExist(houseID, MayamTaskListType.TX_DELIVERY));
	}
	
	@Override
	public  String autoQC(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID);
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

		return  mapper.serialize(doesTaskExist(houseID, MayamTaskListType.QC_VIEW));
	}
	
	@Override
	public  String preview(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID);
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

		return  mapper.serialize(doesTaskExist(houseID, MayamTaskListType.PREVIEW));
	}
	
	@Override
	public  String unmatched(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID);
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

		return  mapper.serialize(doesTaskExist(houseID, MayamTaskListType.UNMATCHED_MEDIA));
	}
	
	@Override
	public  String complianceEdit(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID);
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
	    String parentHouseID = attributeMap.getAttributeAsString(Attribute.PARENT_HOUSE_ID);
	    
	    AttributeMap returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_EDIT);
	    if (returnMap != null)
	    {
	    	Object status = returnMap.getAttribute(Attribute.OP_STAT).toString();
	    	if (status.equals(StatusCodes.CONFIRM))
	    	{
	    		if (parentHouseID == null || parentHouseID.equals(""))
	    		{
	    	    	returnMap.clear();
	    	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	    	returnMap.setAttribute(Attribute.ERROR_MSG, "Compile flag is not set for " + houseID + ". Compliance Edit task cannot be created");
	    		}
	    	}
	    }
	    
	    return  mapper.serialize(returnMap);
	}
	
	@Override
	public  String complianceLogging(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
		validateAttributeMap(attributeMap, Attribute.HOUSE_ID);
		String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
		String sourceHouseId = attributeMap.getAttributeAsString(Attribute.SOURCE_HOUSE_ID);

		AttributeMap returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_LOGGING);
		if (returnMap != null)
		{
			Object status = returnMap.getAttribute(Attribute.OP_STAT).toString();
			if (status.equals(StatusCodes.OK))
			{
				if (sourceHouseId == null || sourceHouseId.equals(""))
				{
					returnMap.clear();
					returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
					returnMap.setAttribute(Attribute.ERROR_MSG, "String.format(messageComplianceLoggingNone, houseID)");
				}
			}
		}
		return  mapper.serialize(doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_LOGGING));
	}

	@Override
	public  String protect(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public  String unprotect(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}



	@Override
	public  String proxyfileCheck(final String attributeMapStr)
	{
		final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
	    validateAttributeMap(attributeMap, Attribute.HOUSE_ID, Attribute.ASSET_ID, Attribute.OP_FILENAME);

	    AttributeMap returnMap = client.createAttributeMap();
	    returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK);
	    
	    String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
	    String filename = attributeMap.getAttributeAsString(Attribute.OP_FILENAME);
	    
	    try
	    {
			AttributeMap filterEqualities = client.createAttributeMap();
			filterEqualities.setAttribute(Attribute.OP_FILENAME, filename);
			filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.EXTENDED_PUBLISHING);
			FilterCriteria criteria = new FilterCriteria();
			criteria.setFilterEqualities(filterEqualities);
			FilterResult existingTasks = client.taskApi().getTasks(criteria, 50, 0);
			
			if (existingTasks != null && existingTasks.getTotalMatches() > 0) {
				returnMap.clear();
		    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
		    	returnMap.setAttribute(Attribute.ERROR_MSG, "A Proxy with name " + filename + " already exists");
			}
	    }
	    catch(RemoteException e)
	    {
			returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	returnMap.setAttribute(Attribute.ERROR_MSG, "WARNING: A technical error occurred while retrieving existing Extended Publishing tasks");
	    }
	    
		return  mapper.serialize(returnMap);
	}




	/**
	 *
	 * @param attributeMap the incoming Mayam Attribute map
	 * @param attributeNamesRequired the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(AttributeMap attributeMap, Attribute... attributeNamesRequired)
	{
		if (attributeMap == null || attributeMap.getAttributeSet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributeNamesRequired == null || attributeNamesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDPSetUp Internal Error: There must be some input map names from Mayam");

		for (Attribute a: attributeNamesRequired)
		{
			if (!attributeMap.getAttributeSet().contains(a))
				throw new WebServiceException("Mayam Attribute " + a + " must be set in this call to the PDP");
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
	private AttributeMap formResult(boolean success, String feedBackMessage)
	{
		AttributeMap result = client.createAttributeMap();


		return result;
	}
	
	private AttributeMap doesTaskExist(String houseID, MayamTaskListType task)
	{
		AttributeMap returnMap = client.createAttributeMap();
		returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK);
		
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
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	returnMap.setAttribute(Attribute.ERROR_MSG, "WARNING: A technical error occurred while checking if tasks already exist for " + houseID);
		}
		
		if (result != null && result.getTotalMatches() > 0)
		{
	    	returnMap.clear();
	    	returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR);
	    	returnMap.setAttribute(Attribute.ERROR_MSG, "Task already exists. No new task created");
		}
		
		return returnMap;
	}

	@Override
	@Path("qcParallel")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String qcParallel(String attributeMapStr) throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fileHeaderVerifyOverride(final String attributeMapStr) throws RemoteException
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}


}
