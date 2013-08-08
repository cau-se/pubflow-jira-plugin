package de.pubflow.wfCompUntis.ocn.entity;

import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;


public class Sample extends PubJect{
	
	public static final String c_id = "sample_id";
	public static final String c_val = "sample_val";
	public static final String c_parameterUnitId = "sample_parameter_unit_id";
	public static final String c_flag = "sample_flag";
	
	public static final String[] c_SAMPLETABLE = {c_id, c_val, c_parameterUnitId, c_flag};
	public static final String[] c_SAMPLETABLETYPE = {"String", "Double", "String", "String"};

}
