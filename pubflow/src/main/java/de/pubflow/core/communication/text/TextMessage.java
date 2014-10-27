package de.pubflow.core.communication.text;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.pubflow.core.communication.Message;

@XmlRootElement(namespace = "http://pubflow.de/message/text")
public class TextMessage extends Message {

	private String content;


	@XmlElement(name="content")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public boolean isValid() {
		if (content == null)
			return false;
		return true;
	}


}
