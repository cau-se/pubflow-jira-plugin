package de.pubflow.rest.ocn.entity;

import de.pubflow.rest.ocn.entity.abstractClass.LBPSContainer;


public class Sample extends LBPSContainer{
	
	public static final String ID = "sample_id";
	public static final String VAL = "sample_val";
	public static final String PARAMETERUNITID = "sample_parameter_unit_id";
	public static final String FLAG = "sample_flag";
	
	public static final String[] c_SAMPLETABLE = {ID, VAL, PARAMETERUNITID, FLAG};
	public static final String[] c_SAMPLETABLETYPE = {"String", "Double", "String", "String"};

}
