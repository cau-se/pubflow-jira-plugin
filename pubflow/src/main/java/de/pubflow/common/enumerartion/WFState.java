package de.pubflow.common.enumerartion;

public enum WFState {

	READY,
	STARTED,
	RUNNING,
	CANCELED,
	STOPPED,
	DELETED;
	
	public static WFState parseJiraString(String val)
	{
		//TODO
		return null;
	}
}
