package de.pubflow.communication.message.workflow;

import de.pubflow.common.entity.StringSerializable;
import de.pubflow.common.entity.workflow.PubFlow;
import de.pubflow.communication.message.Message;

public class WorkflowMessage extends Message {

	protected PubFlow workflow;


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
