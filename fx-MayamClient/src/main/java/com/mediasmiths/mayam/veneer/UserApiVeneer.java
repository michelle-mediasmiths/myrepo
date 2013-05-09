package com.mediasmiths.mayam.veneer;

import java.util.Set;

import com.mayam.wf.exception.RemoteException;

public interface UserApiVeneer
{

	public abstract Set<String> getUserGroups(String username) throws RemoteException;

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

	public abstract String toString();

}