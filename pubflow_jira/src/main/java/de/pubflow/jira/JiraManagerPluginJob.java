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
package de.pubflow.jira;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;

/**
 * The lifecycle of Jira schedules different jobs. Register PubFlow as a job and
 * add it to the lifecycle. Without this job Jira starts the plugin before the
 * whole system is ready. Some managers (i.e. StatusManager) are not initialized
 * then.
 * 
 * @author abar
 *
 */
public class JiraManagerPluginJob {
	/**
	 * Logger for debuging and info messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JiraManagerPluginJobRunner.class);
	
	/**
	 * The key this job is mapped to.
	 */
	private static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of(JiraManagerPluginJobRunner.class.getName());
	
	/**
	 * The job's id.
	 */
	private static final JobId JOB_ID = JobId.of(JiraManagerPluginJobRunner.class.getName());
	
	/**
	 * The schedule service this job will be added to.
	 */
	private final SchedulerService schedulerService;

	/**
	 * Create a job to run PubFlow
	 * 
	 * @param schedulerService The schedule service this job will be added to
	 */
	public JiraManagerPluginJob(final SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	/**
	 * Start the job that runs pubflow
	 */
	public void init() {
		final JiraManagerPluginJobRunner jobRunner = new JiraManagerPluginJobRunner();
		schedulerService.registerJobRunner(JOB_RUNNER_KEY, jobRunner);

		try {
			schedulerService.scheduleJob(JOB_ID,
					JobConfig.forJobRunnerKey(JOB_RUNNER_KEY).withRunMode(RunMode.RUN_LOCALLY)
							.withSchedule(Schedule.runOnce(new Date(System.currentTimeMillis() + 2000))));
		} catch (SchedulerServiceException schedulerException) {
			LOGGER.warn("Error: " + schedulerException);
		}
	}

	/**
	 * Shut down the job.
	 */
	public void destroy() {
		schedulerService.unregisterJobRunner(JOB_RUNNER_KEY);
	}
}