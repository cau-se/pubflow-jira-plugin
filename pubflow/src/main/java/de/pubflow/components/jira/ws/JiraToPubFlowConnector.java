package de.pubflow.components.jira.ws;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.jws.WebService;

import org.quartz.SchedulerException;

import de.pubflow.common.entity.WorkflowEntity;
import de.pubflow.common.repository.WorkflowProvider;
import de.pubflow.components.jira.JiraMessage;
import de.pubflow.components.jira.JiraPluginMsgProducer;
import de.pubflow.core.communication.jira.CamelJiraMessage;

@WebService(endpointInterface = "de.pubflow.components.jiraConnector.ws.IJiraToPubFlowConnector")
public class JiraToPubFlowConnector implements IJiraToPubFlowConnector{

	@Override
	public void eventNotification(JiraMessage message) {
		System.out.println("Received notification");

		CamelJiraMessage camelMsg = new CamelJiraMessage();
		camelMsg.setAction(message.getAction());
		System.out.println("action : " + message.getAction());
		camelMsg.setTarget(message.getTarget());
		System.out.println("target : " + message.getTarget());

		System.out.println("message size : " + message.getMessage().size());
		
		for(Entry<String, String> e : message.getMessage().entrySet()){
			camelMsg.getMessage().put(e.getKey(), e.getValue());
		}

		try {
			JiraPluginMsgProducer msgProducer = new JiraPluginMsgProducer();
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
