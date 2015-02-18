package de.pubflow.server.common.entity.workflow;

import de.pubflow.server.common.enumeration.WFType;

public class PubFlow {

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
		return super.equals(obj);
	}
}
