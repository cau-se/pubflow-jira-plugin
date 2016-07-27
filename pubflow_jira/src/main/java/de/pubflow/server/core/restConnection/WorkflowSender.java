package de.pubflow.server.core.restConnection;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.web.client.RestTemplate;

import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.core.communication.WorkflowCall;


public class WorkflowSender {
	private static WorkflowSender instance;
	private URL targetURL;

	
	private WorkflowSender() throws WFRestException{
		//TODO load url from file
		try {
			targetURL = new URL("http://localhost:8080/executeNewWF");
		} catch (MalformedURLException e) {
			throw new WFRestException("could not load target URL");
		}
	}
	
	synchronized public static WorkflowSender getInstance(){
		if(instance == null){
			try {
				instance = new WorkflowSender();	

			} catch (WFRestException e) {
				//TODO better exception handling
				e.printStackTrace();
			}
		}
		return instance;
			
	}
	
	public void initWorkflow(WorkflowCall wfCall){
		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.postForObject(targetURL.toString(), wfCall, String.class);

	}
	
	//TODO introduce updatemessage as type
	public void updateWorkflow(WorkflowCall wfCall){
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.put(targetURL.toString(), wfCall);
	}

}
