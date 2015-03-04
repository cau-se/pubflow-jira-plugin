package de.pubflow.server.core.jira;

import it.sauronsoftware.cron4j.Scheduler;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.entity.workflow.WFParameterList;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.core.scheduling.PubFlowJob;
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
		myLogger.info("Compute");
		
		String quartzCron = "";

		WFParameterList parameters = msg.getParameters();
		WFParameterList filteredParameters = new WFParameterList();

		for(WFParameter parameter : parameters.getParameterList()){ 
			myLogger.info(parameter.getKey() + " : " + parameter.getValue());
			String key = parameter.getKey();

			if(parameter.getPayloadClazz().equals(ParameterType.STRING)){
				String value = (String) parameter.getValue();

				switch (key) {
				case "quartzCron":
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
				case "workflowName":
					break;
				default:
					try{
						//if(msg.getWorkflowID().substring(msg.getWorkflowID().lastIndexOf(".") + 1).equals(key.substring(key.lastIndexOf("_") + 1))){
						//	parameter.setKey(key.substring(0, key.lastIndexOf("_")));
						parameter.setKey(key);
						filteredParameters.add(parameter);
						//}
					}catch(Exception e){
						myLogger.error(e.getCause().toString() + " : " + e.getMessage());
					}

				}
			}
		}			

		msg.setParameters(filteredParameters);


//		JiraSchedulerFactory fy = new JiraSchedulerFactory(); 
//
//		try {
//
			if(!quartzCron.equals("")){
				myLogger.info("Scheduling new job");			

				final WorkflowMessage schedulerMsg = msg;
				Scheduler s = new Scheduler();
				
				s.schedule(quartzCron, new Runnable() {					
					public void run() {
						PubFlowJob.execute(schedulerMsg);
					}
				});
				
				s.start();
//			}else if(quartzMillis > System.currentTimeMillis()){
//				JobDetail job = new JobDetail();
//				job.setJobClass(PubFlowJob.class);
//				job.getJobDataMap().put("msg", msg);
//				job.setName(msg.getWorkflowID() + System.currentTimeMillis());
//				job.setDurability(true);
//				
//				Trigger trigger = null;
//				//trigger.setStartTime(new Date(quartzMillis));
//				trigger.setName(msg.getWorkflowID() + System.currentTimeMillis());
//				fy.getScheduler().scheduleJob(job, trigger);
//				
			}else{
				myLogger.info("Leaving JiraConnector");			
				WFBroker.getInstance().receiveWFCall(msg);
			}
			
	}
}
