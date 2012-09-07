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

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.IdSet;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MediaExchange.Programme;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MqClient;


public class MayamMaterialController {
	private final TasksClient client;
	private final MqClient mq;
	
	public MayamMaterialController(TasksClient mayamClient, MqClient mqClient) {
		client = mayamClient;
		mq = mqClient;
	}
	
	public MayamClientErrorCode createMaterial(Programme.Media media)
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode createMaterial(MaterialType material)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		final AttributeMap assetAttributes = client.createAttributeMap();
			
		assetAttributes.setAttribute(Attribute.ASSET_TYPE, AssetType.ITEM);
		assetAttributes.setAttribute(Attribute.ASSET_ID, material.getMaterialD());
		assetAttributes.setAttribute(Attribute.QC_NOTES, material.getQualityCheckTask().toString());
		assetAttributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy());
		assetAttributes.setAttribute(Attribute.CONT_FMT, material.getRequiredFormat());
		
		Source source = material.getSource();
		if (source != null) {
			Aggregation aggregation = source.getAggregation();
			if (aggregation != null) {
				Aggregator aggregator = aggregation.getAggregator();
				Order order = aggregation.getOrder();
				if (aggregator != null) {
					// TODO: Approval attributes are not ideal for aggregator values
					assetAttributes.setAttribute(Attribute.APP_ID, aggregator.getAggregatorID());
					assetAttributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
				}
				if (order != null) {
					// TODO: Task operation attributes are not ideal for aggregator values
					assetAttributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated());
					assetAttributes.setAttribute(Attribute.OP_ID, order.getOrderReference());
				}
			}
		}
	
		Compile compile = source.getCompile();
		if (compile != null) {
			//TODO: Asset Parent ID to be added by Mayam shortly
			//assetAttributes.setAttribute(Attribute.ASSET_PARENT_ID, compile.getParentMaterialID());
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
				assetAttributes.setAttribute(Attribute.SOURCE_IDS, tapeIds);
			}
		}
		
		AttributeMap result;
		try {
			result = client.createAsset(assetAttributes);
			if (result != null) {
				returnCode = MayamClientErrorCode.MATERIAL_CREATION_FAILED;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			e.printRemoteMessages(System.err);
			returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
		}
		return returnCode;
	}
		
	//Material - Updating a media asset in Mayam
	public MayamClientErrorCode updateMaterial(Programme.Media media)
	{
		AttributeMap asset;
		try {
			asset = client.getAsset(AssetType.ITEM, media.getId());
			asset.setAttribute(Attribute.ASSET_TITLE, media.getFilename());
			
			client.updateAsset(asset);

		} catch (RemoteException e) {
			e.printStackTrace();
			e.printRemoteMessages(System.err);
		}
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (material != null) {
			AttributeMap assetAttributes = null;
			
			try {
				assetAttributes = client.getAsset(AssetType.ITEM, material.getMaterialD());
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}

			if (assetAttributes != null) {
				assetAttributes.setAttribute(Attribute.QC_NOTES, material.getQualityCheckTask().toString());
				assetAttributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy());
				assetAttributes.setAttribute(Attribute.CONT_FMT, material.getRequiredFormat());
				
				Source source = material.getSource();
				if (source != null) {
					Aggregation aggregation = source.getAggregation();
					if (aggregation != null) {
						Aggregator aggregator = aggregation.getAggregator();
						Order order = aggregation.getOrder();
						if (aggregator != null) {
							// TODO: Approval attributes are not ideal for aggregator values
							assetAttributes.setAttribute(Attribute.APP_ID, aggregator.getAggregatorID());
							assetAttributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
						}
						if (order != null) {
							// TODO: Task operation attributes are not ideal for aggregator values
							assetAttributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated());
							assetAttributes.setAttribute(Attribute.OP_ID, order.getOrderReference());
						}
					}
				}
			
				Compile compile = source.getCompile();
				if (compile != null) {
					//TODO: Asset Parent ID to be added by Mayam shortly
					//assetAttributes.setAttribute(Attribute.ASSET_PARENT_ID, compile.getParentMaterialID());
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
						assetAttributes.setAttribute(Attribute.SOURCE_IDS, tapeIds);
					}
				}
				
				AttributeMap result;
				try {
					result = client.updateAsset(assetAttributes);
					if (result != null) {
						returnCode = MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					e.printRemoteMessages(System.err);
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
		// TODO implement materialExists
		return true;
	}
}
