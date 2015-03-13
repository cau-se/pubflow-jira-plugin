package de.pubflow.server.services.ocn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.pubflow.server.PubFlowSystem;
import de.pubflow.server.common.properties.PropLoader;
import de.pubflow.server.core.jira.ComMap;
import de.pubflow.server.services.ocn.entity.Bottle;
import de.pubflow.server.services.ocn.entity.Leg;
import de.pubflow.server.services.ocn.entity.Parameter;
import de.pubflow.server.services.ocn.entity.Sample;
import de.pubflow.server.services.ocn.entity.abstractClass.PubJect;
import de.pubflow.server.services.ocn.exceptions.PubJectException;

/**
 * @author arl
 *
 */

public class PangaeaMapper {

	public static Map<String,String> foundMappings = new HashMap<String, String>();
	private StringBuilder log;

	public ComMap mapValues(ComMap data, int instanceId) throws Exception {
		long millis = System.currentTimeMillis();

		try{
			
			log = new StringBuilder();
			JAXBContext ctx = JAXBContext.newInstance(Leg.class);
			Unmarshaller um = ctx.createUnmarshaller();
			
			if(data.get("de.pubflow.services.ocn.PluginAllocator.getData.leg") == null){
				throw new IOException("Mapping failed due to an empty input string. Something went terribly wrong in a prior work step.");				
			}
			
			StringReader sr = new StringReader(data.get("de.pubflow.services.ocn.PluginAllocator.getData.leg"));			

			Leg leg = (Leg) um.unmarshal(sr);

			resolveDependency(leg);
			resolveFormats(leg);
		
			ArrayList<PubJect> bottles = leg.getList(Leg.BOTTLELIST);
			ArrayList<PubJect> bottlesSorted = sort(leg);
			bottles.clear();
			bottles.addAll(bottlesSorted);
			
			StringBuilder log = new StringBuilder();
			
			if(data.get("de.pubflow.services.ocn.PluginAllocator.getData.log") != null){
				log.append(data.get("de.pubflow.services.ocn.PluginAllocator.getData.log"));
			}
			
			Marshaller m = ctx.createMarshaller();
			StringWriter sw = new StringWriter();
			//m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(leg, sw);
			
			data.put("de.pubflow.services.ocn.PluginAllocator.convert.leg", sw.toString());
			data.put("de.pubflow.services.ocn.PluginAllocator.convert.log", log.toString());
			
			data.newJiraAttachment("interimOCNToPangaeaMapper.tmp", sw.toString().getBytes());
			data.newJiraComment(String.format("OCNToPangaeaMapper: exited normally after %f s.", (System.currentTimeMillis() - millis)/1000.0));

			return data;

		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("OCNToPangaeaMapper: " + e.getMessage() + " / " + e.getStackTrace().toString());
		}
	}

	private void resolveFormats(PubJect leg) throws PubJectException{

		List<PubJect> l = (List<PubJect>) leg.getList(Leg.PARAMETERLIST);
		Map<String, Integer> low = new HashMap<String, Integer>();
		Map<String, Integer> high = new HashMap<String, Integer>();

		for (PubJect p : l){
			low.put(p.getString(Parameter.UNITID), 0);
			high.put(p.getString(Parameter.UNITID), 0);
		}

		List<PubJect> bottles = (ArrayList<PubJect>) leg.getList(Leg.BOTTLELIST);
		for(PubJect a : bottles){
			List<PubJect> samples = a.getList(Bottle.SAMPLELIST);
			for(PubJect b : samples){
				String id = b.getString(Sample.PARAMETERUNITID);
				int currentValueLow = low.get(id);
				int currentValueHigh = high.get(id);

				StringTokenizer st = new StringTokenizer(b.getString(Sample.VAL), ".");

				int tmp = st.nextToken().length();
				if(tmp > currentValueHigh)
					high.put(id, tmp);	

				tmp = st.nextToken().length();
				if(tmp > currentValueLow)
					low.put(id, tmp);	
			}
		}

		HashMap<String, String> formats = new HashMap<String, String>();

		for(String s : low.keySet()){

			String result = "";

			for(int i = high.get(s)-1; i>0; i--)
				result += "#";

			result += "0.";

			for(int i = low.get(s)-1; i>=0; i--)
				result += "0";

			formats.put(s, result);
		}


		for(PubJect p : l){
			p.add(Parameter.FORMAT, formats.get(p.getString(Parameter.UNITID)));
		}
	}

	private ArrayList<PubJect> sort(PubJect leg) throws PubJectException{

		ArrayList<PubJect> resultList = new ArrayList<PubJect>();
		ArrayList<PubJect> tempList = new ArrayList<PubJect>();
		PubJect lastBottle = null;

		List<PubJect> bottles = ((Leg)leg).getList(Leg.BOTTLELIST);

		for(PubJect bottle : bottles){
			if(lastBottle == null){	
				lastBottle = bottle;	

			}else if(!bottle.getString(Bottle.STATION).equals(lastBottle.getString(Bottle.STATION))){
				//sortieren von klein nach groß

				boolean sortByPressure = false;

				quicksort(tempList, 0, tempList.size() - 1, sortByPressure);

				if(sortByPressure){
					Collections.reverse(tempList);
				}
				resultList.addAll(tempList);
				tempList = new ArrayList<PubJect>();
			}

			tempList.add(bottle);
			lastBottle = bottle;
		}
		return resultList;
	}

