package com.mediasmiths.mayam.veneer;

import java.util.List;
import java.util.Map;

import com.google.inject.ImplementedBy;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;
import com.mayam.wf.attributes.shared.type.MarkerList;
import com.mayam.wf.exception.RemoteException;

@ImplementedBy(AssetApiVeneerImpl.class)
public interface AssetApiVeneer
{

	public abstract AttributeMap createAsset(AttributeMap asset) throws RemoteException;

	public abstract AttributeMap getAsset(AssetType assetType, String assetId) throws RemoteException;

	public abstract int hashCode();

	public abstract AttributeMap getAssetBySiteId(AssetType assetType, String siteId) throws RemoteException;

	public abstract List<AttributeMap> getAssetChildren(AssetType assetType, String assetId, AssetType... childTypes)
			throws RemoteException;

	public abstract AttributeMap updateAsset(AttributeMap asset) throws RemoteException;

	public abstract void deleteAsset(AssetType assetType, String assetId) throws RemoteException;

	public abstract void deleteAsset(AssetType assetType, String assetId, int gracePeriod) throws RemoteException;

	public abstract boolean equals(Object obj);

	public abstract String importFile(AssetType assetType, String assetId, String storage, String filepath)
			throws RemoteException;

	public abstract void moveMediaEssence(AssetType srcType, String srcId, AssetType destType, String destId)
			throws RemoteException;

	public abstract void deleteAssetMedia(AssetType assetType, String assetId) throws RemoteException;

	public abstract MarkerList getMarkers(AssetType assetType, String assetId, String revisionId) throws RemoteException;

	public abstract FileFormatInfo getFormatInfo(AssetType assetType, String assetId) throws RemoteException;

	public abstract String requestHighresXfer(AssetType assetType, String assetId, String destination) throws RemoteException;

	public abstract Map<String, Integer> getTechReport(AssetType assetType, String assetId) throws RemoteException;

	public abstract List<String> getQcMessages(AssetType assetType, String assetId) throws RemoteException;

	public abstract String toString();

}