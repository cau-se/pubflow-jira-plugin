package de.pubflow.service.ocn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.pubflow.common.DataContainer;
import de.pubflow.common.PropLoader;
import de.pubflow.service.ocn.entity.Bottle;
import de.pubflow.service.ocn.entity.Leg;
import de.pubflow.service.ocn.entity.Parameter;
import de.pubflow.service.ocn.entity.Sample;
import de.pubflow.service.ocn.entity.abstractClass.LBPSContainer;
import de.pubflow.service.ocn.exceptions.PubJectException;

/**
 * @author arl
 *
 */

public class PangaeaMapper {	
	public static Map<String,String> foundMappings = new HashMap<String, String>();
	private StringBuilder log = new StringBuilder();

	public DataContainer mapValues(DataContainer data, int instanceId) throws Exception {
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

			if(data.get("de.pubflow.services.ocn.PluginAllocator.getData.log") != null){
				log.append(data.get("de.pubflow.services.ocn.PluginAllocator.getData.log"));
			}

			//map parameter ids
			resolveDependency(leg);
			//resolve min and max values, create mask
			resolveFormats(leg);

			ArrayList<LBPSContainer> bottleList = leg.getList(Leg.BOTTLELIST);
			ArrayList<LBPSContainer> bottlesSorted = sort(leg);
			bottleList.clear();
			bottleList.addAll(bottlesSorted);

			Marshaller marshaller = ctx.createMarshaller();
			StringWriter legSw = new StringWriter();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(leg, legSw);

			data.put("de.pubflow.services.ocn.PluginAllocator.convert.leg", legSw.toString());
			data.put("de.pubflow.services.ocn.PluginAllocator.convert.log", log.toString());
			data.newJiraAttachment("debug_" + "de.pubflow.services.ocn.PluginAllocator.convert.leg", legSw.toString().getBytes());

			data.newJiraComment(String.format("OCNToPangaeaMapper: exited normally after %f s.", (System.currentTimeMillis() - millis)/1000.0));

			return data;
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("OCNToPangaeaMapper: " + e.getMessage() + " / " + e.getStackTrace().toString());
		}
	}

	private void resolveFormats(LBPSContainer leg) throws PubJectException{

		List<LBPSContainer> parameterList = (List<LBPSContainer>) leg.getList(Leg.PARAMETERLIST);
		Map<String, Integer> low = new HashMap<String, Integer>();
		Map<String, Integer> high = new HashMap<String, Integer>();

		for (LBPSContainer parameter : parameterList){
			low.put(parameter.getString(Parameter.UNITID), 0);
			high.put(parameter.getString(Parameter.UNITID), 0);
		}

		List<LBPSContainer> bottleList = (ArrayList<LBPSContainer>) leg.getList(Leg.BOTTLELIST);

		for(LBPSContainer bottle : bottleList){
			List<LBPSContainer> samples = bottle.getList(Bottle.SAMPLELIST);
			for(LBPSContainer sample : samples){
				String id = sample.getString(Sample.PARAMETERUNITID);
				int currentValueLow = low.get(id);
				int currentValueHigh = high.get(id);

				StringTokenizer st = new StringTokenizer(sample.getString(Sample.VAL), ".");

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


		for(LBPSContainer parameter : parameterList){
			parameter.add(Parameter.FORMAT, formats.get(parameter.getString(Parameter.UNITID)));
		}
	}

	private ArrayList<LBPSContainer> sort(LBPSContainer leg) throws PubJectException{
		boolean neverSorted = true;
		ArrayList<LBPSContainer> resultList = new ArrayList<LBPSContainer>();
		ArrayList<LBPSContainer> tempList = new ArrayList<LBPSContainer>();
		LBPSContainer lastBottle = null;

		List<LBPSContainer> bottleList = ((Leg)leg).getList(Leg.BOTTLELIST);
		System.out.println(bottleList.size());

		Iterator<LBPSContainer> bottleIterator = bottleList.iterator();
		while(bottleIterator.hasNext()){
			LBPSContainer bottle = bottleIterator.next();

			if(lastBottle != null){	
				if(!bottleIterator.hasNext()){
					tempList.add(bottle);
				}
				
				if(!bottle.getString(Bottle.STATION).equals(lastBottle.getString(Bottle.STATION)) || !bottleIterator.hasNext()){
					//sortieren von klein nach groß

					neverSorted = false;
					boolean sortByPressure = false;
					quicksort(tempList, 0, tempList.size() - 1, sortByPressure);
					System.out.println(tempList.size());
					System.out.println(lastBottle.getString(Bottle.STATION));

					if(sortByPressure){
						Collections.reverse(tempList);
					}

					resultList.addAll(tempList);
					tempList = new ArrayList<LBPSContainer>();
				}
			}

			if(bottleIterator.hasNext()){
				tempList.add(bottle);
				lastBottle = bottle;
			}			
		}

		if(neverSorted){
			return tempList;
		}else{
			return resultList;
		}
	}

	private void quicksort(ArrayList<LBPSContainer> bottleList, int left, int right, boolean sortByPressure) throws PubJectException{

		if (left < right){
			int divide = divide(bottleList, left, right, sortByPressure);
			quicksort(bottleList, left, divide - 1, sortByPressure);
			quicksort(bottleList, divide + 1, right, sortByPressure);
		}
	}

	private int divide(ArrayList<LBPSContainer> bottleList, int left, int right, boolean sortByPressure) throws PubJectException{
		int i = left;
		int j = right;
		LBPSContainer pivot = bottleList.get(right);

		double valueI = 0.0;
		double valueJ = 0.0;
		double valuePivot = 0.0;

		do{	
			if(sortByPressure){
				valueI = getPressure(bottleList.get(i));
				valuePivot = getPressure(pivot);
				valueJ = getPressure(bottleList.get(j));

			}else{
				valueI = Double.parseDouble(bottleList.get(i).getString(Bottle.ID));
				valueJ = Double.parseDouble(bottleList.get(j).getString(Bottle.ID));
				valuePivot = Double.parseDouble(pivot.getString(Bottle.ID));
			}

			while(valueI <= valuePivot && i < right){
				i = i + 1;
			}
			while(valueJ >= valuePivot && j > left){
				j = j - 1;
			}

			if(i < j){
				LBPSContainer data_i = bottleList.get(i);
				LBPSContainer data_j = bottleList.get(j);

				int index_i = bottleList.indexOf(data_i);
				int index_j = bottleList.indexOf(data_j);

				bottleList.set(index_i, data_j);
				bottleList.set(index_j, data_i);
			}

		}while(i < j);

		if(valueI > valuePivot){
			LBPSContainer data_i = bottleList.get(i);
			LBPSContainer data_right = bottleList.get(right);

			int index_i = bottleList.indexOf(data_i);
			int index_right = bottleList.indexOf(data_right);

			bottleList.set(index_i, data_right);
			bottleList.set(index_right, data_i);
		}

		return i;
	}

	private Double getPressure(LBPSContainer bottle) throws PubJectException{

		ArrayList<LBPSContainer> samples = bottle.getList(Bottle.SAMPLELIST);

		for(LBPSContainer sample : samples){
			// pressure - ocn 10007 == pangaea 715 
			if(sample.getString(Sample.PARAMETERUNITID).equals("7")){
				return Double.parseDouble(sample.getString(Sample.VAL));
			}
		}
		return 0.0d;
	}

	private void resolveDependency(LBPSContainer leg) throws PubJectException, IOException {

		FileReader input = new FileReader(PropLoader.getInstance().getProperty("PANGAEAMAPPINGFILE", this.getClass()));		

		BufferedReader bufRead = new BufferedReader(input);

		String line = bufRead.readLine();
		List<LBPSContainer> searchList = new ArrayList<LBPSContainer>();


		//Lege Liste mit allen Parametern an, die keine PangaeaId haben
		for(LBPSContainer p : leg.getList(Leg.PARAMETERLIST)){
			if(p.getString(Parameter.PANGAEAID).equals("0")){
				searchList.add(p);
			}
		}

		//Prüfe ob eine UnitId bereits gemappt und die zugehörige PangaeaId gespeichert wurde (foundMappings)
		for(LBPSContainer p : searchList){
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
			for(LBPSContainer p : searchList){

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
			for(LBPSContainer parameter : searchList){
				if(parameter.getString(Parameter.PANGAEAID).equals("0")){
					log.append("No mapping found for : " + parameter.getString(Parameter.DESCRIPTION) + " (" + parameter.getString(Parameter.ABBREVIATION) + ") in " + parameter.getString(Parameter.UNIT) + "\n");
				}
			}
		}
	}

}
