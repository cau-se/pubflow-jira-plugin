package de.pubflow.communication.message;

import de.pubflow.common.entity.StringSerializable;


public class MessageToolbox {
	
	
	public String transformObjectToMessageString(Object o )
	{
		String result ="empty";
		if (o!=null)
		{
			if(o instanceof StringSerializable)
			{
				result = ((StringSerializable) o).transformToString();
			}
			
			else if(o.getClass().isEnum())
			{
				
			}
		}
		return result;
	}
	
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
