/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.exceptions.PropertyNotSetException;

public class PropLoader {

	private static PropLoader propLoader;
	private static final String CONF_FILE = System.getProperty("pubflow_home", System.getenv("HOME") + "/pubflow_home/Pubflow.conf");
	
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
			e.printStackTrace();
		}

		try {
			properties.loadFromXML(fi);
		} catch (Exception e) {
			myLogger.error("Could not load Properties File");
			e.printStackTrace();
		}
		properties.list(System.out);
	}

	public String getProperty(String key, Class clazz) {
		System.out.println("name: "+clazz.getCanonicalName() );
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
