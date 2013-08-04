package de.pubflow.common.entity.workflow;

import java.util.ArrayList;

import de.pubflow.common.entity.StringSerializable;

public class WFParamList implements StringSerializable{

	ArrayList<WFParameter> parameterList = new ArrayList<WFParameter>();
	
	@Override
	public String transformToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initFromString(String content) {
		// TODO Auto-generated method stub
		
	}

}
