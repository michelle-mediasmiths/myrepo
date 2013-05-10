package com.mediasmiths.mayam.veneer;

import java.net.URL;

import javax.inject.Provider;

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

public interface TasksClientVeneer
{

	public abstract int hashCode();

	public abstract void injectHelpers(
			AttributeMapMapper mapper,
			Provider<AttributeMap> mapProvider,
			Provider<AttributeRangeMap> rangeMapProvider,
			Provider<AttributeMultiMap> multiMapProvider);

	public abstract TasksClient setup(URL baseUrl, String apiToken);

	public abstract boolean equals(Object obj);

	public abstract SegmentApiVeneer segmentApi();

	public abstract TaskApiVeneer taskApi();

	public abstract AssetApiVeneer assetApi();

	public abstract UserApiVeneer userApi();

	public abstract AttributeMap createAttributeMap();

	public abstract AttributeMap deserializAttributeMap(String serialized);

	public abstract AttributeMap testAlwaysReturnTask();

	public abstract void testAlwaysThrowException();

	public abstract AttributeMap testPutMap(AttributeMap map) throws RemoteException;

	public abstract String toString();

}