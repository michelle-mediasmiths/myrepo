package com.mediasmiths.mq.handlers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;

public class IngestCompleteHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(IngestCompleteHandler.class);
	Properties configProperties;
	
	public IngestCompleteHandler() 
	{
		configProperties = new Properties();
		try {
			configProperties.load(getClass().getClassLoader().getResourceAsStream("services/fx-MqClient.properties"));
		} catch (FileNotFoundException e) {
			log.error("Failed to find fx-MqClient.properties!", e);
		} catch (IOException e) {
			log.error("Failed to read from fx-MqClient.properties!", e);
		}
	}
	
	public void process(AttributeMap messageAttributes)
	{	
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.INGEST.getText())) 
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
			if (taskState == TaskState.FINISHED) 
			{
				Boolean qcRequired = (Boolean) messageAttributes.getAttribute(Attribute.QC_REQUIRED);
				if (!qcRequired)
				{
					String aggregator = messageAttributes.getAttribute(Attribute.AGGREGATOR);
					if (aggregator != null) 
					{
						String property = configProperties.getProperty("aggregators."+aggregator.toLowerCase()+".requiresQC");
						if (property != null)
						{
							qcRequired = property.equals(true);
						}
					}
				}
				
				if (qcRequired)
				{
					try
					{
						String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
						AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
						taskController.createTask(
								assetID,
								MayamAssetType.fromString(assetType.toString()),
								MayamTaskListType.QC_VIEW);
					}
					catch (Exception e)
					{
						log.error("Exception in the Mayam client while handling Ingest Complete Task Message : ", e);
					}
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return "Ingest Complete";		
	}
}
