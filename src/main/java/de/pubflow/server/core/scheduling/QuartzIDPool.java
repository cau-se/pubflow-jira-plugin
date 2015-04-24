package de.pubflow.server.core.scheduling;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.properties.PropLoader;

public class QuartzIDPool {
	private static final long DEFAULT_COUNTER = 100;

	private static long counter;
	private static Logger myLogger;

	static{
		myLogger = LoggerFactory.getLogger(QuartzIDPool.class);	
		counter = Long.parseLong(PropLoader.getInstance().getProperty("QuartzID", QuartzIDPool.class, DEFAULT_COUNTER + ""));
		myLogger.debug("Setting QuartzID to IDPool value " + counter);
	}

	public synchronized static long getUniqueID() throws IOException{

		counter++;
		try {
			PropLoader.getInstance().updateProperty("QuartzID", QuartzIDPool.class, DEFAULT_COUNTER + "");
			myLogger.debug("QuartzID increased to " + counter);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return counter;
	}
}

