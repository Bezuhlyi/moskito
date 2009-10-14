package net.java.dev.moskito.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.java.dev.moskito.core.predefined.MemoryStats;
import net.java.dev.moskito.core.producers.IStats;
import net.java.dev.moskito.core.producers.IStatsProducer;

/**
 * A builtin memory producer for Runtime.get... memory methods. 
 * @author lrosenberg
 *
 */
public class BuiltInMemoryProducer implements IStatsProducer{
	/**
	 * The id of the producer.
	 */
	private String producerId;
	/**
	 * Stats.
	 */
	private MemoryStats stats;
	/**
	 * Cached stats list.
	 */
	private List<IStats> statsList;
	/**
	 * The resolver for memory values reading.
	 */
	private RuntimeMemoryResolver resolver;
	
	public static final String FREE = "JavaRuntimeFree";
	public static final String MAX = "JavaRuntimeMax";
	public static final String TOTAL = "JavaRuntimeTotal";
	
	/**
	 * Private timer instance.
	 */
	private static final Timer timer = new Timer("MoskitoMemoryReader", true);
	
	public BuiltInMemoryProducer(String aProducerId){
		statsList = new ArrayList<IStats>();
		stats = new MemoryStats(aProducerId);
		statsList.add(stats);
		
		producerId = aProducerId;
		if (FREE.equals(aProducerId))
			resolver = new FreeMemoryResolver();
		if (MAX.equals(aProducerId))
			resolver = new MaxMemoryResolver();
		if (TOTAL.equals(aProducerId))
			resolver = new TotalMemoryResolver();
		
		if (resolver==null)
			throw new IllegalArgumentException("Illegal producerId, expected: "+FREE+", "+TOTAL+" or "+MAX);
		
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				readMemory();
			}
		}, 0, 1000L*60);
		readMemory();
	}
	
	@Override
	public String getCategory() {
		return "memory";
	}

	@Override
	public String getProducerId() {
		return producerId;
	}

	@Override
	public List<IStats> getStats() {
		return statsList;
	}

	@Override
	public String getSubsystem() {
		return SUBSYSTEM_BUILTIN;
	}
	
	/**
	 * Resolver interface for different subtypes of memory usage (free, total, max).
	 * @author another
	 *
	 */
	private interface RuntimeMemoryResolver {
		public long getMemoryValue();
	}

	
	private class FreeMemoryResolver implements RuntimeMemoryResolver{
		public long getMemoryValue(){
			return Runtime.getRuntime().freeMemory();
		}
	}
	private class MaxMemoryResolver implements RuntimeMemoryResolver{
		public long getMemoryValue(){
			return Runtime.getRuntime().maxMemory();
		}
	}
	private class TotalMemoryResolver implements RuntimeMemoryResolver{
		public long getMemoryValue(){
			return Runtime.getRuntime().totalMemory();
		}
	}

	private void readMemory() {
		stats.updateMemoryValue(resolver.getMemoryValue());
	}
}
