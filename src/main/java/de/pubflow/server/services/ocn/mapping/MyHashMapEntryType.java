package de.pubflow.server.services.ocn.mapping;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import de.pubflow.server.services.ocn.entity.abstractClass.PubJect;



public class MyHashMapEntryType {
	@XmlAttribute
	public String key; 
	
	public List<PubJect> value;
}