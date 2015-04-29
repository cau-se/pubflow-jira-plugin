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
	private Properties properties = new Properties();

	private PropLoader() {	
		initProp();
	}

	public static PropLoader getInstance() {
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
			properties.loadFromXML(fi);
		} catch (Exception e) {
			myLogger.error("Could not load Properties File");
			//e.printStackTrace();
		}
		properties.list(System.out);
	}

	public String getProperty(String key, Class clazz) {
		String prop = properties.getProperty(clazz.getCanonicalName() + "#" + key);

		if ((prop == null) || (prop.equals(""))){
			try {
				prop = getDefaultProperty(key, clazz);
			} catch (PropertyNotSetException e) {
				prop = "";
			}
		}else{
//			myLogger.info("Getting property - " + clazz.getCanonicalName() + "#" + key + " = " + prop);
		}
		return prop;
	}

	public String getDefaultProperty(String key, Class clazz) throws PropertyNotSetException{
		String prop = properties.getProperty(clazz.getCanonicalName() + "#DEFAULT-" + key);
		if ((prop == null) || (prop.equals(""))){
			myLogger.warn("Property " + clazz.getCanonicalName() + "#" + key  + " doesn't have a default value!");
			throw new PropertyNotSetException();
		}else{
//			myLogger.info("Getting default property - " + clazz.getCanonicalName() + "#" + key + " = " + prop);
			return prop;
		}
	}

	public void setProperty(String key, Class clazz, String prop){
//		myLogger.info("Updating property - " + clazz.getCanonicalName() + "#" + key);
		properties.setProperty(clazz.getCanonicalName() + "#" + key, prop);
		saveProperties();
	}

	public void saveProperties() {
		myLogger.info("Persisting properties");
		FileOutputStream fs = null;

		try {
			fs = new FileOutputStream(CONF_FILE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			properties.storeToXML(fs, "PubFlow properties file (last updated "
					+ Calendar.getInstance().getTime().toLocaleString() + ")");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
