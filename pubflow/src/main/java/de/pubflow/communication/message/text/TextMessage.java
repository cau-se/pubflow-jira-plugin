package de.pubflow.communication.message.text;


import de.pubflow.communication.message.Message;
import de.pubflow.communication.message.MessageToolbox;



public class TextMessage extends Message {

	private String content = "empty";
	
	public TextMessage()
	{
		super();
		clazz = this.getClass().getCanonicalName();
		
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String transformToString() {
		String serial = clazz + coreSeperatorSeq + content;
		return serial;
	}

	@Override
	public void initFromString(String content) {
		myLogger.info("Loading msg from String");
		myLogger.info("Checking Msg-Type");
		Class c = MessageToolbox.getMsgType(content);
		if (!c.equals(this.getClass()))
		{
			myLogger.error("Wrong msg-type");
			return;
		}
		myLogger.info("Correct type ("+c.getCanonicalName()+")");
		myLogger.info("Msg. >> "+content);
		String[] seq = content.split(coreSeperatorSeq);
		for (String string : seq) {
			myLogger.info("Msg.-Part >> "+string);
		}
		if(seq.length==2)
		{
			clazz = seq[0];
			
			myLogger.info(c.getCanonicalName());
			setContent(content = seq[1]);
		}
		else{
			System.err.println(seq.length);
		}
	}

	@Override
	public boolean isValid() {
		if(content==null)return false;
		return true;
	}

}
