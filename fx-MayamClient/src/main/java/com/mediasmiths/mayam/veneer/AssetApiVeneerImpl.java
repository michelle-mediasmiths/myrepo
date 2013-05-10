package com.mediasmiths.mayam.veneer;

import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;
import com.mayam.wf.attributes.shared.type.MarkerList;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.AssetApi;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.retrying.TasksWSRetryable;

public class AssetApiVeneerImpl implements AssetApiVeneer
{
	protected final AssetApi assetApi;

	@Inject
	public AssetApiVeneerImpl(@Named(MayamClientModule.SETUP_TASKS_CLIENT) TasksClient tasksClient)
	{
		this.assetApi = tasksClient.assetApi();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#createAsset(com.mayam.wf.attributes.shared.AttributeMap)
	 */
	@Override
	@TasksWSRetryable
	public AttributeMap createAsset(AttributeMap asset) throws RemoteException
	{
		return assetApi.createAsset(asset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#getAsset(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public AttributeMap getAsset(AssetType assetType, String assetId) throws RemoteException
	{
		return assetApi.getAsset(assetType, assetId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return assetApi.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#getAssetBySiteId(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public AttributeMap getAssetBySiteId(AssetType assetType, String siteId) throws RemoteException
	{
		return assetApi.getAssetBySiteId(assetType, siteId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#getAssetChildren(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String, com.mayam.wf.attributes.shared.type.AssetType)
	 */
	@Override
	@TasksWSRetryable
	public List<AttributeMap> getAssetChildren(AssetType assetType, String assetId, AssetType... childTypes)
			throws RemoteException
	{
		return assetApi.getAssetChildren(assetType, assetId, childTypes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#updateAsset(com.mayam.wf.attributes.shared.AttributeMap)
	 */
	@Override
	@TasksWSRetryable
	public AttributeMap updateAsset(AttributeMap asset) throws RemoteException
	{
		return assetApi.updateAsset(asset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#deleteAsset(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public void deleteAsset(AssetType assetType, String assetId) throws RemoteException
	{
		assetApi.deleteAsset(assetType, assetId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#deleteAsset(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String, int)
	 */
	@Override
	@TasksWSRetryable
	public void deleteAsset(AssetType assetType, String assetId, int gracePeriod) throws RemoteException
	{
		assetApi.deleteAsset(assetType, assetId, gracePeriod);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return assetApi.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#importFile(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public String importFile(AssetType assetType, String assetId, String storage, String filepath) throws RemoteException
	{
		return assetApi.importFile(assetType, assetId, storage, filepath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#moveMediaEssence(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String, com.mayam.wf.attributes.shared.type.AssetType,
	 * java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public void moveMediaEssence(AssetType srcType, String srcId, AssetType destType, String destId) throws RemoteException
	{
		assetApi.moveMediaEssence(srcType, srcId, destType, destId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#deleteAssetMedia(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public void deleteAssetMedia(AssetType assetType, String assetId) throws RemoteException
	{
		assetApi.deleteAssetMedia(assetType, assetId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#getMarkers(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public MarkerList getMarkers(AssetType assetType, String assetId, String revisionId) throws RemoteException
	{
		return assetApi.getMarkers(assetType, assetId, revisionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#getFormatInfo(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public FileFormatInfo getFormatInfo(AssetType assetType, String assetId) throws RemoteException
	{
		return assetApi.getFormatInfo(assetType, assetId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#requestHighresXfer(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public String requestHighresXfer(AssetType assetType, String assetId, String destination) throws RemoteException
	{
		return assetApi.requestHighresXfer(assetType, assetId, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#getTechReport(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public Map<String, Integer> getTechReport(AssetType assetType, String assetId) throws RemoteException
	{
		return assetApi.getTechReport(assetType, assetId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#getQcMessages(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public List<String> getQcMessages(AssetType assetType, String assetId) throws RemoteException
	{
		return assetApi.getQcMessages(assetType, assetId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.AssetApiVeneer#toString()
	 */
	@Override
	public String toString()
	{
		return assetApi.toString();
	}

}
