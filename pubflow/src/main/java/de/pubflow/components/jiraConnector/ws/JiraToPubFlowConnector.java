package de.pubflow.components.jiraConnector.ws;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.jws.WebService;

import org.quartz.SchedulerException;

import de.pubflow.common.entity.repository.WorkflowEntity;
import de.pubflow.common.repository.workflow.WorkflowProvider;
import de.pubflow.components.jiraConnector.JiraMessage;
import de.pubflow.components.jiraConnector.JiraPluginMsgProducer;
import de.pubflow.core.communication.message.jira.CamelJiraMessage;

@WebService(endpointInterface = "de.pubflow.components.jiraConnector.ws.IJiraToPubFlowConnector")
public class JiraToPubFlowConnector implements IJiraToPubFlowConnector{

	@Override
	public void eventNotification(JiraMessage message) {
		System.out.println("Starting");
		JiraPluginMsgProducer msgProducer = new JiraPluginMsgProducer();

		CamelJiraMessage camelMsg = new CamelJiraMessage();

//		camelMsg.setAction(message.getAction());
//		camelMsg.setTarget(message.getTarget());
//		camelMsg.setType(message.getType());		
		
		for(Entry<String, String> e : message.getMessage().entrySet()){
			camelMsg.getMessage().put(e.getKey(), e.getValue());
		}

		try {
			msgProducer.onMsg(camelMsg);
		} catch (SchedulerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public HashMapStringClassWrapper getParameterMap(String workflowName) {
		 
		for(WorkflowEntity we : WorkflowProvider.getInstance().getAllEntries()){
			System.out.println(we.getWorkflowName() + " / " + workflowName);

			if(we.getWorkflowName().equals(workflowName)){
				return new HashMapStringClassWrapper(we.getParameterMap());
			}
		}
		return new HashMapStringClassWrapper(null);
	}

	@Override
	public HashMapStringLongWrapper getWorkflowNames() {
		HashMap<String, Long> workflows = new HashMap<String, Long>();

		for(WorkflowEntity e : WorkflowProvider.getInstance().getAllEntries()){
			workflows.put(e.getWorkflowName(), e.getPubFlowWFID());
			System.out.println(e.getWorkflowName() + " / " + e.getPubFlowWFID());
		}		

		return new HashMapStringLongWrapper(workflows);
	}
}
