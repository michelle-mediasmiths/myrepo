package com.mediasmiths.foxtel.tc.priorities.guice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mediasmiths.foxtel.tc.priorities.PriorityRule;
import com.mediasmiths.foxtel.tc.priorities.PriortyRuleAscThresholdComparator;
import com.mediasmiths.foxtel.tc.priorities.PriortyRuleDescThresholdComparator;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.ListUtility;

public class TranscodePrioritiesModule extends AbstractModule
{

	private final static Logger log = Logger.getLogger(TranscodePrioritiesModule.class);

	@Override
	protected void configure()
	{
		PropertyFile properties = PropertyFile.find("transcodepriorities.properties"); // would be nicer if this was configurable
		
		log.debug(properties.toString());
		
		Names.bindProperties(this.binder(), properties.toProperties());
	}

	@Provides
	@Named("tc.priorities.map.highest")
	@Singleton
	public Map<TranscodeJobType, Integer> highestPrioritiesMap(
			@Named("tc.priorities.map") Multimap<TranscodeJobType, Integer> allPriorities)
	{

		Map<TranscodeJobType, Integer> ret = new HashMap<TranscodeJobType, Integer>();

		Set<TranscodeJobType> keySet = allPriorities.keySet();

		for (TranscodeJobType transcodeJobType : keySet)
		{
			Collection<Integer> p = allPriorities.get(transcodeJobType);
			ret.put(transcodeJobType, Collections.max(p));
		}
		
		log.debug("highest tc priorities:");
		Set<Entry<TranscodeJobType, Integer>> entrySet = ret.entrySet();
		for (Entry<TranscodeJobType, Integer> entry : entrySet)
		{
			log.debug(entry.getKey() + " "+entry.getValue());
		}

		return ret;

	}

	@Provides
	@Named("tc.priorities.map.lowest")
	@Singleton
	public Map<TranscodeJobType, Integer> lowestPrioritiesMap(
			@Named("tc.priorities.map") Multimap<TranscodeJobType, Integer> allPriorities)
	{

		Map<TranscodeJobType, Integer> ret = new HashMap<TranscodeJobType, Integer>();

		Set<TranscodeJobType> keySet = allPriorities.keySet();

		for (TranscodeJobType transcodeJobType : keySet)
		{
			Collection<Integer> p = allPriorities.get(transcodeJobType);
			ret.put(transcodeJobType, Collections.min(p));
		}

		log.debug("lowest tc priorities:");
		Set<Entry<TranscodeJobType, Integer>> entrySet = ret.entrySet();
		for (Entry<TranscodeJobType, Integer> entry : entrySet)
		{
			log.debug(entry.getKey() + " "+entry.getValue());
		}

		
		return ret;

	}

	@Provides
	@Named("tc.priorities.map")
	@Singleton
	public Multimap<TranscodeJobType, Integer> allProritiesMap(
			@Named("tc.priorities.caption") List<Integer> captionPriorities,
			@Named("tc.priorities.publicity") List<Integer> publicitiyPriorities,
			@Named("tc.priorities.compliance") List<Integer> compliancePriorities,
			@Named("tc.priorities.tx") List<Integer> txPriorities)
	{

		Multimap<TranscodeJobType, Integer> ret = HashMultimap.create();

		ret.putAll(TranscodeJobType.CAPTION_PROXY, captionPriorities);
		ret.putAll(TranscodeJobType.COMPLIANCE_PROXY, compliancePriorities);
		ret.putAll(TranscodeJobType.PUBLICITY_PROXY, publicitiyPriorities);
		ret.putAll(TranscodeJobType.TX, txPriorities);

		return ret;
	}

	@Provides
	@Named("tc.priorities.caption")
	@Singleton
	public List<Integer> captionPrioritiesList(
			@Named("tc.caption.tx.soon.priority") Integer soon,
			@Named("tc.caption.tx.later.priority") Integer later,
			@Named("tc.caption.tx.eventually.priority") Integer eventually)
	{

		return ListUtility.list(new Integer[] { soon, later, eventually });

	}

	@Provides
	@Named("tc.priorities.publicity")
	@Singleton
	public List<Integer> publicitiyPrioritiesList(
			@Named("tc.publicity.long.queued.priority") Integer lng,
			@Named("tc.publicity.medium.queued.priority") Integer med,
			@Named("tc.publicity.recently.queued.priority") Integer recent)
	{
		return ListUtility.list(new Integer[] { lng, med, recent });
	}

	@Provides
	@Named("tc.priorities.compliance")
	@Singleton
	public List<Integer> compliancePrioritiesList(
			@Named("tc.compliance.long.queued.priority") Integer lng,
			@Named("tc.compliance.medium.queued.priority") Integer med,
			@Named("tc.compliance.recently.queued.priority") Integer recent)
	{
		return ListUtility.list(new Integer[] { lng, med, recent });
	}

