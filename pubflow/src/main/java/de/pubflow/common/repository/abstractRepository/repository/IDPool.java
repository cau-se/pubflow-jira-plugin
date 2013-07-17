package de.pubflow.common.repository.abstractRepository.repository;

import java.io.IOException;
import java.util.Properties;

import de.pubflow.PubFlowCore;
import de.pubflow.common.exception.PropAlreadySetException;
import de.pubflow.common.exception.PropNotSetException;
import de.pubflow.common.persistence.PersistenceProvider;

public class IDPool {
	private static long l = 100;
	private static volatile boolean isInit = false;

	private synchronized static void init() throws IOException{
		
		String counter = "";
		try {
			counter = PubFlowCore.getInstance().getProperty("ID", IDPool.class.toString());
		} catch (PropNotSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				

		if(counter == null){
			try {
				PubFlowCore.getInstance().setProperty("ID", IDPool.class.toString(), l + "");
			} catch (PropAlreadySetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}else{
			l = Long.parseLong(counter);
		}
		isInit = true;
	}

	public synchronized static long getUniqueID() throws IOException{
		if(isInit == false){
			init();
		}
		l++;
		try {
			PubFlowCore.getInstance().setProperty("ID", IDPool.class.toString(), l + "");
		} catch (PropAlreadySetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}
}

