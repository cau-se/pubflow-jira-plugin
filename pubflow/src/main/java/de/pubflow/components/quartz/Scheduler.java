package de.pubflow.components.quartz;

import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class Scheduler{

	private org.quartz.Scheduler quartzScheduler;
	private static Scheduler scheduler;
	
	public static Scheduler getInstance(){
		if (scheduler == null){
			scheduler = new Scheduler();
		}
		return scheduler;
	}
	
	public org.quartz.Scheduler getScheduler() throws SchedulerException{
		if (quartzScheduler == null){
			quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
		}
		return quartzScheduler;
	}
	
	public void start(){
		try{
			quartzScheduler = StdSchedulerFactory.getDefaultScheduler();
			quartzScheduler.start();

		} catch (SchedulerException se) {
			se.printStackTrace();
		}
	}
	
	public void shutdown(){
		try{
			quartzScheduler.shutdown();

		} catch (SchedulerException se) {
			se.printStackTrace();
		}
	}
}
