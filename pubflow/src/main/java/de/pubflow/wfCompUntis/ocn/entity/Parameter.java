package de.pubflow.wfCompUntis.ocn.entity;

import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;


public class Parameter extends PubJect{

	public static final String c_description = "parameter_description";
	public static final String c_pangaeaId = "parameter_unit_pangaea_id"; 
	public static final String c_unit = "parameter_unit_mathematical"; 
	public static final String c_unitId = "parameter_unit_id";
	public static final String c_methodId = "parameter_method_id"; 
	public static final String c_piId = "parameter_pi_id";
	public static final String c_abbreviation = "parameter_abbreviation";
	public static final String c_name = "parameter_name";
	public static final String c_format = "parameter_format";
	public static final String c_comment = "parameter_comment";
	
	public static final String[] c_PARAMETERTABLE = {c_description, c_comment, c_pangaeaId, c_unit, c_unitId, c_methodId, c_piId};
}
