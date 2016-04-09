package de.pubflow.rest.ocn.entity;

import javax.xml.bind.annotation.XmlRootElement;

import de.pubflow.rest.ocn.entity.abstractClass.LBPSContainer;



@XmlRootElement
public class Leg extends LBPSContainer{

	public static final String NAME = "leg_name";
	public static final String EXPOCODE = "leg_expocode"; 
	public static final String ID = "leg_id";
	public static final String PARAMETERLIST = "parameterList";
	public static final String BOTTLELIST = "bottleList";

	public static final String[] LEGTABLE = {NAME, EXPOCODE, ID};
}
