package de.cau.tf.ifi.se.pubflow.common.persistence.dao;

import de.cau.tf.ifi.se.pubflow.common.entity.workflow.PubFlow;

public class PubFlowDAO extends BasicDAO<PubFlow>{

	public PubFlowDAO() {
		super(PubFlow.class, "PubFlow");
	}
}