	private void quicksort(ArrayList<PubJect> bottles, int left, int right, boolean sortByPressure) throws PubJectException{

		if (left < right){
			int divide = divide(bottles, left, right, sortByPressure);
			quicksort(bottles, left, divide - 1, sortByPressure);
			quicksort(bottles, divide + 1, right, sortByPressure);
		}
	}

	private int divide(ArrayList<PubJect> bottles, int left, int right, boolean sortByPressure) throws PubJectException{
		int i = left;
		int j = right;
		PubJect pivot = bottles.get(right);

		double valueI = 0.0;
		double valueJ = 0.0;
		double valuePivot = 0.0;

		do{	
			if(sortByPressure){
				valueI = getPressure(bottles.get(i));
				valuePivot = getPressure(pivot);
				valueJ = getPressure(bottles.get(j));

			}else{
				valueI = Double.parseDouble(bottles.get(i).getString(Bottle.ID));
				valueJ = Double.parseDouble(bottles.get(j).getString(Bottle.ID));
				valuePivot = Double.parseDouble(pivot.getString(Bottle.ID));
			}

			while(valueI <= valuePivot && i < right){
				i = i + 1;
			}
			while(valueJ >= valuePivot && j > left){
				j = j - 1;
			}

			if(i < j){
				PubJect data_i = bottles.get(i);
				PubJect data_j = bottles.get(j);

				int index_i = bottles.indexOf(data_i);
				int index_j = bottles.indexOf(data_j);

				bottles.set(index_i, data_j);
				bottles.set(index_j, data_i);
			}

		}while(i < j);

		if(valueI > valuePivot){
			PubJect data_i = bottles.get(i);
			PubJect data_right = bottles.get(right);

			int index_i = bottles.indexOf(data_i);
			int index_right = bottles.indexOf(data_right);

			bottles.set(index_i, data_right);
			bottles.set(index_right, data_i);
		}

		return i;
	}

	private Double getPressure(PubJect bottle) throws PubJectException{

		ArrayList<PubJect> samples = bottle.getList(Bottle.SAMPLELIST);

		for(PubJect sample : samples){
			// pressure - ocn 10007 == pangaea 715 
			if(sample.getString(Sample.PARAMETERUNITID).equals("7")){
				return Double.parseDouble(sample.getString(Sample.VAL));
			}
		}
		return 0.0d;
	}

	private void resolveDependency(PubJect leg) throws PubJectException, IOException {

		FileReader input = new FileReader(PubFlowSystem.getInstance().pubflowHome + PropLoader.getInstance().getProperty("MappingFile", this.getClass(), "etc/PANGAEAParameterComplete.tab"));		

		BufferedReader bufRead = new BufferedReader(input);

		String line = bufRead.readLine();
		List<PubJect> searchList = new ArrayList<PubJect>();


		//Lege Liste mit allen Parametern an, die keine PangaeaId haben
		for(PubJect p : leg.getList(Leg.PARAMETERLIST)){
			if(p.getString(Parameter.PANGAEAID).equals("0"))
				searchList.add(p);
		}

		//Prüfe ob eine UnitId bereits gemappt und die zugehörige PangaeaId gespeichert wurde (foundMappings)
		for(PubJect p : searchList){
			if(foundMappings.containsKey(p.getString(Parameter.UNITID))){
				p.add( Parameter.PANGAEAID, foundMappings.get(p.getString(Parameter.UNITID)));
				log.append("Cached parameter found! '" + p.getString(Parameter.UNIT) + " " + p.getString(Parameter.NAME) + "' (id : " +  p.getString(Parameter.PANGAEAID) + ").\n");
			}
		}

		int counter = 0;

		while (line != null && counter < searchList.size()){

			StringTokenizer st = new StringTokenizer(line, "\t");
			Map<String, String> values = new HashMap<String, String>();

			values.put("Parameter", st.nextToken());				
			values.put("Abbreviation", st.nextToken());
			values.put("Unit", st.nextToken());
			values.put("ID parameter", st.nextToken());

			// Iteriere durch die Liste aller Parameter ohne PangaeaId
			for(PubJect p : searchList){

				//Vgl. ob Parameterbezeichnung oder Abkürzung in der Mappingtabelle und dem aktuellen Parameter übereinstimmen
				if(p.getString(Parameter.NAME).toLowerCase().equals(values.get("Parameter").toLowerCase()) ||
						p.getString(Parameter.ABBREVIATION).toLowerCase().equals(values.get("Abbreviation").toLowerCase())){

					//Vgl. Einheiten der Parameter, wenn ok, setze PangaeaId
					if(p.getString(Parameter.UNIT).equals(values.get("Unit"))){
						p.add( Parameter.PANGAEAID, values.get("ID parameter"));
						foundMappings.put(p.getString(Parameter.UNITID), values.get("ID parameter"));
						log.append("Parameter found! Mapped '" + p.getString(Parameter.UNIT) + " " + p.getString(Parameter.NAME) + "' on '" + 
								values.get("Unit") + " " + values.get("Parameter").toLowerCase() + "' with pangaea-id " + p.getString(Parameter.PANGAEAID) + ".\n");
						counter++;
					}
				}
			}
			line = bufRead.readLine();
		}
		bufRead.close();

		if(counter != searchList.size()){
			for(PubJect parameter : searchList){
				if(parameter.getString(Parameter.PANGAEAID).equals("0")){
					log.append("No mapping found for : " + parameter.getString(Parameter.DESCRIPTION) + " (" + parameter.getString(Parameter.ABBREVIATION) + ") in " + parameter.getString(Parameter.UNIT) + "\n");
				}
			}
		}
	}

}
