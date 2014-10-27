package de.pubflow.components.quartz;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.PubFlowSystem;
import de.pubflow.components.jira.JiraPluginMsgProducer;
import de.pubflow.core.communication.MessageToolbox;
import de.pubflow.core.communication.workflow.WorkflowMessage;

public class PubFlowJob implements Job {
	private static Logger myLogger;

	static {
		myLogger = LoggerFactory.getLogger(JiraPluginMsgProducer.class);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap data = context.getJobDetail().getJobDataMap();
		WorkflowMessage workflowMessage = (WorkflowMessage) data.get("msg");

		myLogger.info("Transmitting Msg to pubflow core...");
		// Sending WFMsg
		ProducerTemplate producer;
		CamelContext camelContext = PubFlowSystem.getInstance().getContext();
		producer = camelContext.createProducerTemplate();
		producer.sendBody("test-jms:queue:testOut.queue",
				MessageToolbox.transformToString(workflowMessage));

		myLogger.info("Msg sent!");
	}



}
