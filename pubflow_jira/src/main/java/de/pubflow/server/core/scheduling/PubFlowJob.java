package de.pubflow.server.core.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.core.workflow.WorkflowBroker;
import de.pubflow.server.core.workflow.ServiceCallData;

public class PubFlowJob {
	private static Logger myLogger;

	static {
		myLogger = LoggerFactory.getLogger(PubFlowJob.class);
	}

	public static void execute(ServiceCallData data) {
		myLogger.info("Starting scheduled job");
		WorkflowBroker.getInstance().receiveWFCall(data);
	}
}
