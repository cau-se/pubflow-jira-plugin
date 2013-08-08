package de.pubflow.wfCompUntis.ocn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.pubflow.common.properties.PropLoader;
import de.pubflow.wfCompUntis.ocn.entity.Bottle;
import de.pubflow.wfCompUntis.ocn.entity.Leg;
import de.pubflow.wfCompUntis.ocn.entity.Parameter;
import de.pubflow.wfCompUntis.ocn.entity.Sample;
import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;
import de.pubflow.wfCompUntis.ocn.exception.PubJectException;

public class OCNToPangaeaMapper {
	


	public static Map<String,String> foundMappings = new HashMap<String, String>();
	public StringBuilder log = new StringBuilder();

	public String replaceArtefacts(String input, int instanceId) throws Exception {
		
		
		try{
			JAXBContext ctx = JAXBContext.newInstance(Leg.class);
			Unmarshaller um = ctx.createUnmarshaller();
			StringReader sr = new StringReader(input);			

			Leg leg = (Leg) um.unmarshal(sr);

			resolveDependency(leg);
			resolveFormats(leg);
			leg.add(Leg.c_logString, leg.getString(Leg.c_logString) + log.toString());

			Marshaller m = ctx.createMarshaller();
			StringWriter sw = new StringWriter();
			//m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(leg, sw);

			return sw.toString();	

		}catch(Exception e){

			try {
				PrintStream fw = new PrintStream(PropLoader.getInstance().getProperty("TransformerExceptionFile", this.getClass().getCanonicalName(), "exceptionTransformer.txt"));
				e.printStackTrace();
				fw.append(e.toString());
				fw.append(e.getMessage());
				fw.append(e.getLocalizedMessage());
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			return "";
		}
	}

	private void resolveFormats(PubJect leg) throws PubJectException{


		List<PubJect> l = (List<PubJect>) leg.getList(Leg.c_parameterList);
		Map<String, Integer> low = new HashMap<String, Integer>();
		Map<String, Integer> high = new HashMap<String, Integer>();

		for (PubJect p : l){
			low.put(p.getString(Parameter.c_unitId), 0);
			high.put(p.getString(Parameter.c_unitId), 0);
		}

		List<PubJect> bottles = (ArrayList<PubJect>) leg.getList(Leg.c_bottleList);
		for(PubJect a : bottles){
			List<PubJect> samples = a.getList(Bottle.c_sampleList);
			for(PubJect b : samples){
				String id = b.getString(Sample.c_parameterUnitId);
				int currentValueLow = low.get(id);
				int currentValueHigh = high.get(id);

				StringTokenizer st = new StringTokenizer(b.getString(Sample.c_val), ".");

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
			p.add(Parameter.c_format, formats.get(p.getString(Parameter.c_unitId)));
		}
	}



	private void resolveDependency(PubJect leg) throws PubJectException, IOException {

		

		FileReader input = new FileReader(PropLoader.getInstance().getProperty("MappingFile", this.getClass().getCanonicalName(), "etc/PANGAEAParameterComplete.tab"));
		BufferedReader bufRead = new BufferedReader(input);

		String line = bufRead.readLine();
		List<PubJect> searchList = new ArrayList<PubJect>();


		//Lege Liste mit allen Parametern an, die keine PangaeaId haben
		for(PubJect p : leg.getList(Leg.c_parameterList)){
			if(p.getString(Parameter.c_pangaeaId).equals("0"))
				searchList.add(p);
		}

		//Prüfe ob eine UnitId bereits gemappt und die zugehörige PangaeaId gespeichert wurde (foundMappings)
		for(PubJect p : searchList){
			if(foundMappings.containsKey(p.getString(Parameter.c_unitId))){
				p.add( Parameter.c_pangaeaId, foundMappings.get(p.getString(Parameter.c_unitId)));
				log.append("Cached parameter found! Mapped '" + p.getString(Parameter.c_unit) + " on " + p.getString(Parameter.c_pangaeaId) + ".\n");
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
				if(p.getString(Parameter.c_name).toLowerCase().equals(values.get("Parameter").toLowerCase()) ||
						p.getString(Parameter.c_abbreviation).toLowerCase().equals(values.get("Abbreviation").toLowerCase())){

					//Vgl. Einheiten der Parameter, wenn ok, setze PangaeaId
					if(p.getString(Parameter.c_unit).equals(values.get("Unit"))){
						p.add( Parameter.c_pangaeaId, values.get("ID parameter"));
						foundMappings.put(p.getString(Parameter.c_unitId), values.get("ID parameter"));
						log.append("Parameter found! Mapped '" + p.getString(Parameter.c_unit) + " " + p.getString(Parameter.c_name) + "' on '" + 
								values.get("Unit") + " " + values.get("Parameter").toLowerCase() + "' with pangaea-id " + p.getString(Parameter.c_pangaeaId) + ".\n");
						counter++;
					}
				}
			}
			line = bufRead.readLine();
		}
		bufRead.close();

		if(counter != searchList.size()){
			log.append("WARNING: At least one ParameterId hasn't been mapped! You might have to check it manually.\n");
		}
	}


}
