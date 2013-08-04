package de.pubflow.communication.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.entity.StringSerializable;
import de.pubflow.common.exception.MsgParsingException;

public abstract class Message implements StringSerializable{

	protected String clazz;
	protected static final String fieldSeperatorSeq = "|";
	protected static final String coreSeperatorSeq = "§";
	protected static Logger myLogger;
	
	public Message()
	{
		myLogger = LoggerFactory.getLogger(this.getClass());
		clazz = this.getClass().getCanonicalName();
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
	
	public static String getmsgPart(String msg, MsgPart part) throws MsgParsingException
	{
		myLogger.debug("Parsing Msg");
		String result = "";
		String[] temp = msg.split(coreSeperatorSeq);
		if (temp==null|temp.length!=2)
		{
			myLogger.error("Unknown msg-format");
			throw new MsgParsingException();
		}
		else
		{
			if(part.equals(MsgPart.BODY))
			{
				result=temp[1];
			}
			else if (part.equals(MsgPart.HEADER))
			{
				result=temp[0];
			}
			else
			{
				myLogger.error("How on earth could this happen?!?!");
				throw new MsgParsingException();
			}
		}
		myLogger.debug("msg parsed sucessfuly");
		return result;
	}
	
	public void initFromString(String pContent)
	{
		myLogger.info("Loading msg from String: " + pContent);
		myLogger.info("Checking Msg-Type");
		if (!MessageToolbox.checkType(this.getClass(), pContent))
		{
			myLogger.error("Wrong msg-type");
			return;
		}

		try {
			clazz = getmsgPart(pContent, Message.MsgPart.HEADER);
			parseBody(getmsgPart(pContent, Message.MsgPart.BODY));
		} catch (MsgParsingException e) {
			myLogger.error("An exception occurred while parsing this msg.");
			e.printStackTrace();
		}
	}
	
	public abstract boolean isValid();
	public abstract String transformToString();
	
	public abstract void parseBody(String pContent);
	
	public enum MsgPart
	{
		HEADER,
		BODY
	}
	
}
