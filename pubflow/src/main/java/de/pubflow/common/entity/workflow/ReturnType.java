package de.pubflow.common.entity.workflow;

public class ReturnType {
	
	private String clazzName;
	/**
	 * @return the clazzName
	 */
	public synchronized String getClazzName() {
		return clazzName;
	}
	/**
	 * @param clazzName the clazzName to set
	 */
	public synchronized void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}
	/**
	 * @return the varName
	 */
	public synchronized String getVarName() {
		return varName;
	}
	/**
	 * @param varName the varName to set
	 */
	public synchronized void setVarName(String varName) {
		this.varName = varName;
	}
	private String varName;

}
