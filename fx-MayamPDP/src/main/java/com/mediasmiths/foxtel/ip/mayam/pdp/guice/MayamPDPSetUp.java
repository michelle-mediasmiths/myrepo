package com.mediasmiths.foxtel.ip.mayam.pdp.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mediasmiths.foxtel.ip.mayam.pdp.FoxtelGroups;
import com.mediasmiths.foxtel.ip.mayam.pdp.MayamPDP;
import com.mediasmiths.foxtel.ip.mayam.pdp.MayamPDPImpl;
import com.mediasmiths.foxtel.ip.mayam.pdp.PrivilegedOperations;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MayamPDPSetUp extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(MayamPDP.class).to(MayamPDPImpl.class);
		RestResourceRegistry.register(MayamPDP.class);
	}


	@Provides
	@Named("foxtel.groups.nonao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> provideSecurityGroupsForNonAOOperations(Injector injector)
	{
		Map<PrivilegedOperations, Set<FoxtelGroups>> securityGroups = new HashMap<PrivilegedOperations, Set<FoxtelGroups>>();

		final Set<FoxtelGroups> adminGroups = getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                               Names.named("groups.admin.nonao"))),
		                                                  Collections.<FoxtelGroups>emptySet());

		securityGroups.put(PrivilegedOperations.PROTECT, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                          Names.named("groups.protect.nonao"))),
		                                                             adminGroups));
		securityGroups.put(PrivilegedOperations.UNPROTECT, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                            Names.named("groups.unprotect.nonao"))),
		                                                               adminGroups));
		securityGroups.put(PrivilegedOperations.DELETE, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.delete.nonao"))),
		                                                            adminGroups));
		securityGroups.put(PrivilegedOperations.AUTOQC, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.autoQC.nonao"))),
		                                                            adminGroups));
		securityGroups.put(PrivilegedOperations.QCPARALLEL, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                             Names.named("groups.qcParallel.nonao"))),
		                                                                adminGroups));
		securityGroups.put(PrivilegedOperations.INGEST, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.ingest.nonao"))),
		                                                            adminGroups));
		securityGroups.put(PrivilegedOperations.UNINGEST, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                           Names.named("groups.uningest.nonao"))),
		                                                              adminGroups));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_LOGGING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.complianceLogging.nonao"))),
		                                                                        adminGroups));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_EDITING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.complianceEditing.nonao"))),
		                                                                        adminGroups));
		securityGroups.put(PrivilegedOperations.MATCHING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                           Names.named("groups.matching.nonao"))),
		                                                              adminGroups));
		securityGroups.put(PrivilegedOperations.PREVIEW, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                          Names.named("groups.preview.nonao"))),
		                                                             adminGroups));
		securityGroups.put(PrivilegedOperations.TX_DELIVERY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                              Names.named("groups.txDelivery.nonao"))),
		                                                                 adminGroups));
		securityGroups.put(PrivilegedOperations.EXPORT_MARKERS, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                 Names.named("groups.exportMarker.nonao"))),
		                                                                    adminGroups));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                   Names.named("groups.complianceProxy.nonao"))),
		                                                                      adminGroups));
		securityGroups.put(PrivilegedOperations.CAPTIONS_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                 Names.named("groups.captionsProxy.nonao"))),
		                                                                    adminGroups));
		securityGroups.put(PrivilegedOperations.PUBLICITY_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                  Names.named("groups.publicityProxy.nonao"))),
		                                                                     adminGroups));
		securityGroups.put(PrivilegedOperations.FILEVERIFYOVERRIDE, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.files.verifyOverride.nonao"))),
		                                                                        adminGroups));

		securityGroups.put(PrivilegedOperations.SEGMENT_MISMATCH_OVERRIDE, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                            Names.named("groups.segments.override.nonao"))),
		                                                                               adminGroups));

		return securityGroups;
	}


	@Provides
	@Named("foxtel.groups.ao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> provideSecurityGroupsForAOOperations(Injector injector)
	{
		Map<PrivilegedOperations, Set<FoxtelGroups>> securityGroups = new HashMap<PrivilegedOperations, Set<FoxtelGroups>>();

		final Set<FoxtelGroups> adminGroups = getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                               Names.named("groups.admin.ao"))),
		                                                  Collections.<FoxtelGroups>emptySet());

		securityGroups.put(PrivilegedOperations.PROTECT, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                          Names.named("groups.protect.ao"))),
		                                                             adminGroups));
		securityGroups.put(PrivilegedOperations.UNPROTECT, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                            Names.named("groups.unprotect.ao"))),
		                                                               adminGroups));
		securityGroups.put(PrivilegedOperations.DELETE, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.delete.ao"))),
		                                                            adminGroups));
		securityGroups.put(PrivilegedOperations.AUTOQC, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.autoQC.ao"))),
		                                                            adminGroups));
		securityGroups.put(PrivilegedOperations.QCPARALLEL, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                             Names.named("groups.qcParallel.ao"))),
		                                                                adminGroups));
		securityGroups.put(PrivilegedOperations.INGEST, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                         Names.named("groups.ingest.ao"))),
		                                                            adminGroups));
		securityGroups.put(PrivilegedOperations.UNINGEST, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                           Names.named("groups.uningest.ao"))),
		                                                              adminGroups));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_LOGGING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.complianceLogging.ao"))),
		                                                                        adminGroups));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_EDITING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.complianceEditing.ao"))),
		                                                                        adminGroups));
		securityGroups.put(PrivilegedOperations.MATCHING, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                           Names.named("groups.matching.ao"))),
		                                                              adminGroups));
		securityGroups.put(PrivilegedOperations.PREVIEW, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                          Names.named("groups.preview.ao"))),
		                                                             adminGroups));
		securityGroups.put(PrivilegedOperations.TX_DELIVERY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                              Names.named("groups.txDelivery.ao"))),
		                                                                 adminGroups));
		securityGroups.put(PrivilegedOperations.EXPORT_MARKERS, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                 Names.named("groups.exportMarker.ao"))),
		                                                                    adminGroups));
		securityGroups.put(PrivilegedOperations.COMPLIANCE_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                   Names.named("groups.complianceProxy.ao"))),
		                                                                      adminGroups));
		securityGroups.put(PrivilegedOperations.CAPTIONS_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                 Names.named("groups.captionsProxy.ao"))),
		                                                                    adminGroups));
		securityGroups.put(PrivilegedOperations.PUBLICITY_PROXY, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                  Names.named("groups.publicityProxy.ao"))),
		                                                                     adminGroups));

		securityGroups.put(PrivilegedOperations.FILEVERIFYOVERRIDE, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                     Names.named("groups.files.verifyOverride.ao"))),
		                                                                        adminGroups));
		securityGroups.put(PrivilegedOperations.SEGMENT_MISMATCH_OVERRIDE, getGroupSet(injector.getInstance(Key.get(String.class,
		                                                                                                            Names.named("groups.segments.override.ao"))),
		                                                                               adminGroups));

		return securityGroups;
	}


	private Set<FoxtelGroups> getGroupSet(final String csvGroupStr, final Set<FoxtelGroups> adminGroups)
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

		grps.addAll(adminGroups);

		grps.add(FoxtelGroups.admin);
		grps.add(FoxtelGroups.ardendo);

		return grps;
	}


	@Provides
	public JAXBSerialiser jaxbSerialiser()
	{
		return JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events");
	}
}
