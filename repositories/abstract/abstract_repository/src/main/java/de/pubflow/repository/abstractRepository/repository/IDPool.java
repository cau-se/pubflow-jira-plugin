package de.pubflow.repository.abstractRepository.repository;

import java.io.IOException;
import java.util.Properties;

import de.pubflow.PropLoader;

public class IDPool {
	private static long l = 100;
	private static Properties props;

	private static void init() throws IOException{
		props = PropLoader.loadProperties();
		String counter = props.getProperty("ID");

		if(counter == null){
			props.setProperty("ID", l + "");
			PropLoader.saveProperties(props);

		}else{
			l = Long.parseLong(counter);
		}
	}

	public static long getUniqueID() throws IOException{
		if(props == null){
			init();
		}
		l++;
		props.setProperty("ID", l + "");
		PropLoader.saveProperties(props);
		return l;
	}
}

