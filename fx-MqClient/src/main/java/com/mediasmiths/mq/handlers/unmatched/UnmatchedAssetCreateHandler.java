package com.mediasmiths.mq.handlers.unmatched;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mq.handlers.AttributeHandler;

public class UnmatchedAssetCreateHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedAssetCreateHandler.class);

	@Inject
	@Named("purge.unmatch.material.days")
	private int purgeTimeForUmatchedMaterial;
	
	public void process(AttributeMap messageAttributes)
	{
		String contentMaterialType = messageAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		if (contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.UNMATCHED))
		{
			try
			{
				// Add to purge candidate list with expiry date of 30 days
				String houseId = messageAttributes.getAttribute(Attribute.HOUSE_ID);
				taskController.createOrUpdatePurgeCandidateTaskForAsset(MayamAssetType.MATERIAL,houseId, purgeTimeForUmatchedMaterial);
			}
			catch (Exception e)
			{
				log.error("Exception creating purge candidate task : ", e);
			}
		}
	}

	@Override
	public String getName()
	{
		return "Unmatched Asset Create";
	}
}
