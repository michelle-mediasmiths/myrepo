package com.mediasmiths.mayam.util;

import java.util.ArrayList;
import java.util.List;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.TasksClient;

public class RevisionUtil
{
	public static String findHighestRevision(String itemId, TasksClient client) throws RemoteException
	{
		List<AttributeMap> maps = getAllRevisionsForItem(itemId, client);
		return findHighestRevisionID(maps);
	}

	public static List<AttributeMap> getAllRevisionsForItem(String itemID, TasksClient client) throws RemoteException
	{
		return client.assetApi().getAssetChildren(AssetType.ITEM, itemID, AssetType.REVISION);
	}

	public static String findHighestRevisionID(List<AttributeMap> allRevisions)
	{
		AttributeMap map = findHighestRevision(allRevisions);
		String id = map.getAttribute(Attribute.ASSET_ID);
		return id;
	}

	public static int findHighestRevisionNumber(List<AttributeMap> allRevisions)
	{
		AttributeMap map = findHighestRevision(allRevisions);
		Integer no = map.getAttribute(Attribute.REVISION_NUMBER);
		return no.intValue();
	}
	
	public static AttributeMap findHighestRevision(List<AttributeMap> allRevisions){
		int maxno = -1;
		AttributeMap ret = null;
		for (AttributeMap map : allRevisions)
		{
			Integer no = map.getAttribute(Attribute.REVISION_NUMBER);
			if (no > maxno)
			{
				ret = map;
				maxno = no;
			}
		}
		return ret;
	}

	public static List<AttributeMap> findAllButHighestRevision(String itemId, TasksClient client) throws RemoteException
	{

		List<AttributeMap> allRevisions = getAllRevisionsForItem(itemId, client);
		return findAllButHighestRevision(allRevisions);
	}

	public static List<AttributeMap> findAllButHighestRevision(List<AttributeMap> allRevisions)
	{
		int highestRevision = findHighestRevisionNumber(allRevisions);

		List<AttributeMap> ret = new ArrayList<AttributeMap>();

		for (AttributeMap map : allRevisions)
		{
			Integer no = map.getAttribute(Attribute.REVISION_NUMBER);
			if (!no.equals(Integer.valueOf(highestRevision)))
			{
				ret.add(map);
			}
		}

		return ret;
	}
}
