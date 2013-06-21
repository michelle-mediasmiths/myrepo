package com.mediasmiths.mq.handlers.button.export;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.foxtel.tc.priorities.TranscodePriorities;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.button.ButtonClickHandler;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public abstract class ExportProxyButton extends ButtonClickHandler
{
	private final static Logger log = Logger.getLogger(ExportProxyButton.class);

	@Inject
	private MuleWorkflowController mule;

	@Inject
	@Named("export.ftp.user")
	private String exportFTPUser;

	@Inject
	@Named("export.ftp.password")
	private String exportFTPPassword;

	@Inject
	@Named("export.ftp.server")
	private String exportFTPServer;

	@Inject
	@Named("export.transient.tc.output.location")
	// where transcoded files go before ftp upload + deletion
	private String exportOutputLocation;

	@Inject
	protected ChannelProperties channelProperties;

	@Inject
	private EventService eventService;

	@Inject
	private TranscodePriorities transcodePriorities;

	@Override
	protected void buttonClicked(AttributeMap requestAttributes)
	{
		// asset could be an item, could be a segmentlist
		AssetType assetType = requestAttributes.getAttribute(Attribute.ASSET_TYPE);

		log.debug("Asset type is " + assetType.toString());

		
		boolean isPackage=false;
		AttributeMap materialAttributes;

		if (assetType.equals(MayamAssetType.PACKAGE.getAssetType()))
		{
			isPackage=true;
			log.debug("export is for a package");
			String materialID = (String) requestAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
			materialAttributes = materialController.getMaterialAttributes(materialID);
		}
		else if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			materialAttributes = requestAttributes;
		}
		else
		{
			log.error("unexpected asset type");
			throw new IllegalArgumentException("can only export items or packages");
		}

		boolean isAO = AssetProperties.isAO(materialAttributes);

		try
		{
			requestAttributes = setExportAttributes(requestAttributes,materialAttributes,isPackage);
		}
		catch (RemoteException e)
		{
			log.error("error setting export attributes",e);
			return;
		}
		
		if (!isAO)
		{

			String materialID = (String) materialAttributes.getAttribute(Attribute.HOUSE_ID);

			// create export task
			try
			{
				long taskID = createExportTask(requestAttributes, getJobType());
			}
			catch (MayamClientException e1)
			{
				log.error("error creating export task", e1);
				return;
			}
		}
		else
		{
			log.info("Item is ao, will not attempt to export");
		}

	}

	protected abstract TranscodeJobType getJobType();

	protected AttributeMap setExportAttributes(AttributeMap requestAttributes, AttributeMap materialAttributes, boolean exportIsForPackage) throws RemoteException{
		//allow customisation of export request attributes
		return requestAttributes;
	}
	
	private long createExportTask(AttributeMap requestAttributes, TranscodeJobType transcodeJobType) throws MayamClientException
	{
		return taskController.createExportTask(requestAttributes, transcodeJobType.getText());
	}

}
