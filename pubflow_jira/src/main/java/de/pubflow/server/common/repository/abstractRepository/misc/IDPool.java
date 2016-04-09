package de.pubflow.server.common.repository.abstractRepository.misc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.PropLoader;

public class IDPool {
	private static long counter;
	private static Logger myLogger;

	static{
		myLogger = LoggerFactory.getLogger(IDPool.class);	
		counter = Long.parseLong(PropLoader.getInstance().getProperty("ID", IDPool.class));
		myLogger.debug("Setting ID to IDPool value " + counter);
	}

	public synchronized static long getUniqueID() throws IOException{
		counter++;
		PropLoader.getInstance().setProperty("ID", IDPool.class, counter + "");
		myLogger.debug("ID increased to " + counter);
		return counter;
	}
}

