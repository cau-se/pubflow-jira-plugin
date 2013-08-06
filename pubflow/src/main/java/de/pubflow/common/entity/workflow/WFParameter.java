package de.pubflow.common.entity.workflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(namespace = "http://pubflow.de/wfparameter")
public class WFParameter {
	
	public WFParameter(){}
	
	protected String key;

	@XmlElement(name="key")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

}
