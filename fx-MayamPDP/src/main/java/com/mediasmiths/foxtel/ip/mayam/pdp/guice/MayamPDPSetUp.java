package com.mediasmiths.foxtel.ip.mayam.pdp.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mediasmiths.foxtel.ip.mayam.pdp.FoxtelGroups;
import com.mediasmiths.foxtel.ip.mayam.pdp.MayamPDP;
import com.mediasmiths.foxtel.ip.mayam.pdp.PrivilegedOperations;
import com.mediasmiths.foxtel.ip.mayam.pdp.Stub;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MayamPDPSetUp extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(MayamPDP.class).to(Stub.class);
		RestResourceRegistry.register(MayamPDP.class);
	}

	@Provides
	@Named("foxtel.groups.nonao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> provideSecurityGroupsForNonAOOperations(Injector injector)
	{
		Map<PrivilegedOperations, Set<FoxtelGroups>> securityGroups = new HashMap<PrivilegedOperations, Set<FoxtelGroups>>();

		securityGroups.put(PrivilegedOperations.PROTECT, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                          Names.named("groups.protect.nonao")))));
		securityGroups.put(PrivilegedOperations.UNPROTECT, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                            Names.named("groups.unprotect.nonao")))));
		securityGroups.put(PrivilegedOperations.DELETE, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.delete.nonao")))));
		securityGroups.put(PrivilegedOperations.AUTOQC, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.autoQC.nonao")))));
		securityGroups.put(PrivilegedOperations.INGEST, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.ingest.nonao")))));
		securityGroups.put(PrivilegedOperations.UNINGEST, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                           Names.named("groups.uningest.nonao")))));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_LOGGING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.complianceLogging.nonao")))));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_EDITING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.complianceEditing.nonao")))));
		securityGroups.put(PrivilegedOperations.MATCHING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                           Names.named("groups.matching.nonao")))));
		securityGroups.put(PrivilegedOperations.PREVIEW, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                          Names.named("groups.preview.nonao")))));
		securityGroups.put(PrivilegedOperations.TX_DELIVERY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                              Names.named("groups.txDelivery.nonao")))));
		securityGroups.put(PrivilegedOperations.EXPORT_MARKERS, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                 Names.named("groups.exportMarker.nonao")))));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                   Names.named("groups.complianceProxy.nonao")))));
		securityGroups.put(PrivilegedOperations.CAPTIONS_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                 Names.named("groups.captionsProxy.nonao")))));
		securityGroups.put(PrivilegedOperations.PUBLICITY_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                  Names.named("groups.publicityProxy.nonao")))));
		return securityGroups;

	}


	@Provides
	@Named("foxtel.groups.ao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> provideSecurityGroupsForAOOperations(Injector injector)
	{
		Map<PrivilegedOperations, Set<FoxtelGroups>> securityGroups = new HashMap<PrivilegedOperations, Set<FoxtelGroups>>();

		securityGroups.put(PrivilegedOperations.PROTECT, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                          Names.named("groups.protect.ao")))));
		securityGroups.put(PrivilegedOperations.UNPROTECT, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                            Names.named("groups.unprotect.ao")))));
		securityGroups.put(PrivilegedOperations.DELETE, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.delete.ao")))));
		securityGroups.put(PrivilegedOperations.AUTOQC, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.autoQC.ao")))));
		securityGroups.put(PrivilegedOperations.INGEST, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.ingest.ao")))));
		securityGroups.put(PrivilegedOperations.UNINGEST, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                           Names.named("groups.uningest.ao")))));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_LOGGING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.complianceLogging.ao")))));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_EDITING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.complianceEditing.ao")))));
		securityGroups.put(PrivilegedOperations.MATCHING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                           Names.named("groups.matching.ao")))));
		securityGroups.put(PrivilegedOperations.PREVIEW, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                          Names.named("groups.preview.ao")))));
		securityGroups.put(PrivilegedOperations.TX_DELIVERY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                              Names.named("groups.txDelivery.ao")))));
		securityGroups.put(PrivilegedOperations.EXPORT_MARKERS, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                 Names.named("groups.exportMarker.ao")))));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                   Names.named("groups.complianceProxy.ao")))));
		securityGroups.put(PrivilegedOperations.CAPTIONS_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                 Names.named("groups.captionsProxy.ao")))));
		securityGroups.put(PrivilegedOperations.PUBLICITY_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                  Names.named("groups.publicityProxy.ao")))));
		return securityGroups;

	}

	private Set<FoxtelGroups> getGroupSet(final String csvGroupStr)
	{
		Set<FoxtelGroups> grps = new HashSet<FoxtelGroups>();

		if (csvGroupStr == null)
			throw new IllegalArgumentException("CSV String cannot be null");

		String[] elements = csvGroupStr.split(", *");

		for (String el : elements)
		{
			FoxtelGroups grp = FoxtelGroups.fromString(el);

			grps.add(grp);
		}

		grps.add(FoxtelGroups.GC_MAM_FULL_SysAdmin);

		return grps;
	}


}
