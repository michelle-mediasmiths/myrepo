package com.mediasmiths.mq.handlers.purge;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.PurgeEventNotification;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.mayam.DateUtil;
import org.apache.log4j.Logger;

import java.util.Date;

public class PurgeEvent
{

	@Inject(optional = false)
	@Named("bms.events.namespace")
	private String namespace;

	@Inject
	private EventService eventsService;

	@Inject
	private DateUtil dateUtil;

	private final static Logger log = Logger.getLogger(PurgeEvent.class);


	/**
	 * Sends a PurgeEventNotification when an asset deleted message is received
	 *
	 * @param assetAttributes
	 * 		- the attributes from the delete message, this has only the attributes normally found in asset create and update messages,
	 * 		it wont have the extra attributes that are present on tasks
	 */
	public void sendPurgeEventOnDelete(AttributeMap assetAttributes)
	{
		PurgeEventNotification pe = preparePurgeEventForTitleOrMaterial(assetAttributes);
		if (pe == null)
		{
			return;
		}
		pe.setPurged(Boolean.TRUE);
		sendEvent(pe);
	}


	/**
	 * Sends a PurgeEventNotification when the purge protected flag for an asset is changed
	 *
	 * @param assetAttributes
	 * 		- the attributes from the asset update message, this has only the attributes normally found in asset create and update
	 * 		messages,
	 * 		it wont have the extra attributes that are present on tasks
	 */
	public void sendPurgeEventOnProtectedChange(AttributeMap assetAttributes)
	{
		PurgeEventNotification pe = preparePurgeEventForTitleOrMaterial(assetAttributes);
		if (pe == null)
		{
			return;
		}
		updateProtectedFlag(assetAttributes, pe);
		sendEvent(pe);
	}


	/**
	 * Sends a PurgeEventNotification, called when a purge candidate task is created or changes state
	 *
	 * @param taskAttributes - Attributes of a purge candidate task
	 */
	public void setPurgeEventForPurgeCandidateTask(AttributeMap taskAttributes)
	{
		PurgeEventNotification pe = preparePurgeEventForMaterial(taskAttributes);
		if (pe == null)
		{
			return;
		}
		updateProtectedFlag(taskAttributes, pe);
		pe.setHasPurgeCandidateTask(Boolean.TRUE);
		updateExpiresDate(taskAttributes, pe);
		updateExtended(taskAttributes, pe);

		sendEvent(pe);
	}


	private void updateExtended(final AttributeMap taskAttributes, final PurgeEventNotification pe)
	{
		TaskState ts = taskAttributes.getAttribute(Attribute.TASK_STATE);

		if (TaskState.EXTENDED.equals(ts))
		{
			pe.setExtended(Boolean.TRUE);
		}
	}


	private void updateExpiresDate(final AttributeMap taskAttributes, final PurgeEventNotification pe)
	{
		final Date expires = taskAttributes.getAttribute(Attribute.OP_DATE);
		if (expires != null)
		{
			pe.setExpires(dateUtil.fromDate(expires));
		}
	}


	private void sendEvent(final PurgeEventNotification pe)
	{
		eventsService.saveEvent(namespace, EventNames.PURGE_EVENT_NOTIFICATION, pe);
	}


	private void updateProtectedFlag(final AttributeMap assetAttributes, final PurgeEventNotification pe)
	{
		final Boolean isProtected = assetAttributes.getAttribute(Attribute.PURGE_PROTECTED);

		log.info("Purge protected flag is " + isProtected);
		pe.setProtected(isProtected);
	}


	private PurgeEventNotification preparePurgeEventForMaterial(final AttributeMap attributes)
	{
		PurgeEventNotification pe = new PurgeEventNotification();

		final AssetType assetType = attributes.getAttribute(Attribute.ASSET_TYPE);
		final String houseId = attributes.getAttribute(Attribute.HOUSE_ID);

		if (assetType == null)
		{
			log.debug("null asset type");
			return null;
		}

		pe.setAssetType(assetType.toString());

		if (AssetType.ITEM == assetType)
		{
			pe.setMaterialId(houseId);
		}
		else
		{
			log.warn(String.format("Unexpected asset type %s , ignoring", assetType));
			return null;
		}
		pe.setHouseId(houseId);
		return pe;
	}


	private PurgeEventNotification preparePurgeEventForTitleOrMaterial(final AttributeMap assetAttributes)
	{
		PurgeEventNotification pe = new PurgeEventNotification();

		final AssetType assetType = assetAttributes.getAttribute(Attribute.ASSET_TYPE);
		final String houseId = assetAttributes.getAttribute(Attribute.HOUSE_ID);

		if (assetType == null)
		{
			log.debug("null asset type");
			return null;
		}
		pe.setAssetType(assetType.toString());

		switch (assetType)
		{

			case COLLECTION:
			case LOG:
			case SERIES:
			case PACKAGE:
			case REVISION:
				log.warn(String.format("Unexpected asset type %s , ignoring", assetType));
				return null;
			case SEGMENT_LIST:
				break;
			case EPISODE:
				pe.setTitleId(houseId);
				break;
			case ITEM:
				pe.setMaterialId(houseId);
				break;
		}

		pe.setHouseId(houseId);
		return pe;
	}
}
