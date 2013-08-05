package de.pubflow.common.entity.workflow;

import java.util.ArrayList;

import de.pubflow.common.entity.StringSerializable;

public class WFParamList{

	ArrayList<WFParameter> parameterList = new ArrayList<WFParameter>();
	
	public ArrayList<WFParameter> getParameter()
	{
		return parameterList;
	}

}
