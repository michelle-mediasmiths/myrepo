package com.mediasmiths.mq.handlers.asset;

import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;

public class AccessUpdateHandler extends UpdateAttributeHandler
{
	private final static Logger log = Logger.getLogger(AccessUpdateHandler.class);

	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		boolean anyChanged = false;

		for (Attribute a : MayamAccessRightsController.attributesAffectingAccessRights)
		{
			if (attributeChanged(a, before, after,currentAttributes))
			{
				anyChanged = true;
				break;
			}
		}

		if (anyChanged)
		{
			log.info("Attributes affecting access rights changed, updating");
			
			try
			{
				AttributeMap withNewAccessRights = accessRightsController.updateAccessRights(currentAttributes.copy());
				AttributeMap update = taskController.updateMapForAsset(withNewAccessRights);
				update.setAttribute(Attribute.ASSET_ACCESS, withNewAccessRights.getAttribute(Attribute.ASSET_ACCESS));
				tasksClient.assetApi().updateAsset(update);
				
				String assetId = currentAttributes.getAttribute(Attribute.ASSET_ID);
				AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
				List<AttributeMap> openTasks = taskController.getAllOpenTasksForAsset(assetType, Attribute.ASSET_ID, assetId);
				for (AttributeMap task: openTasks)
				{
					AttributeMap updatedTaskRights = accessRightsController.updateAccessRights(task.copy());
					AttributeMap updateTask = taskController.updateMapForTask(updatedTaskRights);
					updateTask.setAttribute(Attribute.ASSET_ACCESS, updatedTaskRights.getAttribute(Attribute.ASSET_ACCESS));
					tasksClient.taskApi().updateTask(updateTask);
				}
			}
			catch (Exception e)
			{
				log.error("error updating assets access rights", e);
			}
		}
	}

	@Override
	public String getName()
	{
		return "Access rights";
	}
}
