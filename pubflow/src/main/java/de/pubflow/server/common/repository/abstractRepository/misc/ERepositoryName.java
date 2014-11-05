package de.pubflow.server.common.repository.abstractRepository.misc;

public enum ERepositoryName {
	CONFIGURATION(1),
	CONTEXT(2), 
	DATA(3),
	SERVICE(4),
	WORKFLOW(5);
	
	private int type;
	
	ERepositoryName(int type){
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
