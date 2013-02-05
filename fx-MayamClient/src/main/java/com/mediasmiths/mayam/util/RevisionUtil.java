package com.mediasmiths.mayam.util;

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
		List<AttributeMap> maps = client.assetApi().getAssetChildren(AssetType.ITEM, itemId, AssetType.REVISION);
		int maxno = -1;
		String retId = null;
		for (AttributeMap map : maps)
		{
			Integer no = map.getAttribute(Attribute.REVISION_NUMBER);
			if (no > maxno)
			{
				retId = map.getAttribute(Attribute.ASSET_ID);
				maxno = no;
			}
		}
		return retId;
	}
}
