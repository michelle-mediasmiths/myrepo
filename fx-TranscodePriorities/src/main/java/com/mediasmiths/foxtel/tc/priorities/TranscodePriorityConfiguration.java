package com.mediasmiths.foxtel.tc.priorities;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TranscodePriorityConfiguration
{

	private final List<Integer> manualInterventionPriorities;

	private final Map<TranscodeJobType, Integer> highestPriorities;
	private final Map<TranscodeJobType, Integer> lowestPriorities;
	private final Long pastTxPriorityThreshold;

	private final List<PriorityRule> publicityRules;
	private final List<PriorityRule> complianceRules;
	private final List<PriorityRule> captionRules;
	private final List<PriorityRule> txRules;

	@Inject
	public TranscodePriorityConfiguration(
			@Named("tc.intervention.priorities") String interventionPriorities,
			@Named("tc.priorities.map.highest") Map<TranscodeJobType, Integer> highestPriorities,
			@Named("tc.priorities.map.lowest") Map<TranscodeJobType, Integer> lowestPriorities,
			@Named("tc.past.tx.recent.threshold") Long pastTxPriorityThreshold,
			@Named("tc.rules.publicity") List<PriorityRule> publicityRules,
			@Named("tc.rules.compliance") List<PriorityRule> complianceRules,
			@Named("tc.rules.caption") List<PriorityRule> captionRules,
			@Named("tc.rules.tx") List<PriorityRule> txRules)
	{

		this.manualInterventionPriorities = parseInterventionPrioirities(interventionPriorities);
		this.highestPriorities = highestPriorities;
		this.lowestPriorities = lowestPriorities;
		this.pastTxPriorityThreshold = pastTxPriorityThreshold;

		this.publicityRules = publicityRules;
		this.complianceRules = complianceRules;
		this.captionRules = captionRules;
		this.txRules = txRules;

	}

	private List<Integer> parseInterventionPrioirities(String interventionPriorities)
	{

		String[] strPriorities = StringUtils.split(interventionPriorities, ',');
		List<Integer> ret = new ArrayList<Integer>(strPriorities.length);

		for (String string : strPriorities)
		{
			ret.add(Integer.parseInt(string));
		}

		return ret;
	}

	public List<Integer> getManualInterventionProirities()
	{
		return manualInterventionPriorities;
	}

	public Integer getHighestPriorityForJobType(TranscodeJobType type)
	{
		return highestPriorities.get(type);
	}

	public Integer getLowestPriorityForJobType(TranscodeJobType type)
	{
		return lowestPriorities.get(type);
	}

	public long getPastTxPriorityThreshold()
	{
		return pastTxPriorityThreshold.longValue();
	}

	public List<PriorityRule> getPublicityRules()
	{
		return publicityRules;
	}

	public List<PriorityRule> getComplianceRules()
	{
		return complianceRules;
	}

	public List<PriorityRule> getCaptionRules()
	{
		return captionRules;
	}

	public List<PriorityRule> getTxRules()
	{
		return txRules;
	}
}
