package de.pubflow.server.core.scheduling;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.core.workflow.WFBroker;
import de.pubflow.server.core.workflow.WorkflowMessage;

public class PubFlowJob implements Job {
	private static Logger myLogger;

	static {
		myLogger = LoggerFactory.getLogger(PubFlowJob.class);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap data = context.getJobDetail().getJobDataMap();
		WorkflowMessage workflowMessage = (WorkflowMessage) data.get("msg");

		myLogger.info("Transmitting Msg to pubflow core...");
		
		// Sending WFMsg
		WFBroker.getInstance().receiveWFCall(workflowMessage);

		myLogger.info("Msg sent!");
	}



}
