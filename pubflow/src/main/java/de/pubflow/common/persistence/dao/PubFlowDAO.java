package de.pubflow.common.persistence.dao;

import de.pubflow.common.entity.workflow.PubFlow;

public class PubFlowDAO extends BasicDAO<PubFlow>{

	public PubFlowDAO() {
		super(PubFlow.class, "PubFlow");
	}
}
