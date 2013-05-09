package com.mediasmiths.mayam.veneer;

import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.exception.RemoteException;

public interface SegmentApiVeneer
{

	public abstract SegmentListList getSegmentListsForAsset(AssetType assetType, String assetId) throws RemoteException;

	public abstract SegmentList getSegmentList(String segmentListId) throws RemoteException;

	public abstract SegmentList getSegmentListBySiteId(String siteId) throws RemoteException;

	public abstract int hashCode();

	public abstract SegmentList createSegmentList(AssetType assetType, String assetId, SegmentList segmentList)
			throws RemoteException;

	public abstract void deleteSegmentList(String segmentListId) throws RemoteException;

	public abstract SegmentList updateSegmentList(String segmentListId, SegmentList segmentList) throws RemoteException;

	public abstract boolean equals(Object obj);

	public abstract String toString();

}