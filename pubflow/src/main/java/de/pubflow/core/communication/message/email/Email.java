package de.pubflow.core.communication.message.email;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.pubflow.core.communication.message.Message;

@XmlRootElement(namespace = "http://pubflow.de/message/email")
public class Email extends Message {

	private String recipient;
	private String topic;
	private String text;
	
	/**
	 * @return the recipient
	 */
	@XmlElement(name="Recipient")
	public synchronized String getRecipient() {
		return recipient;
	}

	/**
	 * @param recipient the recipient to set
	 */
	public synchronized void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	/**
	 * @return the topic
	 */
	@XmlElement(name="Topic")
	public synchronized String getTopic() {
		return topic;
	}

	/**
	 * @param topic the topic to set
	 */
	public synchronized void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @return the text
	 */
	@XmlElement(name="Text")
	public synchronized String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public synchronized void setText(String text) {
		this.text = text;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

}
