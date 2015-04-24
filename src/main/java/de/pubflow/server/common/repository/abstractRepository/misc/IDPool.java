package de.pubflow.server.common.repository.abstractRepository.misc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.exceptions.PropertyNotSetException;
import de.pubflow.server.common.properties.PropLoader;

public class IDPool {
	private static final long DEFAULT_COUNTER = 100;
	
	
	private static long counter;
	private static Logger myLogger;

	static{
		myLogger = LoggerFactory.getLogger(IDPool.class);	
		counter = Long.parseLong(PropLoader.getInstance().getProperty("ID", IDPool.class, DEFAULT_COUNTER + ""));
		myLogger.debug("Setting ID to IDPool value " + counter);
	}

	public synchronized static long getUniqueID() throws IOException{

		counter++;
		try {
			PropLoader.getInstance().updateProperty("ID", IDPool.class, DEFAULT_COUNTER + "");
			myLogger.debug("ID increased to " + counter);

		} catch (PropertyNotSetException e) {
			e.printStackTrace();
		}
		return counter;
	}
}

