package de.pubflow.common.entity;

import java.util.HashMap;
import java.util.Map.Entry;

public class PubFlowMessage {

	String target = "";
	public String getTarget() {
		return target;
	}

	public String getMsgAsString()
	{
		//TODO
		return "";
	}
	
	public static PubFlowMessage initFromString(String msg)
	{
		//TODO
		return null;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	String action = "";

	HashMap<String, String> message = new HashMap<String, String>();

	public PubFlowMessage(String action){
		this.action = action;
	}
	
	public PubFlowMessage(String action, HashMap<String, String> message){
		this.action = action;
		this.message = message;
	}
	

	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

	public HashMap<String, String> getMessage() {
		return message;
	}

	public void setMessage(HashMap<String, String> message) {
		this.message = message;
	}

	public static HashMap<String, String> getMap(String key, HashMap<String, String> message){
		HashMap<String, String> map = new HashMap<String, String>();

		for(Entry<String, String> e : message.entrySet()){
			if (e.getKey().startsWith(key + "_")){
				map.put(e.getKey().substring(key.length()), e.getValue());
			}
		}
		
		return map;
	}
}
