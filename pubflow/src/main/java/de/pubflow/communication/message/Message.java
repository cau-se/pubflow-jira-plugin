package de.pubflow.communication.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Message {

	protected String clazz;
	protected String seperatorSeq = ":";
	protected Logger myLogger;
	
	public Message()
	{
		myLogger = LoggerFactory.getLogger(this.getClass());
	}
	
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
