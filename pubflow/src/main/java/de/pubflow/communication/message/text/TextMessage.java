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
	public void initFromString(String envelope) {
		myLogger.info("Loading msg from String: " + envelope);
		myLogger.info("Checking Msg-Type");
		if (!MessageToolbox.checkType(this.getClass(), envelope))
		{
			myLogger.error("Wrong msg-type");
			return;
		}

		try {
			clazz = getmsgPart(envelope, Message.MsgPart.HEADER);
			setContent(getmsgPart(envelope, Message.MsgPart.BODY));
		} catch (MsgParsingException e) {
			myLogger.error("An exception occurred while parsing this msg.");
			e.printStackTrace();
		}

		

	}

	@Override
	public boolean isValid() {
		if (content == null)
			return false;
		return true;
	}

}
