package de.pubflow.services.ocn;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import de.pubflow.components.jira.ByteRay;
import de.pubflow.services.ocn.entity.Bottle;
import de.pubflow.services.ocn.entity.Leg;
import de.pubflow.services.ocn.entity.Parameter;
import de.pubflow.services.ocn.entity.Sample;
import de.pubflow.services.ocn.entity.abstractClass.PubJect;


/**
 * @author arl
 *
 */

public class FileCreator4D {

	public HashMap<String, byte[]> toCSV(HashMap<String, byte[]> input, String pid, String login, 
			String source, String author, String project, 
			String topology, String status, String savePath, 
			String reference, String fileName, String comment, int instanceId) throws Exception{

		long millis = System.currentTimeMillis();

		try{
			ByteRay.flushData(input);

			if(input.get("return") == null){
				throw new IOException("4d file creation failed due to an empty input string. Something went terribly wrong in a prior work step.");				
			}

			StringBuilder log = new StringBuilder();

			if(input.get("log") != null){
				log.append(new String(input.get("log")));
			}

			JAXBContext ctx = JAXBContext.newInstance(Leg.class);

			Unmarshaller um = ctx.createUnmarshaller();
			StringReader sr = new StringReader(new String(input.get("return")));

			Leg leg = (Leg) um.unmarshal(sr);

			String title = "Hydrochemistry during  cruise " + leg.getString("leg_name");
			String event = leg.getString("leg_name") + "-" + leg.getList(Leg.BOTTLELIST).get(0).getString(Bottle.STATION);


			//METADATEN

			StringBuilder fourDStringBuilder = new StringBuilder();
			fourDStringBuilder.append("/* DATA DESCRIPTION:").append("\r");

			if(author != null)
				if(!author.equals("")){
					fourDStringBuilder.append("Author:\t").append(author).append("\r");
				}else{
					log.append("Author was not set, skipping... \n");
				}

			if(source != null)
				if(!source.equals("")){
					fourDStringBuilder.append("Source:\t").append(source).append("\r");
				}else{
					log.append("Source was not set, skipping... \n");
				}

			if(title != null)
				if(!title.equals("")){
					fourDStringBuilder.append("Title:\t").append(title).append("\r");
				}else{
					log.append("Title was not set, skipping... \n");
				}

			if(reference != null)
				if(!reference.equals("")){
					fourDStringBuilder.append("Reference:\t").append(reference).append("\r");
				}else{
					log.append("Reference was not set, skipping... \n");
				}

			if(fileName != null)
				if(!fileName.equals("")){
					fourDStringBuilder.append("Export Filename:\t").append(fileName).append("\r");
				}else{
					log.append("Export Filename was not set, skipping... \n");
				}

			fourDStringBuilder.append("Event:\t").append(event.replace(" ", "")).append("\r");
			fourDStringBuilder.append("PI:\t").append(pid).append("\r");
			fourDStringBuilder.append("Parameter:\t");

			fourDStringBuilder.append("790").append(" * ")
			.append("PI: ").append(pid).append(" * ")
			.append("METHOD: 43").append(" * ")
			.append("FORMAT: #####0").append("\r");

			for(PubJect p : leg.getList("parameterList")){
				fourDStringBuilder.append(p.getString(Parameter.PANGAEAID)).append(" * ")
				.append("PI: ").append(pid).append(" * ")
				.append("METHOD: ").append(p.getString(Parameter.METHODID)).append(" * ")
				.append("FORMAT: ").append(p.getString(Parameter.FORMAT)).append(" * ")
				.append(" COMMENT: ").append(p.getString(Parameter.ABBREVIATION)).append(" - ").append(p.getString(Parameter.COMMENT)).append("\r");
			}

			if(comment != null)
				if(!comment.equals("")){
					fourDStringBuilder.append("Comment:\t").append(comment).append("\r");
				}else{
					log.append("Comment was not set, skipping... \n");
				}

			if(project != null)
				if(!project.equals("")){
					fourDStringBuilder.append("Project:\t").append(project).append("\r");
				}else{
					log.append("Project was not set, skipping... \n");
				}

			if(topology != null)
				if(!topology.equals("")){
					fourDStringBuilder.append("Topologic Type:\t").append(topology).append("\r");
				}else{
					log.append("Topologic type was not set, skipping... \n");
				}

			if(status != null)
				if(!status.equals("")){
					fourDStringBuilder.append("Status:\t").append(status).append("\r");
				}else{
					log.append("Status was not set, skipping... \n");
				}

			if(login != null)
				if(!login.equals("")){
					fourDStringBuilder.append("Login:\t").append(login).append("\r");
				}else{
					log.append("Login was not set, skipping... \n");
				}

			fourDStringBuilder.append("*/").append("\r");

			//DATEN

			fourDStringBuilder.append("Event label\t790");

			for(PubJect p : leg.getList(Leg.PARAMETERLIST))
				fourDStringBuilder.append("\t").append(p.getString(Parameter.PANGAEAID));

			fourDStringBuilder.append("\r");


			log.append("\n");

			String tmp_stationName = "";
			for(PubJect b : leg.getList(Leg.BOTTLELIST)){

				if(!tmp_stationName.equals(b.getString(Bottle.STATION)))
					fourDStringBuilder.append(leg.getString(Leg.NAME)+ "-" + b.getString(Bottle.STATION).replace(" ", ""));

				fourDStringBuilder.append("\t").append(b.getString(Bottle.LABEL));

				for(PubJect parameter : leg.getList(Leg.PARAMETERLIST)){
					boolean found = false; 

					for(PubJect sample : b.getList(Bottle.SAMPLELIST)){

						if(parameter.getString(Parameter.UNITID).equals(sample.getString(Sample.PARAMETERUNITID))) {

							String flagChar = "";

							if(sample.getString(Sample.FLAG).equals("3") || sample.getString(Sample.FLAG).equals("8")){
								flagChar = "?";
							}else if(sample.getString(Sample.FLAG).equals("4")){
								flagChar = "/";
							}else if(sample.getString(Sample.FLAG).equals("5") || sample.getString(Sample.FLAG).equals("7")){
								flagChar = "*";
							}else if(sample.getString(Sample.FLAG).equals("6")){
								flagChar = "#";
							}else if(sample.getString(Sample.FLAG).equals("1") || sample.getString(Sample.FLAG).equals("9")){
								log.append("WARNING: \""+ parameter.getString(Parameter.DESCRIPTION) + "\" (" + parameter.getString(Parameter.ABBREVIATION) +  " - " + parameter.getString(Parameter.COMMENT) + ", id: " + parameter.getString(Parameter.PANGAEAID) + ") in bottle ").append(b.getString(Bottle.STATION)).append("-").append(b.getString(Bottle.LABEL)).append(" has flag (" + sample.getString(Sample.FLAG) + ")! \n");
							}

							fourDStringBuilder.append("\t").append(flagChar + sample.getString(Sample.VAL));

							if(Double.parseDouble(sample.getString(Sample.VAL)) < 0.0d)
								log.append("WARNING: \""+ parameter.getString(Parameter.DESCRIPTION) + "\" (" + parameter.getString(Parameter.ABBREVIATION) +  " - " + parameter.getString(Parameter.COMMENT) + ", id: " + parameter.getString(Parameter.PANGAEAID) + ") in bottle ").append(b.getString(Bottle.STATION)).append("-").append(b.getString(Bottle.LABEL)).append(" is negative (" + sample.getString(Sample.VAL) + ")! Measuring fault?\n");

							found = true;
						}
					}

					if (!found) fourDStringBuilder.append("\t");
				}

				fourDStringBuilder.append("\r");
				tmp_stationName = b.getString(Bottle.STATION);
			}

			log.append("Writing... ");
			log.append("done!\n");
			log.append("=================================================== END JOB ===================================================\n\n");

			ByteRay.newJiraAttachment(input, "result.4d", fourDStringBuilder.toString().getBytes());
			ByteRay.newJiraAttachment(input, "log.txt", log.toString().getBytes());
			ByteRay.newJiraComment(input, "FileCreator4D: exited normally after " + (System.currentTimeMillis() - millis)/1000.0 + " s.");
			ByteRay.newJiraComment(input, log.toString());
			return input;

		}catch(Exception e){
			throw new Exception("FileCreator4D: " + e.getMessage());
		}
	}

	public static void main (String[] args) throws Exception{


		HashMap<String, byte[]> s = (new OCNDataLoader()).getData("4", 0);

		s = new OCNToPangaeaMapper().replaceArtefacts(s, 0);

		HashMap<String, byte[]> map = new FileCreator4D().toCSV(s, "333", "333", "333", "333", "333", "333", "333", "/tmp/" + "3_sortedBottleId.4d", "333", "333", "333", 0);

		for(Entry<String, byte[]> e : map.entrySet()){
			FileWriter fw = new FileWriter("/tmp/" + e.getKey());
			fw.append(new String(e.getValue()));
			fw.close();	
		}


	}

}
