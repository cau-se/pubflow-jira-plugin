package de.pubflow.common.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.exception.PropNotSetException;

public class PropLoader {


	private static PropLoader propLoader;
	private static final String CONF_FILE = "Pubflow.conf";
	private Logger myLogger = LoggerFactory.getLogger(this.getClass());
	private Properties pubflowConf = new Properties();

	private PropLoader(){	
		initProp();
	}

	public static PropLoader getInstance(){
		if(propLoader == null){
			propLoader = new PropLoader();
		}
		return propLoader;
	}

	private void initProp(){
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(CONF_FILE);
		} catch (Exception e) {
			myLogger.error("Could not find Properties File");

			// e.printStackTrace();
		}

		try {
			pubflowConf.loadFromXML(fi);
		} catch (Exception e) {
			myLogger.error("Could not load Properties File");
			//e.printStackTrace();
		}
		pubflowConf.list(System.out);

	}

	public String getProperty(String key, String calleeSig, String defaultValue) {
		myLogger.info("Getting Property - " + key + " : " + calleeSig);
		String prop = pubflowConf.getProperty(calleeSig + "-" + key);

		if ((prop == null) || (prop.equals(""))){
			myLogger.warn("Property " + key + " : " + calleeSig + " is not set!");
			myLogger.info("Property " + key + " : " + calleeSig + " has been set to: " + defaultValue);

			pubflowConf.setProperty(calleeSig + "-" + key, defaultValue);
			saveProperties();
			prop = defaultValue;
		}
		return prop;
	}

	//	public void setProperty(String key, String calleeSig, String prop) throws PropAlreadySetException {
	//		myLogger.info("Setting Property - " + key + " : " + calleeSig);
	//		String temp = pubflowConf.getProperty(calleeSig + "-" + key);
	//		if (temp != null){
	//			myLogger.warn("Property " + key + " : " + calleeSig + " was already set!");
	//			throw new PropAlreadySetException();
	//		}
	//		pubflowConf.setProperty(calleeSig + "-" + key, prop);
	//	}

	public void updateProperty(String key, String calleeSig, String prop) throws PropNotSetException {
		myLogger.info("Updating Property - " + key + " : " + calleeSig);
		String temp = pubflowConf.getProperty(calleeSig + "-" + key);
		if ((temp == null) || (temp.equals(""))){
			myLogger.warn("Property " + key + " : " + calleeSig + " is empty!");
			throw new PropNotSetException();
		}
		pubflowConf.setProperty(calleeSig + "-" + key, prop);
		saveProperties();
	}

	public void saveProperties() {
		myLogger.info("Persisting Properties");
		FileOutputStream fs = null;
		try {
			fs = new FileOutputStream(CONF_FILE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			pubflowConf.storeToXML(fs, "PubFlow Properties File (last updated "
					+ Calendar.getInstance().getTime().toLocaleString() + ")");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
