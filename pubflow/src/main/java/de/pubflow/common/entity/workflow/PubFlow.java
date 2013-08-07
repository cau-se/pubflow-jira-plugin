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
	private byte[] wfDef;
	
	private ReturnTypeList returnList = new ReturnTypeList();

	/**
	 * @return the returnList
	 */
	public synchronized ReturnTypeList getReturnList() {
		return returnList;
	}

	/**
	 * @param returnList the returnList to set
	 */
	public synchronized void setReturnList(ReturnTypeList returnList) {
		this.returnList = returnList;
	}

	/**
	 * @return the wfDef
	 */
	public synchronized byte[] getWfDef() {
		return wfDef;
	}

	/**
	 * @param wfDef the wfDef to set
	 */
	public synchronized void setWfDef(byte[] wfDef) {
		this.wfDef = wfDef;
	}

	public WFType getType() {
		return type;
	}

	public void setType(WFType type) {
		this.type = type;
	}

	public String getWFID() {
		return WFID;
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
