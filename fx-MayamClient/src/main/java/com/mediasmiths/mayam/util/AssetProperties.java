package com.mediasmiths.mayam.util;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.controllers.MayamMaterialController;

public class AssetProperties
{
	private final static Logger log = Logger.getLogger(AssetProperties.class);

	public static boolean isMaterialSD(AttributeMap map)
	{

		String format = map.getAttribute(Attribute.CONT_FMT);

		if (format != null && format.equals("SD"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isFromDARTorVizCapture(AttributeMap map)
	{

		String agg = map.getAttribute(Attribute.AGGREGATOR);

		if (agg == null)
		{
			return false;
		}

		agg = agg.toLowerCase();

		if ("dart".equals(agg))
		{
			return true;
		}

		if ("vizcapture".equals(agg))
		{
			return true;
		}

		return false;

	}

	public static boolean isMaterialSurround(AttributeMap map)
	{

		AudioTrackList audioTracks = map.getAttribute(Attribute.AUDIO_TRACKS);

		if(audioTracks != null && audioTracks.size() >= 8)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean isPackageSD(AttributeMap packageAttributes)
	{

		String reqFMT = packageAttributes.getAttributeAsString(Attribute.REQ_FMT);

		if ("sd".equals(reqFMT.toLowerCase()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean isMaterialProgramme(AttributeMap currentAttributes)
	{
		String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		return (contentType != null && MayamMaterialController.PROGRAMME_MATERIAL_CONTENT_TYPE.equals(contentType));
	}

	public static boolean isMaterialAssociated(AttributeMap currentAttributes)
	{
		String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		return (contentType != null && MayamMaterialController.ASSOCIATED_MATERIAL_CONTENT_TYPE.equals(contentType));
	}

	public static boolean isMaterialPlaceholder(AttributeMap materialAttributes)
	{

		MediaStatus mediaStatus = materialAttributes.getAttribute(Attribute.MEDST_HR);
		if (mediaStatus != null)
		{
			log.debug("Media status is " + mediaStatus.toString());
			if (mediaStatus == MediaStatus.MISSING)
			{
				return true;
			}
		}
		else
		{
			log.error("Media status is null");
		}

		return false;

	}

	public static boolean isClassificationSet(AttributeMap packageAttributes)
	{
		String classification = packageAttributes.getAttribute(Attribute.CONT_CLASSIFICATION);
		return classification != null;
	}

	public static boolean isQCPassed(AttributeMap materialAttributes)
	{

		QcStatus qcStatus = materialAttributes.getAttribute(Attribute.QC_STATUS);

		if (qcStatus != null)
		{
			return qcStatus.equals(QcStatus.PASS) || qcStatus.equals(QcStatus.PASS_MANUAL);
		}

		return false;

	}

	public static boolean isQCStatusDetermined(AttributeMap materialAttributes)
	{

		QcStatus qcStatus = materialAttributes.getAttribute(Attribute.QC_STATUS);

		if (qcStatus == null)
		{
			return false;
		}
		else if (qcStatus.equals(QcStatus.TBD))
		{
			return false;
		}
		else
		{
			return true;
		}

	}

	public static boolean isPurgeProtected(AttributeMap asset)
	{

		Boolean purgeProtected = asset.getAttribute(Attribute.PURGE_PROTECTED);

		if (purgeProtected == null)
		{
			return false;
		}
		else
		{
			return purgeProtected.booleanValue();
		}
	}

	public static boolean isAO(AttributeMap attributes)
	{
		Boolean p = attributes.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);

		if (p == null)
		{
			return false;
		}
		else
		{
			return p.booleanValue();
		}
	}
	
	public static boolean isPackageTXReady(AttributeMap attributes){
		
		Boolean p = attributes.getAttribute(Attribute.TX_READY);
		
		if(p==null){
			return false;
		}
		else{
			return p.booleanValue();
		}
				
		
		
	}

	public static boolean isQCParallel(AttributeMap attributes)
	{
		Boolean p = attributes.getAttribute(Attribute.QC_PARALLEL_ALLOWED);
		
		if(p==null){
			return false;
		}
		else{
			return p.booleanValue();
		}
				
		
	}

	public static boolean isMaterialPreviewPassed(AttributeMap material) {
	
		String qcPreviewResult = material.getAttribute(Attribute.QC_PREVIEW_RESULT);
		
		if(qcPreviewResult==null){
			return false;
		}
		else{
			return MayamPreviewResults.isPreviewPass(qcPreviewResult);
		}
		
	}
	
	public static boolean shouldPackagesForMaterialBeConsideredPending(AttributeMap material){
		boolean materialHasPreviewPass = AssetProperties.isMaterialPreviewPassed(material);
		boolean materialHasMedia = !AssetProperties.isMaterialPlaceholder(material);
		boolean pendingPackage = ! ( materialHasPreviewPass && materialHasMedia);
		
		return pendingPackage;				
	}

	public static boolean hasQCStatus(AttributeMap material)
	{
		QcStatus qcStatus = material.getAttribute(Attribute.QC_STATUS);

		if (qcStatus == null)
		{
			return false;
		}
		else
		{
			if (qcStatus.equals(QcStatus.TBD))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

}
