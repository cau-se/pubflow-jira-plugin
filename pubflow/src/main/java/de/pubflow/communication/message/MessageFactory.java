package de.pubflow.communication.message;


public class MessageFactory {
	
	public static Class<?> getMsgType(String msg)
	{
		Class<?> currentClazz = null;
		String[] msgParts = msg.split(Message.getSeperatorSeq());
		String clazzName = msgParts[0];
		
		try {
			currentClazz = Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currentClazz;
	}

}
