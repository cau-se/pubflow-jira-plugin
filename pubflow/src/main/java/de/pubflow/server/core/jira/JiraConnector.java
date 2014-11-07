package de.pubflow.server.core.jira;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.entity.workflow.WFParameterList;
import de.pubflow.server.common.repository.WorkflowProvider;
import de.pubflow.server.core.communication.WorkflowMessage;
import de.pubflow.server.core.workflow.WFBroker;

public class JiraConnector {

	private static Logger myLogger = LoggerFactory.getLogger(JiraConnector.class.getSimpleName());
	private static JiraConnector jiraConnector;

	static {
		myLogger = LoggerFactory.getLogger(JiraConnector.class);
	}

	private JiraConnector(){
	}

	public static JiraConnector getInstance(){
		if (jiraConnector == null) {
			jiraConnector = new JiraConnector();
		}
		return jiraConnector;			
	}

	public void start() throws Exception {		
		//Scheduler.getInstance().start();
	}

	public void stop(){
		//Scheduler.getInstance().shutdown();
	}


	/**
	 * Maps the Jira Msg to a Workflow Msg
	 * 
	 * @param msg
	 * @throws SchedulerException 
	 */
	public void compute(WorkflowMessage msg) {
		myLogger.info("Received Msg from Jira-Plugin");

		WFParameterList paramList = new WFParameterList();
		// Mapping PubFlowMsg to WorkflowMessage
		myLogger.info("Transforming msg to wfMessage");
		WFParameterList params = msg.getParameters();

		String mapDebugString = "";
		for(WFParameter parameter : params.getParameterList()){ 
			mapDebugString += parameter.getKey() + " : " + parameter.getStringValue() + "\n";
		}
		myLogger.info(mapDebugString);

		long quartzMillis = 0l;
		String quartzCron = "";

		for(WFParameter parameter : params.getParameterList()){ 
			String key = parameter.getKey();
			String value = parameter.getStringValue();

			//TODO: add params?

			switch (key) {
			//			case "Author":
			//				wfMsg.setUser(User.getUserFromJiraID(value));
			//				break;

			case "quartzMillis":
				quartzMillis = Long.parseLong(value);
				break;	


			case "Quartz Cron":
				quartzCron = value;
				break;

			case "status":
				msg.setState(null);
				break;

			case "assignee":
				break;

			case "eventType":
				break;

			case "date":
				break;

			case "reporter":
				break;

			case "workflowName":
				myLogger.info("Val: "+key+" > "+value);
				if(value.equalsIgnoreCase("OCN")){
					msg.setWorkflowID(WorkflowProvider.getInstance().getIDByWFName("de.pubflow.OCN"));
					myLogger.info("WFID:" + WorkflowProvider.getInstance().getIDByWFName("de.pubflow.OCN"));

				}else if (value.equalsIgnoreCase("Pi")){
					msg.setWorkflowID(WorkflowProvider.getInstance().getIDByWFName("de.pubflow.Pi"));
					myLogger.info("WFID:" + WorkflowProvider.getInstance().getIDByWFName("de.pubflow.Pi"));
				}
				break;
			}

			myLogger.info("recMillis : " + quartzMillis);
			myLogger.info("curMillis : " + System.currentTimeMillis());


			//		if(!quartzCron.equals("")){
			//			JobDetail job = newJob(PubFlowJob.class)
			//					.withIdentity("job_" + System.currentTimeMillis(), "pubflow")
			//					.build();
			//			job.getJobDataMap().put("msg", wfMsg);
			//
			//			Trigger trigger = newTrigger()
			//					.withIdentity("cron_" + System.currentTimeMillis() + "-trigger", "pubflow")
			//					.startAt(new Date(quartzMillis))
			//					.withSchedule(SimpleScheduleBuilder.simpleSchedule())            
			//					.build();
			//			Scheduler.getInstance().getScheduler().scheduleJob(job, trigger);
			//			
			//		}else if(quartzMillis > System.currentTimeMillis()){
			//			JobDetail job = newJob(PubFlowJob.class)
			//					.withIdentity("job_" + quartzMillis, "pubflow")
			//					.build();
			//			job.getJobDataMap().put("msg", wfMsg);
			//
			//			Trigger trigger = newTrigger()
			//					.withIdentity("job_" + quartzMillis + "-trigger", "pubflow")
			//					.startAt(new Date(quartzMillis))
			//					.withSchedule(SimpleScheduleBuilder.simpleSchedule())            
			//					.build();
			//			Scheduler.getInstance().getScheduler().scheduleJob(job, trigger);
			//
			//		}else{
			myLogger.info("Transmitting Msg to pubflow core...");			
			WFBroker.getInstance().receiveWFCall(msg);

			myLogger.info("Msg sent!");
		}
	}
}
