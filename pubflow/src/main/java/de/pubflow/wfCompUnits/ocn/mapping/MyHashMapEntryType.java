package de.pubflow.wfCompUnits.ocn.mapping;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import de.pubflow.wfCompUnits.ocn.entity.abstractClass.PubJect;



public class MyHashMapEntryType {
	@XmlAttribute
	public String key; 
	
	public List<PubJect> value;
}