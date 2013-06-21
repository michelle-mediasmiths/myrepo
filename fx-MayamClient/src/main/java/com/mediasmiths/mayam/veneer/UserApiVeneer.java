package com.mediasmiths.mayam.veneer;

import com.google.inject.ImplementedBy;
import com.mayam.wf.exception.RemoteException;

import java.util.Set;

@ImplementedBy(UserApiVeneerImpl.class)
public interface UserApiVeneer
{
	public abstract Set<String> getUserGroups(String username) throws RemoteException;

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

	public abstract String toString();

}
