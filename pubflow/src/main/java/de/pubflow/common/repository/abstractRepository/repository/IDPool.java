package de.pubflow.common.repository.abstractRepository.repository;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.PubFlowCore;
import de.pubflow.common.exception.PropAlreadySetException;
import de.pubflow.common.exception.PropNotSetException;

public class IDPool {
	private static long l = 100;
	private static volatile boolean isInit = false;
	private static Logger myLogger;

	static{
		myLogger = LoggerFactory.getLogger(IDPool.class);
	}

	private synchronized static void init() throws IOException{

		try {
			myLogger.debug("Get current ID");
			l = Long.parseLong(PubFlowCore.getInstance().getProperty("ID", IDPool.class.toString()));

		} catch (PropNotSetException e) {
			try {
				myLogger.debug("Setting ID to initial value " + l);
				PubFlowCore.getInstance().setProperty("ID", IDPool.class.toString(), l + "");
		
			} catch (PropAlreadySetException e1) {
				//Impossibru!
			}
		}
		isInit = true;
	}

	public synchronized static long getUniqueID() throws IOException{
		if(isInit == false){
			init();
		}
		
		l++;

		try {
			PubFlowCore.getInstance().updateProperty("ID", IDPool.class.toString(), l + "");
			myLogger.debug("ID increased to " + l);

		} catch (PropNotSetException e) {
			//Impossibru!
		}
		return l;
	}
}

