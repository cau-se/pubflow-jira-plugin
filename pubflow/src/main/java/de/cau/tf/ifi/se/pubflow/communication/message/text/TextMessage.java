package de.cau.tf.ifi.se.pubflow.communication.message.text;

import de.cau.tf.ifi.se.pubflow.communication.message.Message;



public class TextMessage extends Message {

	private String content;
	
	public TextMessage()
	{
		super();
		clazz = this.getClass().toString();
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String transformToString() {
		String serial = clazz + seperatorSeq + content;
		return serial;
	}

	@Override
	public void initFromString(String content) {
		myLogger.info("Loading msg from String");
		myLogger.info("Msg. >> "+content);
		String[] seq = content.split(seperatorSeq);
		for (String string : seq) {
			myLogger.info("Msg.-Part >> "+string);
		}
		if(seq.length==2)
		{
			clazz = seq[0];
			setContent(content = seq[1]);
			System.out.println(content);
		}
		else{
			System.err.println(seq.length);
		}
	}

}
