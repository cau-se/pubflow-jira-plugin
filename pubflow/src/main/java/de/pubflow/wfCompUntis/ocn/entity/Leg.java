package de.pubflow.wfCompUntis.ocn.entity;

import javax.xml.bind.annotation.XmlRootElement;

import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;



@XmlRootElement
public class Leg extends PubJect{

	public static final String c_name = "leg_name";
	public static final String c_expocode = "leg_expocode"; 
	public static final String c_id = "leg_id";
	public static final String c_parameterList = "parameterList";
	public static final String c_bottleList = "bottleList";
	public static final String c_logString = "logString";

	public static final String[] c_LEGTABLE = {c_name, c_expocode, c_id};
}
