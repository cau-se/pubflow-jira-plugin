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
package de.pubflow.server.assistance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

public class ConfWriterAssist {

	private static Properties pubflowConf;
	private static final String CONF_FILE = "Pubflow.conf";

	public static void main(String[] args) {
		FileOutputStream fs = null;
		FileInputStream fi = null;

		try {
			fi = new FileInputStream(CONF_FILE);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		pubflowConf = new Properties();

		try {
			pubflowConf.loadFromXML(fi);
			pubflowConf.list(System.out);

			fs = new FileOutputStream(CONF_FILE);

			pubflowConf.setProperty("PubFlowSystem-CONFSERVER", "ON");
			pubflowConf.storeToXML(fs,
					"PubFlow Properties File (last updated " + Calendar.getInstance().getTime().toString() + ")");

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				fi.close();
				fs.close();

			} catch (Exception e) {
			}
		}
	}
}
