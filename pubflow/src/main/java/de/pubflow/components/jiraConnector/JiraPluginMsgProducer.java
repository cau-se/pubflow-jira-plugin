package de.pubflow.components.jiraConnector;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

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
import de.pubflow.common.entity.User;
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

	public enum KEYWORDS{ status };
	static {
		myLogger = LoggerFactory.getLogger(JiraPluginMsgProducer.class);
	}

	/**
	 * Map the Jira Msg to a Workflow Msg
	 * 
	 * Author_OCN : 8 Reference_OCN : 0 status : TaskRecived Login_OCN : 2
	 * Topology_OCN : 7 Zielpfad_OCN : 12 assignee : admin issueId : 10101
	 * Source_OCN : 1 eventType : 1 File name_OCN : 4 date : 1376395102681 Leg
	 * ID_OCN : 9 Project_OCN : 11 reporter : admin Status_OCN : 3 workflowName
	 * : OCN PID_OCN : 6 Leg comment_OCN : 5
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

		for (Entry<String, String> entry : fieldmap) {
			String key = entry.getKey();
			String value = entry.getValue();

			//TODO: add params?

			switch (key) {
			case "Author":
				wfMsg.setUser(User.getUserFromJiraID(value));
				break;
			case "quartzMillis":
				quartzMillis = Long.parseLong(value);
				break;
			case "Reference":

				break;
			case "status":
				wfMsg.setWfstate(WFState.parseJiraString(value));
				break;
			case "Login":

				break;
			case "Topology":

				break;
			case "Zielpfad":

				break;
			case "assignee":

				break;
			case "issueKey":
				WFParameter param = new WFParameter();
				param.setKey("issueKey");
				param.setPayloadClazz(ParameterType.STRING);
				myLogger.info("Set issueKey to "+value);
				param.setStringValue(value);
				paramList.add(param);
				break;
			case "Source":

				break;
			case "eventType":

				break;
			case "File name":

				break;
			case "date":

				break;
			case "Leg ID":
				WFParameter param1 = new WFParameter();
				param1.setKey("legID");
				param1.setPayloadClazz(ParameterType.INTEGER);
				int val = Integer.parseInt(value);
				myLogger.info("Set Leg_ID to "+val);
				param1.setIntValue(val);
				paramList.add(param1);
				break;
			case "Project":

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
			case "PID":

				break;
			case "Leg comment":

				break;

			default:
				break;
			}
		}
		wfMsg.setWfparams(paramList);

		myLogger.info("recMillis : " + quartzMillis);
		myLogger.info("curMillis : " + System.currentTimeMillis());
			
		if(quartzMillis > System.currentTimeMillis()){
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
