package de.pubflow.server.common.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.PubFlowSystem;
import de.pubflow.server.common.exceptions.PropertyNotSetException;

public class PropLoader {


	private static PropLoader propLoader;
	private static final String CONF_FILE = PubFlowSystem.getInstance().pubflowHome + "Pubflow.conf";
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

	public String getProperty(String key, Class clazz, String defaultValue) {
		String prop = pubflowConf.getProperty(clazz.getCanonicalName() + "-" + key);


		if ((prop == null) || (prop.equals(""))){
			myLogger.warn("Property " + key + " : " + clazz.getCanonicalName()  + " is not set!");
			myLogger.info("Property " + key + " : " + clazz.getCanonicalName()  + " has been set to: " + defaultValue);

			pubflowConf.setProperty(clazz.getCanonicalName()  + "-" + key, defaultValue);
			saveProperties();
			prop = defaultValue;
		}else{
			myLogger.info("Getting Property - " + key + " : " + clazz.getCanonicalName()  + " = " + prop);
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

	public void updateProperty(String key, Class clazz, String prop) throws PropertyNotSetException {
		myLogger.info("Updating Property - " + key + " : " + clazz.getCanonicalName());
		String temp = pubflowConf.getProperty(clazz.getCanonicalName() + "-" + key);
		if ((temp == null) || (temp.equals(""))){
			myLogger.warn("Property " + key + " : " + clazz.getCanonicalName() + " is empty!");
			throw new PropertyNotSetException();
		}
		pubflowConf.setProperty(clazz.getCanonicalName() + "-" + key, prop);
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
