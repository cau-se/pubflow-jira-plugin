package de.pubflow.communication.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.entity.StringSerializable;

public abstract class Message implements StringSerializable{

	protected String clazz;
	protected static final String fieldSeperatorSeq = "|";
	protected static final String coreSeperatorSeq = "|";
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
	
	public static String getSeperatorSeq() {
		return coreSeperatorSeq;
	}
	
	public abstract boolean isValid();
	public abstract String transformToString();
	public abstract void initFromString(String content);
	
}
