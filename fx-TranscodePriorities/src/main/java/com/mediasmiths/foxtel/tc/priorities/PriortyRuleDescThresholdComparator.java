package com.mediasmiths.foxtel.tc.priorities;

import java.util.Comparator;

public class PriortyRuleDescThresholdComparator implements Comparator<PriorityRule>
{
	@Override
	public int compare(PriorityRule o1, PriorityRule o2)
	{
		return o2.getThreshold().compareTo(o1.getThreshold());
	}
}