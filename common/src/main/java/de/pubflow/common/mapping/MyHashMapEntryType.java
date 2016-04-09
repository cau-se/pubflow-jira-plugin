package de.pubflow.common.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MyHashMapEntryType {
	
	@XmlAttribute
	public String key = ""; 
	@XmlAttribute
	public String value = null;
	
	public MyHashMapEntryType() {	
	}
}