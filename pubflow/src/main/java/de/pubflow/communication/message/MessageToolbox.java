package de.pubflow.communication.message;

import org.slf4j.Logger;

import de.pubflow.common.entity.StringSerializable;
import de.pubflow.common.exception.StringTransformationException;


public class MessageToolbox {
	
	private static Logger myLogger = org.slf4j.LoggerFactory.getLogger(MessageToolbox.class);
	
	
	public static String transformObjectToMessageString(Object o, boolean optionalParameter) throws StringTransformationException 
	{
		myLogger.debug("Transforming msg String");
		String result ="empty";
		if (o!=null)
		{
			if(o instanceof StringSerializable)
			{
				result = ((StringSerializable) o).transformToString();
			}
			else if (o.getClass().equals(String.class))
			{
				if(((String)o).length()==0)
				{
					return result;
				}
				result = (String)o;
			}
			else if(o.getClass().isEnum())
			{
				result = o.toString();
			}
			else
			{
				try
				{
					result = o.toString();
				}
				catch (Exception e)
				{
					if(optionalParameter)
					{
						myLogger.warn("Parameter could not be transformed to msg String: "+o.toString());
					}
					else{
						myLogger.error("Parameter could not be transformed to msg String: "+o.toString());
						throw new StringTransformationException();
					}
				}
				
			}
			myLogger.debug("Transformation ended");
		}
		else
		{
			myLogger.debug("Object was null!");
		}
		return result;
	}
	
	public static boolean checkType(Class<?> expectedType, String msg)
	{
		myLogger.debug("checking msg type ...");
		Class c = getMsgType(msg);
		if (c.equals(expectedType))
		{
			myLogger.debug("check passed");
			return true;
		}
		myLogger.debug("check NOT passed");
		return false;
	}
	
	public static Class<?> getMsgType(String msg)
	{
		Class<?> currentClazz = null;
		String[] msgParts = msg.split(Message.getSeperatorSeq());
		String clazzName = msgParts[0];		
		myLogger.debug("Looking type up: "+clazzName);
		try {
			currentClazz = Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			myLogger.error("This class could not be mapped: "+clazzName);
			e.printStackTrace();
		}
		return currentClazz;
	}
}
