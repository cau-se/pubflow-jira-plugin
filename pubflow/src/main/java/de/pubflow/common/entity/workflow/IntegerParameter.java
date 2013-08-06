package de.pubflow.common.entity.workflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://pubflow.de/wfparameter/integer")
public class IntegerParameter extends WFParameter {
	
	private int value;

	public IntegerParameter(){}
	public IntegerParameter(String string, int i) {
		key = string;
		value = i;
	}

	/**
	 * @return the value
	 */
	@XmlElement(name="value")
	public int getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}

}
