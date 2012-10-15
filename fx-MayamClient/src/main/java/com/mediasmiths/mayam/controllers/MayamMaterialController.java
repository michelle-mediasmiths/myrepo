package com.mediasmiths.mayam.controllers;

import java.util.List;

import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.Aggregator;
import au.com.foxtel.cf.mam.pms.Compile;
import au.com.foxtel.cf.mam.pms.Library;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.Source;
import au.com.foxtel.cf.mam.pms.TapeType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AspectRatio;
import com.mayam.wf.attributes.shared.type.IdSet;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamMaterialController {
	private final TasksClient client;
	
	@Inject
	public MayamMaterialController(@Named(SETUP_TASKS_CLIENT)TasksClient mayamClient) {
		client = mayamClient;
	}
	
	public MayamClientErrorCode createMaterial(MaterialType material)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		
		if (material != null) 
		{
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_ID, material.getMaterialID());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.QC_NOTES, material.getQualityCheckTask().toString());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_FMT, material.getRequiredFormat());
			
			Source source = material.getSource();
			if (source != null) {
				Aggregation aggregation = source.getAggregation();
				if (aggregation != null) {
					Aggregator aggregator = aggregation.getAggregator();
					Order order = aggregation.getOrder();
					if (aggregator != null) {
						// TODO: Approval attributes are not ideal for aggregator values
						attributesValid = attributesValid && attributes.setAttribute(Attribute.APP_ID, aggregator.getAggregatorID());
						attributesValid = attributesValid && attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
					}
					if (order != null) {
						// TODO: Task operation attributes are not ideal for aggregator values
						attributesValid = attributesValid && attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated());
						attributesValid = attributesValid && attributes.setAttribute(Attribute.OP_ID, order.getOrderReference());
					}
				}
	
		
				Compile compile = source.getCompile();
				if (compile != null) {
					//TODO: Asset Parent ID to be added by Mayam shortly
					//attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_PARENT_ID, compile.getParentMaterialID());
				}
				
				Library library = source.getLibrary();
				if (library != null) {
					List<TapeType> tapeList = library.getTape();
					if (tapeList != null) {
						IdSet tapeIds = new IdSet();
						for (int i = 0; i < tapeList.size(); i++) {
							TapeType tape = tapeList.get(i);
							if (tape != null) {
								// TODO: Should we store the tape library ids, the presentation (package) id, or both?
								// If both are required then we will need a new way of storing the Attribute values
								tape.getLibraryID();
								tapeIds.add(tape.getPresentationID());
							}
						}
						attributesValid = attributesValid && attributes.setAttribute(Attribute.SOURCE_IDS, tapeIds);
					}
				}
			}
			
			if (!attributesValid) {
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}
			
			AttributeMap result;
			try {
				result = client.createAsset(attributes.getAttributes());
				if (result == null) {
					returnCode = MayamClientErrorCode.MATERIAL_CREATION_FAILED;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
		}
		else {
			return MayamClientErrorCode.MATERIAL_UNAVAILABLE;
		}
		return returnCode;
	}
	
	public MayamClientErrorCode createMaterial(MarketingMaterialType material) {
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		
		if (material != null) 
		{
			//TODO: What all data can we get from this 'Object'
			//Object additionalMaterial = material.getAdditionalMarketingDetail();
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASPECT_RATIO, material.getAspectRatio());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_DURATION, material.getDuration());
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_FLAG, material.isAdultMaterial());
			
			//TODO: Require attributes for timecode
			//attributesValid = attributesValid && attributes.setAttribute(Attribute., material.getFirstFrameTimecode());
			//attributesValid = attributesValid && attributes.setAttribute(Attribute., material.getLastFrameTimecode());
			
			//TODO: What should Media be set as?
			//MediaType media = material.getMedia();

			//TODO: List of audio data
