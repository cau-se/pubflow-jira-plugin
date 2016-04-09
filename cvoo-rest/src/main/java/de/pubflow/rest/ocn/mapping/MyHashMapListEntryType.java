package de.pubflow.rest.ocn.mapping;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import de.pubflow.rest.ocn.entity.abstractClass.LBPSContainer;



public class MyHashMapListEntryType {
	@XmlAttribute
	public String key; 
	public List<LBPSContainer> value;
}