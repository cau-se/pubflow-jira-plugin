package de.pubflow.server.core.jira;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.entity.workflow.WFParameterList;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.core.workflow.WFBroker;
import de.pubflow.server.core.workflow.WorkflowMessage;

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


	public static void main (String[] args) throws Exception{
		WorkflowMessage wm = new WorkflowMessage();
		wm.setWorkflowID("de.pubflow.OCN");
		WFParameterList wpl = new WFParameterList();
		WFParameter wp1 = new WFParameter("issueKey", "PUB-3");
		WFParameter wp2 = new WFParameter("workflowName", "OCN");		
		WFParameter wp3 = new WFParameter("Leg ID_OCN", "s");
		wpl.add(wp1);
		wpl.add(wp2);
		wpl.add(wp3);
		wm.setParameters(wpl);
		wm.setType(WFType.BPMN2);

		new JiraConnector().compute(wm);
	}

	/**
	 * 
	 * @param msg
	 * @throws SchedulerException 
	 */
	public void compute(WorkflowMessage msg) {
		myLogger.info("Received Msg from Jira-Plugin");

		// Mapping PubFlowMsg to WorkflowMessage
		myLogger.info("Transforming msg to wfMessage");
		WFParameterList parameters = msg.getParameters();

		long quartzMillis = 0l;
		String quartzCron = "";

		WFParameterList filteredParameters = new WFParameterList();

		for(WFParameter parameter : parameters.getParameterList()){ 
			myLogger.info(parameter.getKey() + " : " + parameter.getValue());
			String key = parameter.getKey();

			if(parameter.getPayloadClazz().equals(ParameterType.STRING)){
				String value = (String) parameter.getValue();

				switch (key) {

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
				case "issueKey":
					filteredParameters.add(parameter);
					break;
				case "reporter":
					break;
				default:
					try{
						if(msg.getWorkflowID().substring(msg.getWorkflowID().lastIndexOf(".") + 1).equals(key.substring(key.lastIndexOf("_") + 1))){
							parameter.setKey(key.substring(0, key.lastIndexOf("_")));
							filteredParameters.add(parameter);
						}
					}catch(Exception e){
						myLogger.error(e.getCause().toString() + " : " + e.getMessage());
					}

				}
			}
		}			

		msg.setParameters(filteredParameters);


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

		myLogger.info("Leaving JiraConnector");			
		WFBroker.getInstance().receiveWFCall(msg);
	}
}

