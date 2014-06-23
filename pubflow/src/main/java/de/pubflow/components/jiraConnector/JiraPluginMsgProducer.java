package de.pubflow.components.jiraConnector;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.PubFlowSystem;
import de.pubflow.common.entity.workflow.ParameterType;
import de.pubflow.common.entity.workflow.WFParamList;
import de.pubflow.common.entity.workflow.WFParameter;
import de.pubflow.common.enumerartion.WFState;
import de.pubflow.common.repository.workflow.WorkflowProvider;
import de.pubflow.components.quartz.PubFlowJob;
import de.pubflow.components.quartz.Scheduler;
import de.pubflow.core.communication.message.MessageToolbox;
import de.pubflow.core.communication.message.jira.CamelJiraMessage;
import de.pubflow.core.communication.message.workflow.WorkflowMessage;


public class JiraPluginMsgProducer {

	private static Logger myLogger;
	private static final String START_WF = "";

	//public enum KEYWORDS{ status };
	
	final private String[] ocnParams = {
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
	"Project_OCN"};

	static {
		myLogger = LoggerFactory.getLogger(JiraPluginMsgProducer.class);
	}

	/**
	 * Maps the Jira Msg to a Workflow Msg
	 * 
	 * @param msg
	 * @throws SchedulerException 
	 */
	public void onMsg(CamelJiraMessage msg) throws SchedulerException {
		myLogger.info("Received Msg from Jira-Plugin");
		WorkflowMessage wfMsg = new WorkflowMessage();
		WFParamList paramList = new WFParamList();
		// Mapping PubFlowMsg to WorkflowMessage
		myLogger.info("Transforming msg to wfMessage");
		Set<Entry<String, String>> fieldmap = msg.getMessage().entrySet();

		String mapDebugString = "";
		for(Entry<String, String> e : fieldmap){ 
			mapDebugString += e.getKey() + " : " + e.getValue() + "\n";
		}
		myLogger.info(mapDebugString);

		long quartzMillis = 0l;
		String quartzCron = "";
		
		for (Entry<String, String> entry : fieldmap) {
			String key = entry.getKey();
			String value = entry.getValue();

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
				wfMsg.setWfstate(WFState.parseJiraString(value));
				break;

			case "assignee":
				break;

			case "eventType":
				break;

			case "date":
				break;
				
			case "reporter":
				break;

			case "Status":

				break;
			case "workflowName":
				myLogger.info("Val: "+key+" > "+value);
				if(value.equalsIgnoreCase("OCN")){
					wfMsg.setWorkflowID(WorkflowProvider.getInstance().getIDByWFName("de.pubflow.OCN"));
					myLogger.info("WFID:" + WorkflowProvider.getInstance().getIDByWFName("de.pubflow.OCN"));

				}
				break;
				
			default:
				if(Arrays.asList(ocnParams).contains(key)){	
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
		wfMsg.setWfparams(paramList);

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
			ProducerTemplate producer;
			CamelContext context = PubFlowSystem.getInstance().getContext();
			producer = context.createProducerTemplate();
			producer.sendBody("test-jms:queue:testOut.queue",
					MessageToolbox.transformToString(wfMsg));

			myLogger.info("Msg sent!");
		}
	}
}
