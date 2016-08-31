/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow.server.core.jira;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://pubflow.de/message/jira")
public class JiraMessage{

	private HashMap<String, String> message = new HashMap<String, String>();
	private String action = "";
	private String target = "";

	public JiraMessage(){
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
