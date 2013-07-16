package de.cau.tf.ifi.se.pubflow.communication.message;

public abstract class Message {

	protected String clazz;
	protected String seperatorSeq = ":";
	
	public String getType() {
		return clazz;
	}
	
	public void setType(String c) {
		this.clazz = c;
	}
	
	public String getSeperatorSeq() {
		return seperatorSeq;
	}
	
	public void setSeperatorSeq(String seperatorSeq) {
		this.seperatorSeq = seperatorSeq;
	}
	
	public abstract String transformToString();
	public abstract void initFromString(String content);
	
}
