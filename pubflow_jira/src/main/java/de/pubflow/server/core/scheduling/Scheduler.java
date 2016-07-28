/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow.server.core.scheduling;

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
			getScheduler().start();

		} catch (SchedulerException se) {
			se.printStackTrace();
		}
	}
	
	public void shutdown(){
		try{
			getScheduler().shutdown();

		} catch (SchedulerException se) {
			se.printStackTrace();
		}
	}
}
