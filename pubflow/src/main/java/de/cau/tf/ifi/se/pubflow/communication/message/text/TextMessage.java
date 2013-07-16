package de.cau.tf.ifi.se.pubflow.communication.message.text;

import de.cau.tf.ifi.se.pubflow.communication.message.Message;



public class TextMessage extends Message {

	private String content;
	
	public TextMessage()
	{
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
		String[] seq = content.split(seperatorSeq);
		if(seq.length==2)
		{
			clazz = seq[0];
			content = seq[1];
		}
		else{
			System.err.println(seq.length);
		}
	}

}
