package de.pubflow.components.jiraConnector;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://pubflow.de/message/jira")
public class JiraMessage{

	private HashMap<String, String> message = new HashMap<String, String>();
	private String action = "";
	private String target = "";
	public String clazz;

	public JiraMessage(){
		clazz = this.getClass().getCanonicalName();
	}



	@XmlElement(name="clazz")
	public String getType() {
		return clazz;
	}

	public void setType(String c) {
		this.clazz = c;
	}

	public void initFromString(String pContent){

	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public JiraMessage(String action){
		this.action = action;
	}

	public JiraMessage(String action, HashMap<String, String> message){
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

	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}
}
