package de.pubflow.wfCompUntis.ocn.entity;

import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;


public class Parameter extends PubJect{

	public static final String DESCRIPTION = "parameter_description";
	public static final String PANGAEAID = "parameter_unit_pangaea_id"; 
	public static final String UNIT = "parameter_unit_mathematical"; 
	public static final String UNITID = "parameter_unit_id";
	public static final String METHODID = "parameter_method_id"; 
	public static final String PIID = "parameter_pi_id";
	public static final String ABBREVIATION = "parameter_abbreviation";
	//= NAME - FIRST TOKEN (ATOMIC) FROM DESCRIPTION 
	public static final String NAME = "parameter_name";
	public static final String FORMAT = "parameter_format";
	public static final String COMMENT = "parameter_comment";
	
	public static final String[] c_PARAMETERTABLE = {DESCRIPTION, ABBREVIATION, COMMENT, PANGAEAID, UNITID, UNIT, METHODID, PIID};
}
