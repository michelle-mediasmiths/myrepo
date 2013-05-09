package com.mediasmiths.mayam.veneer;

import java.net.URL;

import javax.inject.Provider;

import com.google.inject.Inject;
import com.mayam.wf.attributes.server.AttributeMapMapper;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.AttributeMultiMap;
import com.mayam.wf.attributes.shared.AttributeRangeMap;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.AssetApi;
import com.mayam.wf.ws.client.SegmentApi;
import com.mayam.wf.ws.client.TaskApi;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.UserApi;

public class TasksClientVeneerImpl implements TasksClientVeneer
{
	
	@Inject
	protected TasksClient tasksClient;
	@Inject
	protected AssetApiVeneer assetApiVeneer;
	@Inject
	protected TaskApiVeneer taskApiVeneer;
	@Inject
	protected SegmentApiVeneer segmentApiVeneer;
	@Inject
	protected UserApiVeneer userApiVeneer;

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return tasksClient.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#injectHelpers(com.mayam.wf.attributes.server.AttributeMapMapper, javax.inject.Provider, javax.inject.Provider, javax.inject.Provider)
	 */
	@Override
	public void injectHelpers(
			AttributeMapMapper mapper,
			Provider<AttributeMap> mapProvider,
			Provider<AttributeRangeMap> rangeMapProvider,
			Provider<AttributeMultiMap> multiMapProvider)
	{
		tasksClient.injectHelpers(mapper, mapProvider, rangeMapProvider, multiMapProvider);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#setup(java.net.URL, java.lang.String)
	 */
	@Override
	public final TasksClient setup(URL baseUrl, String apiToken)
	{
		return tasksClient.setup(baseUrl, apiToken);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return tasksClient.equals(obj);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#segmentApi()
	 */
	@Override
	public SegmentApiVeneer segmentApi()
	{
		return segmentApiVeneer;
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#taskApi()
	 */
	@Override
	public TaskApiVeneer taskApi()
	{
		return taskApiVeneer;
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#assetApi()
	 */
	@Override
	public AssetApiVeneer assetApi()
	{
		return assetApiVeneer;
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#userApi()
	 */
	@Override
	public UserApiVeneer userApi()
	{
		return userApiVeneer;
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#createAttributeMap()
	 */
	@Override
	public AttributeMap createAttributeMap()
	{
		return tasksClient.createAttributeMap();
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#deserializAttributeMap(java.lang.String)
	 */
	@Override
	public AttributeMap deserializAttributeMap(String serialized)
	{
		return tasksClient.deserializAttributeMap(serialized);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#testAlwaysReturnTask()
	 */
	@Override
	public AttributeMap testAlwaysReturnTask()
	{
		return tasksClient.testAlwaysReturnTask();
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#testAlwaysThrowException()
	 */
	@Override
	public void testAlwaysThrowException()
	{
		tasksClient.testAlwaysThrowException();
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#testPutMap(com.mayam.wf.attributes.shared.AttributeMap)
	 */
	@Override
	public AttributeMap testPutMap(AttributeMap map) throws RemoteException
	{
		return tasksClient.testPutMap(map);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.veneer.TasksClientVeneer#toString()
	 */
	@Override
	public String toString()
	{
		return tasksClient.toString();
	}
	
}
