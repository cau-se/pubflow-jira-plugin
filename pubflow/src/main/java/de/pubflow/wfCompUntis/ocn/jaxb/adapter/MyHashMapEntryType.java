package de.pubflow.wfCompUntis.ocn.jaxb.adapter;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;



public class MyHashMapEntryType {
	@XmlAttribute
	public String key; 
	
	public List<PubJect> value;
}