/*			AudioTracks audioTracks = material.getAudioTracks();
			List<Track> tracks = audioTracks.getTrack();
			for (int i = 0; i < tracks.size(); i++) {
				Track track = tracks.get(i);
				track.getTrackEncoding().toString();
				track.getTrackName().toString();
				track.getTrackNumber();
			}*/
			
			if (!attributesValid) {
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}
			
			AttributeMap result;
			try {
				result = client.createAsset(attributes.getAttributes());
				if (result == null) {
					returnCode = MayamClientErrorCode.MATERIAL_CREATION_FAILED;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
		}
		else {
			return MayamClientErrorCode.MATERIAL_UNAVAILABLE;
		}
		return returnCode;
	}
	
	//Material - Updating a media asset in Mayam
	public MayamClientErrorCode updateMaterial(ProgrammeMaterialType material)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		
		if (material != null) {
			MayamAttributeController attributes = null;
			AttributeMap assetAttributes = null;
			
			try {
				assetAttributes = client.getAsset(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}

			if (assetAttributes != null) {
				attributes = new MayamAttributeController(assetAttributes);
				
				//TODO: Confirm aspect ratio is in the correct notation, otherwise have conversion method
				attributesValid = attributesValid && attributes.setAttribute(Attribute.ASPECT_RATIO, AspectRatio.valueOf(material.getAspectRatio()));
				
				attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_DURATION, material.getDuration());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
				
				//TODO: No appropriate attributes for start and end frame markers
				//material.getLastFrameTimecode();
				//material.getFirstFrameTimecode();
				
				
				//TODO: How to handle multiple audio tracks and segments?
/*				SegmentationType segmentation = material.getOriginalConform();
				List<Segment> segments = segmentation.getSegment();
				for (int i = 0; i < segments.size(); i++) {
					Segment segment = segments.get(i);
					segment.getDuration();
					segment.getEOM();
					segment.getSegmentNumber();
					segment.getSegmentTitle();
					segment.getSOM();
				}
				
				AudioTracks audioTracks = material.getAudioTracks();
				List<Track> tracks = audioTracks.getTrack();
				for (int i = 0; i < tracks.size(); i++) {
					Track track = tracks.get(i);
					track.getTrackEncoding().toString();
					track.getTrackName().toString();
					track.getTrackNumber();
				}*/
				
				if (!attributesValid) {
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}
				
				AttributeMap result;
				try {
					result = client.updateAsset(attributes.getAttributes());
					if (result == null) {
						returnCode = MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else {
				returnCode = MayamClientErrorCode.MATERIAL_FIND_FAILED;
			}
		}
		else {
			returnCode = MayamClientErrorCode.MATERIAL_UNAVAILABLE;	
		}
		return returnCode;
	}
	
	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		if (material != null) {
			AttributeMap assetAttributes = null;
			MayamAttributeController attributes = null;
			
			try {
				assetAttributes = client.getAsset(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}

			if (assetAttributes != null) {
				attributes = new MayamAttributeController(assetAttributes);
				
				attributesValid = attributesValid && attributes.setAttribute(Attribute.QC_NOTES, material.getQualityCheckTask().toString());
				if (material.getRequiredBy() != null && material.getRequiredBy().toGregorianCalendar() != null) {
					attributesValid = attributesValid && attributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy().toGregorianCalendar().getTime());
				}
				attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_FMT, material.getRequiredFormat());
				
				Source source = material.getSource();
				if (source != null) {
					Aggregation aggregation = source.getAggregation();
					if (aggregation != null) {
						Aggregator aggregator = aggregation.getAggregator();
						Order order = aggregation.getOrder();
						
						if (aggregator != null) {
							// TODO: Approval attributes are not ideal for aggregator values
							attributesValid = attributesValid && attributes.setAttribute(Attribute.APP_VAL, aggregator.getAggregatorID());
							attributesValid = attributesValid && attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
						}
						if (order != null) {
							// TODO: Task operation attributes are not ideal for aggregator values
							attributesValid = attributesValid && attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated().toGregorianCalendar().getTime());
							attributesValid = attributesValid && attributes.setAttribute(Attribute.OP_VAL, order.getOrderReference());
						}
					}

					Compile compile = source.getCompile();
					if (compile != null) {
						//TODO: Asset Parent ID to be added by Mayam shortly
						//attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_PARENT_ID, compile.getParentMaterialID());
					}
					
					Library library = source.getLibrary();
					if (library != null) {
						List<TapeType> tapeList = library.getTape();
						if (tapeList != null) {
							IdSet tapeIds = new IdSet();
							for (int i = 0; i < tapeList.size(); i++) {
								TapeType tape = tapeList.get(i);
								if (tape != null) {
									// TODO: Should we store the tape library ids, the presentation (package) id, or both?
									// If both are required then we will need a new way of storing the Attribute values
									tape.getLibraryID();
									tapeIds.add(tape.getPresentationID());
								}
							}
							attributesValid = attributesValid && attributes.setAttribute(Attribute.SOURCE_IDS, tapeIds);
						}
					}
				}
				
				if (!attributesValid) {
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}
				
				AttributeMap result;
				try {
					result = client.updateAsset(attributes.getAttributes());
					if (result == null) {
						returnCode = MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else {
				returnCode = MayamClientErrorCode.MATERIAL_FIND_FAILED;
			}
		}
		else {
			returnCode = MayamClientErrorCode.MATERIAL_UNAVAILABLE;	
		}
		return returnCode;
	}

	public boolean materialExists(String materialID) {
		boolean materialFound = false;
		try {
			AttributeMap assetAttributes = client.getAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
			if (assetAttributes != null) {
				materialFound = true;
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return materialFound;
	}
	
	public AttributeMap getMaterial(String materialID) {
		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.getAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
		} catch (RemoteException e1) {
			//TODO: Error Handling
			e1.printStackTrace();
		}
		return assetAttributes;
	}

	public MayamClientErrorCode deleteMaterial(String materialID) {
		// TODO Find out how to delete assets in Mayam
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
}
