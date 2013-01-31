package com.mediasmiths.mq.handlers.tx;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioTXFlow;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.PackageNotFoundException;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class InitiateTxHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(InitiateTxHandler.class);

	@Inject
	private MuleWorkflowController mule;
	
	@Inject
	@Named("tx.delivery.location")
	private String txDeliveryLocation;
	
	@Override
	public String getName()
	{
		return "Initiate TX Delivery";
	}

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		try
		{
			String packageID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
			Long taskID = messageAttributes.getAttribute(Attribute.TASK_ID);

			SegmentList segmentList = packageController.getSegmentList(packageID);
			String materialID = segmentList.getAttributeMap().getAttribute(Attribute.PARENT_HOUSE_ID);
			AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);
			
			boolean isAO = AssetProperties.isAO(materialAttributes);
			boolean materialIsSurround = AssetProperties.isMaterialSurround(materialAttributes);
			boolean isPackageSD = AssetProperties.isPackageSD(segmentList.getAttributeMap());
			
			String title = materialAttributes.getAttributeAsString(Attribute.ASSET_TITLE);
			Date requiredDate = (Date) segmentList.getAttributeMap().getAttribute(Attribute.TX_FIRST);
			
			String materialPath = mayamClient.pathToMaterial(materialID);
			
			TCJobParameters tcParams = createTCParamsForTxDelivery(packageID, materialIsSurround,isPackageSD,requiredDate,materialPath);
			
			startTXFlow(isAO, packageID, requiredDate, taskID, tcParams, title);
			
		}
		catch (PackageNotFoundException pnfe)
		{
			log.error("package not found when attempting to initiate tx delivery!", pnfe);
		}
		catch (UnsupportedEncodingException e)
		{
			log.error("error invoking tx delivery",e);
		}
		catch (JAXBException e)
		{
			log.error("error invoking tx delivery",e);
		}
		catch (MayamClientException e)
		{
			log.error("error getting materials location");
		}
	}

	private TCJobParameters createTCParamsForTxDelivery(String packageID,boolean materialIsSurround, boolean isPackageSD, Date requiredDate, String materialPath)
	{
		TCJobParameters ret = new TCJobParameters();
		
		if(materialIsSurround){
			ret.audioType=TCAudioType.DOLBY_E;
		}
		else{
			ret.audioType=TCAudioType.STEREO;
		}
		
		//no bug for tx delivery
		ret.bug=null;
		ret.description=String.format("TX Delivery for package %s", packageID);
		
		ret.inputFile=materialPath;
		ret.outputFileBasename=packageID;
		ret.outputFolder=txDeliveryLocation;
		
		ret.priority=getPriorityForTXJob(requiredDate);
		
		if(isPackageSD){
			ret.purpose=TCOutputPurpose.TX_SD;	
		}
		else{
			ret.purpose=TCOutputPurpose.TX_HD;
		}
		
		//no timecode for tx delivery
		ret.timecode=null;
		
		return ret;
}
	
	
	

	private void startTXFlow(boolean isAO, String packageID, Date requiredDate, Long taskID, TCJobParameters tcParams, String title) throws UnsupportedEncodingException, JAXBException{
		InvokeIntalioTXFlow startMessage = new InvokeIntalioTXFlow();
		
		startMessage.setAO(isAO);
		startMessage.setPackageID(packageID);
		startMessage.setRequiredDate(requiredDate);
		startMessage.setTaskID(taskID);
		startMessage.setTcParams(tcParams);
		startMessage.setTitle(title);
		
		mule.initiateTxDeliveryWorkflow(startMessage);
		
	}
	
	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.TX_DELIVERY;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}

	private final static long ONE_HOUR = 1000 * 3600l;
	private final static long TWELVE_HOURS = ONE_HOUR * 12;
	private final static long ONE_DAY = ONE_HOUR * 24;
	private final static long THREE_DAYS = ONE_DAY * 3;
	private final static long EIGHT_DAYS = ONE_DAY * 8;
	
	
	public int getPriorityForTXJob(Date txDate)
	{
		log.debug(String.format("determining prority for job for asset tx date is %s",txDate.toString()));
	
		int priority=1;
		
		long now = System.currentTimeMillis();
		long txTime = txDate.getTime();
		long difference = txTime - now;
		
		if(difference < TWELVE_HOURS){
			priority=8;
		}
		else if(difference < ONE_DAY){
			priority=7;
		}
		else if(difference < THREE_DAYS){
			priority=6;
		}		
		else if(difference < EIGHT_DAYS){
			priority=4;
		}	
		else{
			priority=2;
		}
		
		log.debug("returning prority "+priority);
		return priority;
	}

	
}
