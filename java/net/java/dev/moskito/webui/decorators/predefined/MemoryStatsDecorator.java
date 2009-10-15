package net.java.dev.moskito.webui.decorators.predefined;

import java.util.ArrayList;
import java.util.List;

import net.java.dev.moskito.core.predefined.MemoryStats;
import net.java.dev.moskito.core.producers.IStats;
import net.java.dev.moskito.core.stats.TimeUnit;
import net.java.dev.moskito.webui.bean.LongValueBean;
import net.java.dev.moskito.webui.bean.StatValueBean;
import net.java.dev.moskito.webui.decorators.AbstractDecorator;

public class MemoryStatsDecorator extends AbstractDecorator{
	
	private static long MB = 1024L*1024;
	
	private static String CAPTIONS[] = {
		"Current",
		"Min",
		"Max",
		"Current Mb",
		"Min Mb",
		"Max Mb",
	};
	
	private static String SHORT_EXPLANATIONS[] = {
		"Current amount of memory",
		"Minimum amount of memory",
		"Maximum amount of memory",
		"Current amount of memory in Mb",
		"Minimum amount of memory in Mb",
		"Maximum amount of memory in Mb",
	};

	private static String EXPLANATIONS[] = {
		"Current amount of memory",
		"Minimum amount of memory",
		"Maximum amount of memory",
		"Current amount of memory in Mb",
		"Minimum amount of memory in Mb",
		"Maximum amount of memory in Mb",
	};

	public MemoryStatsDecorator(){
		super("Memory", CAPTIONS, SHORT_EXPLANATIONS, EXPLANATIONS);
	}
	

	public List<StatValueBean> getValues(IStats statsObject, String interval, TimeUnit unit) {
		MemoryStats stats = (MemoryStats)statsObject;
		List<StatValueBean> ret = new ArrayList<StatValueBean>(CAPTIONS.length);
		int i = 0;
		ret.add(new LongValueBean(CAPTIONS[i++], stats.getCurrent(interval)));
		ret.add(new LongValueBean(CAPTIONS[i++], stats.getMin(interval)));
		ret.add(new LongValueBean(CAPTIONS[i++], stats.getMax(interval)));
		ret.add(new LongValueBean(CAPTIONS[i++], stats.getCurrent(interval)/MB));
		ret.add(new LongValueBean(CAPTIONS[i++], stats.getMin(interval)/MB));
		ret.add(new LongValueBean(CAPTIONS[i++], stats.getMax(interval)/MB));
		return ret;
	}
}