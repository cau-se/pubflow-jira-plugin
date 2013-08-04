package de.pubflow.communication.message.text;

import de.pubflow.common.exception.MsgParsingException;
import de.pubflow.communication.message.Message;
import de.pubflow.communication.message.MessageToolbox;

public class TextMessage extends Message {

	private String content = "empty";

	public TextMessage() {
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
	public boolean isValid() {
		if (content == null)
			return false;
		return true;
	}

	@Override
	public void parseBody(String pContent) {

		content = pContent;
	}

}
