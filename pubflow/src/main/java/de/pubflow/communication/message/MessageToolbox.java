package de.pubflow.communication.message;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.slf4j.Logger;

import de.pubflow.common.entity.StringSerializable;
import de.pubflow.common.exception.StringTransformationException;
import de.pubflow.communication.message.text.TextMessage;


public class MessageToolbox {
	
	private static Logger myLogger = org.slf4j.LoggerFactory.getLogger(MessageToolbox.class);
	
	
	public static Message loadFromString(String msgString)
	{
		Message result = null;
		
		
		
		return result;
		
	}
	
	public static String transformToString(Message msg)
	{
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		try {
			JAXBContext context = JAXBContext.newInstance( msg.getClass() );
			
			Marshaller m = context.createMarshaller();
			m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			m.marshal( msg, oStream);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return oStream.toString();
	} 
	
	public static void main(String[] args) {
		TextMessage test = new TextMessage();
		test.setContent("This is a test");
		System.out.println(transformToString(test));
	}
}
