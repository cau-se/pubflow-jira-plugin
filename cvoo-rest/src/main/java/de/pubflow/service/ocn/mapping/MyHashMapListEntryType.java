package de.pubflow.service.ocn.mapping;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import de.pubflow.service.ocn.entity.abstractClass.LBPSContainer;



public class MyHashMapListEntryType {
	@XmlAttribute
	public String key; 
	public List<LBPSContainer> value;
}