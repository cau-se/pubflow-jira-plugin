package de.pubflow.server.common.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class ServiceCallData implements Serializable{
	ArrayList <String> content = new ArrayList<String>();
	int wfId = 0;
	int callbackId = 0;
	
	public ServiceCallData(int wfId, int callbackId){
		this.wfId = wfId;
		this.callbackId = callbackId;
	}
	
	
	public ArrayList<String> getContent() {
		return content;
	}

	public void setItems(ArrayList<String> content) {
		this.content = content;
	}
	
	public void addContent(String entry){
		content.add(entry);
	}
}