package com.mediasmiths.mq.handlers.button.export;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.extendedpublishing.OutputPaths;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.mayam.MayamButtonType;
import org.apache.log4j.Logger;

public class CaptionProxy extends ExportProxyButton
{
	private final static Logger log = Logger.getLogger(CaptionProxy.class);	
	
	@Inject
	OutputPaths outputPaths;
	
	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.CAPTION_PROXY;
	}


	@Override
	public String getName()
	{
		return getJobType().getText();
	}
	
	@Override
	protected AttributeMap setExportAttributes(AttributeMap requestAttributes, AttributeMap materialAttributes, boolean exportIsForPackage) throws RemoteException{
		
		if(!exportIsForPackage){
			throw new IllegalArgumentException("Caption exports can only be made against packages");
		}
		
		String presentationID = (String) requestAttributes.getAttribute(Attribute.HOUSE_ID);
		String programmeTitle = (String) materialAttributes.getAttribute(Attribute.SERIES_TITLE);
		Integer seriesNumber = (Integer) materialAttributes.getAttribute(Attribute.SEASON_NUMBER);
		Integer episodeNumber = (Integer) materialAttributes.getAttribute(Attribute.EPISODE_NUMBER);
		
		Integer exportVersion = (Integer) requestAttributes.getAttribute(Attribute.VERSION_NUMBER);
		
		if(exportVersion==null){
			log.debug("First export for this package");
			exportVersion = 0;			
		}
		
		exportVersion=Integer.valueOf(exportVersion.intValue() + 1);
		
		AttributeMap updateMap = taskController.updateMapForAsset(requestAttributes);
		updateMap.setAttribute(Attribute.VERSION_NUMBER, exportVersion);
		log.debug("Setting updated export version on package");
		tasksClient.assetApi().updateAsset(updateMap);
		
		
		String filename = outputPaths.getFileNameForCaptionExport(presentationID, programmeTitle, seriesNumber, episodeNumber, exportVersion);
		requestAttributes.setAttribute(Attribute.OP_FILENAME, filename);
		requestAttributes.setAttribute(Attribute.OP_FLAG, Boolean.TRUE);
		
		return requestAttributes;
	}
	

	@Override
	protected TranscodeJobType getJobType()
	{
		return TranscodeJobType.CAPTION_PROXY;
	}

}
