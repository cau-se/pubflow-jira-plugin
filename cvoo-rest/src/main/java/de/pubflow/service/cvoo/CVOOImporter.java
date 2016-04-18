package de.pubflow.service.cvoo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CVOOImporter {

	public void cvooImport() throws IOException{

		HashMap<String, String> mappingTable = new HashMap<String, String>();
		mappingTable.put("osisid", "Metadata_EventID");
		mappingTable.put("o", "CTDOXY");
		mappingTable.put("t", "CTDTMP");
		mappingTable.put("s", "CTDSAL");
		mappingTable.put("sal_btl", "SALNTY");
		mappingTable.put("autosal", "SALNTY");
		mappingTable.put("p", "CTDPRS");
		mappingTable.put("lat", "Latitude");
		mappingTable.put("lon", "Longitude");
		mappingTable.put("btl", "BottleLabel");
		mappingTable.put("no3", "Nitrat");
		mappingTable.put("no3_qf", "Nitrat_flag");
		mappingTable.put("no2", "Nitrit");
		mappingTable.put("no2_qf", "Nitrit_flag");
		mappingTable.put("oxy_titr", "oxygen");
		mappingTable.put("oxy_titr_qf", "oxygen_flag");
		mappingTable.put("po4", "phspht");
		mappingTable.put("po4_qf", "phspht_flag");
		mappingTable.put("si", "silcat");
		mappingTable.put("si_qf", "silcat_flag");
		mappingTable.put("chl_filt", "chlora");
		mappingTable.put("chl_filt_qf", "chlora_flag");

	}

	public static void main(String [] args) throws IOException{
		HashMap<String, LinkedList<String>> parameters = new HashMap<String, LinkedList<String>>();

		FileInputStream fis = new FileInputStream(new File("/home/arl/Desktop/CVOO_ATA4_btl.txt"));

		int phase = 0;

		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		LinkedList<LinkedList<String>> valuesColumnY = new LinkedList<LinkedList<String>>();
		boolean parameterSectionEnded = false;

		while(br.ready()){
			String s = br.readLine();
			Pattern stupidCSVregex = Pattern.compile("(?:[@\\w|/.,()-:](?:[ ][@\\w|/.,()-:])*)+");
			Matcher parameterPatternMatcher = stupidCSVregex.matcher(s);

			if(s.startsWith("Columns")){
				parameterSectionEnded = true;
			}

			if(!parameterSectionEnded){
				parameterPatternMatcher.find();
				String key = parameterPatternMatcher.group(0);
				LinkedList<String> parameterValues = new LinkedList<String>();

				try{
					while(true){
						parameterPatternMatcher.find();
						parameterValues.add(parameterPatternMatcher.group(0));
					}
				}catch(Exception e){}

				parameters.put(key, parameterValues);
			}else{

				parameterPatternMatcher.find();
				LinkedList<String> valuesColumnX = new LinkedList<String>();
				try{
					while(true){
						parameterPatternMatcher.find();
						valuesColumnX.add(parameterPatternMatcher.group(0));
					}
				}catch(Exception e){}

				valuesColumnY.add(valuesColumnX);
			}
		}

		LinkedList<String> columns = new LinkedList<String>(); 
		Pattern stupidColumnsregex = Pattern.compile("(\\w+)+");
		Matcher columnsPatternMatcher = stupidColumnsregex.matcher(parameters.get("Columns").get(0));
		try{
			while(true){
				columnsPatternMatcher.find();
				columns.add(columnsPatternMatcher.group(0));
			}
		}catch(Exception e){}


		String referenceMetaData = "";
		for(Entry<String, LinkedList<String>> e :parameters.entrySet()){
			referenceMetaData += "#" + e.getKey();

			if(e.getValue().size() != 0){
				for(String s : e.getValue()){
					referenceMetaData += " " + s;
				}
			}
		}

		String query = String.format("insert into bottleocn.leg " +
				"(name, expocode, workflow_realisation_workstep_id, id, reference, mleg_id, who) " +
				"values (%s, %s, %s, %s, %s, %s, %s)", 
				parameters.get("Cruise"), parameters.get("Expocode"), 
				parameters.get("workflow_realisation_workstep_id?"),parameters.get("osisid?"), 
				referenceMetaData, parameters.get("osisid?"), "CVOO");

		for(int i = 0; i < valuesColumnY.size(); i++){
			LinkedList<String> sample = valuesColumnY.get(i);

			for(int j = 0; j < columns.size(); j++){		
				String column = columns.get(i);

				if(!column.endsWith("qf")){
					int qfIndex = columns.indexOf(column + "_qf");

					String querySample = String.format("insert into bottleocn.sample " +
							"(id, val, parameter_id, flag, bottle_id) " +
							"values (%s, %s, %s, %s, %s)",
							"?", "", "", "", "");

				}
			}
		}
	}
}
