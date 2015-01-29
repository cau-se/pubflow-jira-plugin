package de.pubflow.server.services.ocn;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import de.pubflow.server.core.jira.ComMap;
import de.pubflow.server.core.jira.Entity.JiraAttachment;
import de.pubflow.server.services.ocn.entity.Bottle;
import de.pubflow.server.services.ocn.entity.Leg;
import de.pubflow.server.services.ocn.entity.Parameter;
import de.pubflow.server.services.ocn.entity.Sample;
import de.pubflow.server.services.ocn.entity.abstractClass.PubJect;


/**
 * @author arl
 *
 */

public class FileCreator4D {

	public ComMap toCSV(ComMap input, int instanceId) throws Exception{

		long millis = System.currentTimeMillis();

		try{
			input.flushData();

			if(input.get("return") == null){
				throw new IOException("4d file creation failed due to an empty input string. Something went terribly wrong in a prior work step.");				
			}

			StringBuilder log = new StringBuilder();

			if(input.get("de.pubflow.services.ocn.PluginAllocator.convert.log") != null){
				log.append((String)input.get("de.pubflow.services.ocn.PluginAllocator.convert.log"));
			}

			JAXBContext ctx = JAXBContext.newInstance(Leg.class);

			Unmarshaller um = ctx.createUnmarshaller();
			StringReader sr = new StringReader((String)input.get("de.pubflow.services.ocn.PluginAllocator.convert.leg"));

			Leg leg = (Leg) um.unmarshal(sr);

			String title = "Hydrochemistry during  cruise " + leg.getString("leg_name");
			String event = leg.getString("leg_name") + "-" + leg.getList(Leg.BOTTLELIST).get(0).getString(Bottle.STATION);


			//METADATEN

			StringBuilder result4dBuilder = new StringBuilder();
			result4dBuilder.append("/* DATA DESCRIPTION:").append("\r");
			
			String author = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.author");
			String source = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.source");
			String reference = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.reference");
			String fileName = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.fileName");
			String pid = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.pid");
			String comment = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.comment");
			String project = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.project");
			String topology = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.topology");
			String status = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.status");
			String login = input.get("de.pubflow.services.ocn.PluginAllocator.toCSV.login");


			if(!author.equals("")){
				result4dBuilder.append("Author:\t").append(author).append("\r");
			}else{
				log.append("Author was not set, skipping... \n");
			}

			if(!source.equals("")){
				result4dBuilder.append("Source:\t").append(source).append("\r");
			}else{
				log.append("Source was not set, skipping... \n");
			}

			if(!title.equals("")){
				result4dBuilder.append("Title:\t").append(title).append("\r");
			}else{
				log.append("Title was not set, skipping... \n");
			}

			if(!reference.equals("")){
				result4dBuilder.append("Reference:\t").append(reference).append("\r");
			}else{
				log.append("Reference was not set, skipping... \n");
			}

			if(!fileName.equals("")){
				result4dBuilder.append("Export Filename:\t").append(fileName).append("\r");
			}else{
				log.append("Export Filename was not set, skipping... \n");
			}

			result4dBuilder.append("Event:\t").append(event.replace(" ", "")).append("\r");
			result4dBuilder.append("PI:\t").append(pid).append("\r");
			result4dBuilder.append("Parameter:\t");

			result4dBuilder.append("790").append(" * ")
			.append("PI: ").append(pid).append(" * ")
			.append("METHOD: 43").append(" * ")
			.append("FORMAT: #####0").append("\r");

			for(PubJect p : leg.getList("parameterList")){
				result4dBuilder.append(p.getString(Parameter.PANGAEAID)).append(" * ")
				.append("PI: ").append(pid).append(" * ")
				.append("METHOD: ").append(p.getString(Parameter.METHODID)).append(" * ")
				.append("FORMAT: ").append(p.getString(Parameter.FORMAT)).append(" * ")
				.append(" COMMENT: ").append(p.getString(Parameter.ABBREVIATION)).append(" - ").append(p.getString(Parameter.COMMENT)).append("\r");
			}

			if(!comment.equals("")){
				result4dBuilder.append("Comment:\t").append(comment).append("\r");
			}else{
				log.append("Comment was not set, skipping... \n");
			}

			if(!project.equals("")){
				result4dBuilder.append("Project:\t").append(project).append("\r");
			}else{
				log.append("Project was not set, skipping... \n");
			}

			if(!topology.equals("")){
				result4dBuilder.append("Topologic Type:\t").append(topology).append("\r");
			}else{
				log.append("Topologic type was not set, skipping... \n");
			}

			if(!status.equals("")){
				result4dBuilder.append("Status:\t").append(status).append("\r");
			}else{
				log.append("Status was not set, skipping... \n");
			}

			if(!login.equals("")){
				result4dBuilder.append("Login:\t").append(login).append("\r");
			}else{
				log.append("Login was not set, skipping... \n");
			}

			result4dBuilder.append("*/").append("\r");

			//DATEN

			result4dBuilder.append("Event label\t790");

			for(PubJect p : leg.getList(Leg.PARAMETERLIST))
				result4dBuilder.append("\t").append(p.getString(Parameter.PANGAEAID));

			result4dBuilder.append("\r");


			log.append("\n");

			String tmp_stationName = "";
			for(PubJect b : leg.getList(Leg.BOTTLELIST)){

				if(!tmp_stationName.equals(b.getString(Bottle.STATION)))
					result4dBuilder.append(leg.getString(Leg.NAME)+ "-" + b.getString(Bottle.STATION).replace(" ", ""));

				result4dBuilder.append("\t").append(b.getString(Bottle.LABEL));

				for(PubJect parameter : leg.getList(Leg.PARAMETERLIST)){
					boolean found = false; 

					for(PubJect sample : b.getList(Bottle.SAMPLELIST)){

						if(parameter.getString(Parameter.UNITID).equals(sample.getString(Sample.PARAMETERUNITID))) {

							String flagChar = "";

							switch(sample.getString(Sample.FLAG)){
							case "1" : 
								log.append("WARNING: \""+ parameter.getString(Parameter.DESCRIPTION) + "\" (" + parameter.getString(Parameter.ABBREVIATION) +  " - " + parameter.getString(Parameter.COMMENT) + ", id: " + parameter.getString(Parameter.PANGAEAID) + ") in bottle ").append(b.getString(Bottle.STATION)).append("-").append(b.getString(Bottle.LABEL)).append(" has flag (" + sample.getString(Sample.FLAG) + ")! \n");
								break;
							case "3" : 
								flagChar = "?"; 
								break;
							case "4" : 
								flagChar = "/"; 
								break;
							case "5" : 
								flagChar = "*"; 
								break;
							case "6" : 
								flagChar = "#"; 
								break;
							case "7" : 
								flagChar = "*"; 
								break;
							case "8" : 
								flagChar = "?"; 
								break;
							case "9" : 
								log.append("WARNING: \""+ parameter.getString(Parameter.DESCRIPTION) + "\" (" + parameter.getString(Parameter.ABBREVIATION) +  " - " + parameter.getString(Parameter.COMMENT) + ", id: " + parameter.getString(Parameter.PANGAEAID) + ") in bottle ").append(b.getString(Bottle.STATION)).append("-").append(b.getString(Bottle.LABEL)).append(" has flag (" + sample.getString(Sample.FLAG) + ")! \n");
								break;
							}

							result4dBuilder.append("\t").append(flagChar + sample.getString(Sample.VAL));

							if(Double.parseDouble(sample.getString(Sample.VAL)) < 0.0d)
								log.append("WARNING: \""+ parameter.getString(Parameter.DESCRIPTION) + "\" (" + parameter.getString(Parameter.ABBREVIATION) +  " - " + parameter.getString(Parameter.COMMENT) + ", id: " + parameter.getString(Parameter.PANGAEAID) + ") in bottle ").append(b.getString(Bottle.STATION)).append("-").append(b.getString(Bottle.LABEL)).append(" is negative (" + sample.getString(Sample.VAL) + ")! Measuring fault?\n");

							found = true;
						}
					}

					if (!found) result4dBuilder.append("\t");
				}

				result4dBuilder.append("\r");
				tmp_stationName = b.getString(Bottle.STATION);
			}

			log.append("Writing... ");
			log.append("done!\n");
			log.append("=================================================== END JOB ===================================================\n\n");

			input.newJiraAttachment("result.4d", result4dBuilder.toString().getBytes());
			input.newJiraAttachment("log.txt", log.toString().getBytes());
			input.newJiraComment("FileCreator4D: exited normally after " + (System.currentTimeMillis() - millis)/1000.0 + " s.");
			input.newJiraComment(log.toString());
			return input;

		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("FileCreator4D: " + e.getMessage());
		}
	}

	public static void main (String[] args) throws Exception{

		ComMap s = new ComMap("");
		s.put("de.pubflow.services.ocn.PluginAllocator.getData.legid", "12013");
		
		s = (new OCNDataLoader()).getData(s, 0);

		s = new OCNToPangaeaMapper().mapValues(s, 0);

		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.author", "author");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.source", "source");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.reference", "reference");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.fileName", "/tmp/3_sortedBottleId.4d");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.pid", "pid");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.comment", "comment");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.project", "project");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.topology", "topology");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.status", "status");
		s.put("de.pubflow.services.ocn.PluginAllocator.toCSV.login", "login");

		ComMap map = new FileCreator4D().toCSV(s, 0);

		for(Entry<String, Object> e : map.entrySet()){
			FileWriter fw = new FileWriter("/tmp/" + e.getKey());
			fw.append((String)e.getValue());
			fw.close();	
		}

	}

}
