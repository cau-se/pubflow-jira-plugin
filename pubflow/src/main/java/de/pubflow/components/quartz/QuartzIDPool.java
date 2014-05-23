package de.pubflow.components.quartz;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.exception.PropNotSetException;
import de.pubflow.common.properties.PropLoader;

public class QuartzIDPool {
	private static long l = 1;
	private static Logger myLogger;

	static{
		myLogger = LoggerFactory.getLogger(QuartzIDPool.class);	
		l = Long.parseLong(PropLoader.getInstance().getProperty("QuartzID", QuartzIDPool.class.toString(), l + ""));
		myLogger.debug("Setting QuartzID to IDPool value " + l);
	}

	public synchronized static long getUniqueID() throws IOException{

		l++;
		try {
			PropLoader.getInstance().updateProperty("QuartzID", QuartzIDPool.class.toString(), l + "");
			myLogger.debug("QuartzID increased to " + l);

		} catch (PropNotSetException e) {
			e.printStackTrace();
		}
		return l;
	}
}

