package de.pubflow.server.core.jira;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.Map.Entry;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.entity.workflow.WFParameterList;
import de.pubflow.server.common.repository.WorkflowProvider;
import de.pubflow.server.core.communication.WorkflowMessage;
import de.pubflow.server.core.scheduling.PubFlowJob;
import de.pubflow.server.core.scheduling.Scheduler;

public class JiraConnector {
	private static Logger myLogger = LoggerFactory.getLogger(JiraConnector.class.getSimpleName());

	final private String[] params = {
			"Author_OCN", 
			"Reference_OCN", 
			"Status_OCN", 
			"Login_OCN", 
			"Leg ID_OCN", 
			"Leg comment_OCN", 
			"Topology_OCN", 
			"PID_OCN", 
			"File name_OCN", 
			"Source_OCN", 
			"Zielpfad_OCN", 
			"Project_OCN",
			"tropfenzahl_Pi",
			"issueKey"};

	static {
		myLogger = LoggerFactory.getLogger(JiraConnector.class);
	}
	
	public static void start() throws Exception {		
		Scheduler.getInstance().start();
	}

	public static void stop(){
		Scheduler.getInstance().shutdown();
	}


	/**
	 * Maps the Jira Msg to a Workflow Msg
	 * 
	 * @param msg
	 * @throws SchedulerException 
	 */
	public void compute(WorkflowMessage msg) throws SchedulerException {
		myLogger.info("Received Msg from Jira-Plugin");
		WorkflowMessage wfMsg = new WorkflowMessage();
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
				wfMsg.setState(null);
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
					wfMsg.setWorkflowID(WorkflowProvider.getInstance().getIDByWFName("de.pubflow.OCN"));
					myLogger.info("WFID:" + WorkflowProvider.getInstance().getIDByWFName("de.pubflow.OCN"));

				}else if (value.equalsIgnoreCase("Pi")){
					wfMsg.setWorkflowID(WorkflowProvider.getInstance().getIDByWFName("de.pubflow.Pi"));
					myLogger.info("WFID:" + WorkflowProvider.getInstance().getIDByWFName("de.pubflow.Pi"));
				}
				break;
				
			default:
				if(Arrays.asList(params).contains(key)){	
					WFParameter param1 = new WFParameter();
					param1.setKey(key);
					param1.setPayloadClazz(ParameterType.STRING);
					myLogger.info("Set " + key + " to " + value);
					param1.setStringValue(value);
					paramList.add(param1);
					break;
				}
				break;
			}
		}
		wfMsg.setParameters(paramList);

		myLogger.info("recMillis : " + quartzMillis);
		myLogger.info("curMillis : " + System.currentTimeMillis());

		if(!quartzCron.equals("")){
			JobDetail job = newJob(PubFlowJob.class)
					.withIdentity("job_" + System.currentTimeMillis(), "pubflow")
					.build();
			job.getJobDataMap().put("msg", wfMsg);

			Trigger trigger = newTrigger()
					.withIdentity("cron_" + System.currentTimeMillis() + "-trigger", "pubflow")
					.startAt(new Date(quartzMillis))
					.withSchedule(SimpleScheduleBuilder.simpleSchedule())            
					.build();
			Scheduler.getInstance().getScheduler().scheduleJob(job, trigger);
			
		}else if(quartzMillis > System.currentTimeMillis()){
			JobDetail job = newJob(PubFlowJob.class)
					.withIdentity("job_" + quartzMillis, "pubflow")
					.build();
			job.getJobDataMap().put("msg", wfMsg);

			Trigger trigger = newTrigger()
					.withIdentity("job_" + quartzMillis + "-trigger", "pubflow")
					.startAt(new Date(quartzMillis))
					.withSchedule(SimpleScheduleBuilder.simpleSchedule())            
					.build();
			Scheduler.getInstance().getScheduler().scheduleJob(job, trigger);

		}else{
			myLogger.info("Transmitting Msg to pubflow core...");
			// Sending WFMsg
			//wfMsg

			myLogger.info("Msg sent!");
		}
	}
}
