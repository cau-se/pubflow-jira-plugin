package de.pubflow.communication.message;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;

import de.pubflow.communication.message.text.TextMessage;
import de.pubflow.communication.message.workflow.WorkflowMessage;

public class MessageToolbox {

	private static Logger myLogger = org.slf4j.LoggerFactory
			.getLogger(MessageToolbox.class);

	@SuppressWarnings("unchecked")
	public static <T extends Message> T loadFromString(String msgString,
			Class<? extends Message> msgType) {
		T result = null;

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(msgType);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(msgString);
			result= (T) unmarshaller.unmarshal(reader);
		} catch (Exception ex) {
			myLogger.error("Upps, wrong msg type specified");
			ex.printStackTrace();
		}
		return result;
	}

	public static String transformToString(Message msg) {
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		try {
			JAXBContext context = JAXBContext.newInstance(msg.getClass());

			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(msg, oStream);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return oStream.toString();
	}

	public static void main(String[] args) {
		TextMessage test = new TextMessage();
		test.setContent("This is a test");
		String msgAsString = MessageToolbox.transformToString(test);
		System.out.println(msgAsString);
		TextMessage nT = MessageToolbox.loadFromString(msgAsString, TextMessage.class);
		System.out.println(nT.getContent());
		
		WorkflowMessage wm = new WorkflowMessage();
		wm.setComments("TestWFMsg");
		String temp = MessageToolbox.transformToString(wm);
		System.out.println(temp);
		WorkflowMessage nwm = MessageToolbox.loadFromString(temp, WorkflowMessage.class);
		System.out.println(nwm.getComments());
	}
}
