package de.pubflow.core.communication.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement( namespace = "http://pubflow.de/message" )
public abstract class Message{

	
	public String clazz;
	protected static Logger myLogger;
	
	public Message()
	{
		myLogger = LoggerFactory.getLogger(this.getClass());
		clazz = this.getClass().getCanonicalName();
	}
	
	@XmlElement(name="clazz")
	public String getMsgType() {
		return clazz;
	}
	
	public void setType(String c) {
		this.clazz = c;
	}
	
	public void initFromString(String pContent)
	{
		
	}
	
	public abstract boolean isValid();
	

}
