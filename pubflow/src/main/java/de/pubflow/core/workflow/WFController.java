package de.pubflow.core.workflow;

public class WFController {

	private static volatile WFController instance;
	
	private WFController(){
		
	}
	
	public static synchronized WFController getInstance(){
		if(instance == null){
			instance = new WFController();
		}
		return instance;
	}
	
	public void startWF(){
		
	}
}
