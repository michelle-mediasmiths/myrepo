package com.mediasmiths.mayam.veneer;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.UserApi;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.retrying.TasksWSRetryable;

public class UserApiVeneerImpl implements UserApiVeneer
{
	protected final UserApi userApi;

	@Inject
	public UserApiVeneerImpl(@Named(MayamClientModule.SETUP_TASKS_CLIENT) TasksClient tasksClient)
	{
		this.userApi = tasksClient.userApi();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.UserApiVeneer#getUserGroups(java.lang.String)
	 */
	@Override
	@TasksWSRetryable
	public Set<String> getUserGroups(String username) throws RemoteException
	{
		return userApi.getUserGroups(username);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.UserApiVeneer#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return userApi.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.UserApiVeneer#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return userApi.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.UserApiVeneer#toString()
	 */
	@Override
	public String toString()
	{
		return userApi.toString();
	}
}