	@Provides
	@Named("tc.priorities.tx")
	@Singleton
	public List<Integer> txPrioritiesMap(
			@Named("tc.tx.urgent.priority") Integer urgent,
			@Named("tc.tx.tight.priority") Integer tight,
			@Named("tc.tx.soon.priority") Integer soon,
			@Named("tc.tx.later.priority") Integer later,
			@Named("tc.tx.eventually.priority") Integer eventually)
	{
		return ListUtility.list(new Integer[] { urgent, tight, soon, later, eventually });
	}

	@Provides
	@Named("tc.rules.caption")
	@Singleton
	public List<PriorityRule> captionPrioritiesRules(
			@Named("tc.caption.tx.soon.threshold") Long soonThreshold,
			@Named("tc.caption.tx.soon.priority") Integer soonPriority,
			@Named("tc.caption.tx.later.threshold") Long laterThresold,
			@Named("tc.caption.tx.later.priority") Integer laterPriotity)
	{

		List<PriorityRule> ret = new ArrayList<PriorityRule>();
		ret.add(new PriorityRule(soonThreshold, soonPriority));
		ret.add(new PriorityRule(laterThresold, laterPriotity));

		Collections.sort(ret, new PriortyRuleAscThresholdComparator());

		log.debug("caption proxy rules");
		for (PriorityRule priorityRule : ret)
		{
			log.debug(priorityRule.toString());
		}

		return ret;
	}

	@Provides
	@Named("tc.rules.tx")
	@Singleton
	public List<PriorityRule> txPrioritiesRules(
			@Named("tc.tx.urgent.threshold") Long urgetThreshold,
			@Named("tc.tx.urgent.priority") Integer urgentPriority,
			@Named("tc.tx.tight.threshold") Long tightThreshold,
			@Named("tc.tx.tight.priority") Integer tightPriority,
			@Named("tc.tx.soon.threshold") Long soonThreshold,
			@Named("tc.tx.soon.priority") Integer soonPriority,
			@Named("tc.tx.later.threshold") Long laterThresold,
			@Named("tc.tx.later.priority") Integer laterPriotity)
	{

		List<PriorityRule> ret = new ArrayList<PriorityRule>();
		ret.add(new PriorityRule(urgetThreshold, urgentPriority));
		ret.add(new PriorityRule(tightThreshold, tightPriority));
		ret.add(new PriorityRule(soonThreshold, soonPriority));
		ret.add(new PriorityRule(laterThresold, laterPriotity));

		Collections.sort(ret, new PriortyRuleAscThresholdComparator());

		log.debug("tx rules");
		for (PriorityRule priorityRule : ret)
		{
			log.debug(priorityRule.toString());
		}

		return ret;
	}

	@Provides
	@Named("tc.rules.compliance")
	@Singleton
	public List<PriorityRule> complianceRules(
			@Named("tc.compliance.long.queued.threshold") Long lngThreshold,
			@Named("tc.compliance.long.queued.priority") Integer lngPriority,
			@Named("tc.compliance.medium.queued.threshold") Long medThreshold,
			@Named("tc.compliance.medium.queued.priority") Integer medPriority)
	{

		List<PriorityRule> ret = new ArrayList<PriorityRule>();
		ret.add(new PriorityRule(lngThreshold, lngPriority));
		ret.add(new PriorityRule(medThreshold, medPriority));

		Collections.sort(ret, new PriortyRuleDescThresholdComparator());

		log.debug("compliance proxy rules");
		for (PriorityRule priorityRule : ret)
		{
			log.debug(priorityRule.toString());
		}

		return ret;

	}

	@Provides
	@Named("tc.rules.publicity")
	@Singleton
	public List<PriorityRule> publicityRules(
			@Named("tc.publicity.long.queued.threshold") Long lngThreshold,
			@Named("tc.publicity.long.queued.priority") Integer lngPriority,
			@Named("tc.publicity.medium.queued.threshold") Long medThreshold,
			@Named("tc.publicity.medium.queued.priority") Integer medPriority)
	{

		List<PriorityRule> ret = new ArrayList<PriorityRule>();
		ret.add(new PriorityRule(lngThreshold, lngPriority));
		ret.add(new PriorityRule(medThreshold, medPriority));

		Collections.sort(ret, new PriortyRuleDescThresholdComparator());

		log.debug("publicity proxy rules");
		for (PriorityRule priorityRule : ret)
		{
			log.debug(priorityRule.toString());
		}

		return ret;

	}
}
