package de.pubflow.assistance;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import de.pubflow.PubFlowSystem;


public class ConfWriterAssist {

	private static Properties pubflowConf;
	private static final String CONF_FILE = "Pubflow.conf";

	public static void main(String[] args) {
		FileOutputStream fs = null;
		FileInputStream fi = null;

		try {
			fi = new FileInputStream(CONF_FILE);

		}catch (FileNotFoundException e){
			e.printStackTrace();
		}

		pubflowConf = new Properties();

		try {
			pubflowConf.loadFromXML(fi);
			pubflowConf.list(System.out);

			fs = new FileOutputStream(CONF_FILE);

			String signiture = PubFlowSystem.class.toString();

			pubflowConf.setProperty( signiture+"-CONFSERVER","ON" );
			pubflowConf.storeToXML( fs, "PubFlow Properties File (last updated "+Calendar.getInstance().getTime().toLocaleString()+")");

		}catch ( IOException e ){
			e.printStackTrace();

		}finally{
			try { 
				fi.close(); 
				fs.close();

			} catch ( Exception e ){ 
			}
		}
	}
}
