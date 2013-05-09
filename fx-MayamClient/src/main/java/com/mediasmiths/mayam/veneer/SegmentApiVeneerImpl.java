package com.mediasmiths.mayam.veneer;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.SegmentApi;
import com.mayam.wf.ws.client.TasksClient;

public class SegmentApiVeneerImpl implements SegmentApiVeneer
{
	protected final SegmentApi segmentApi;
	
	@Inject
	public SegmentApiVeneerImpl(TasksClient tasksClient){
		this.segmentApi=tasksClient.segmentApi();
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#getSegmentListsForAsset(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String)
	 */
	@Override
	public SegmentListList getSegmentListsForAsset(AssetType assetType, String assetId) throws RemoteException
	{
		return segmentApi.getSegmentListsForAsset(assetType, assetId);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#getSegmentList(java.lang.String)
	 */
	@Override
	public SegmentList getSegmentList(String segmentListId) throws RemoteException
	{
		return segmentApi.getSegmentList(segmentListId);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#getSegmentListBySiteId(java.lang.String)
	 */
	@Override
	public SegmentList getSegmentListBySiteId(String siteId) throws RemoteException
	{
		return segmentApi.getSegmentListBySiteId(siteId);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return segmentApi.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#createSegmentList(com.mayam.wf.attributes.shared.type.AssetType, java.lang.String, com.mayam.wf.attributes.shared.type.SegmentList)
	 */
	@Override
	public SegmentList createSegmentList(AssetType assetType, String assetId, SegmentList segmentList) throws RemoteException
	{
		return segmentApi.createSegmentList(assetType, assetId, segmentList);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#deleteSegmentList(java.lang.String)
	 */
	@Override
	public void deleteSegmentList(String segmentListId) throws RemoteException
	{
		segmentApi.deleteSegmentList(segmentListId);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#updateSegmentList(java.lang.String, com.mayam.wf.attributes.shared.type.SegmentList)
	 */
	@Override
	public SegmentList updateSegmentList(String segmentListId, SegmentList segmentList) throws RemoteException
	{
		return segmentApi.updateSegmentList(segmentListId, segmentList);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return segmentApi.equals(obj);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.SegmentApiVeneer#toString()
	 */
	@Override
	public String toString()
	{
		return segmentApi.toString();
	}
}
