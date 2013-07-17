package de.pubflow.common.entity.workflow;

import javax.persistence.Entity;
import javax.persistence.Id;

import de.pubflow.common.enumerartion.WFType;


@Entity
public class PubFlow {

	@Id
	private long PubFlowWFID;
	
	private String WFID;
	private WFType type;

	public WFType getType() {
		return type;
	}

	public void setType(WFType type) {
		this.type = type;
	}

	public String getWFID() {
		return WFID;
	}

	public byte[] getWFAsByteArray() {
		return null;
	}

	public void setWFID(String wFID) {
		WFID = wFID;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	public long getPubFlowWFID() {
		return PubFlowWFID;
	}

	public void setPubFlowWFID(long pubFlowWFID) {
		PubFlowWFID = pubFlowWFID;
	}
}
