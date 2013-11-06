package de.pubflow.wfCompUntis.ocn;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import de.pubflow.common.properties.PropLoader;
import de.pubflow.wfCompUntis.ocn.entity.Bottle;
import de.pubflow.wfCompUntis.ocn.entity.Leg;
import de.pubflow.wfCompUntis.ocn.entity.Parameter;
import de.pubflow.wfCompUntis.ocn.entity.Sample;
import de.pubflow.wfCompUntis.ocn.entity.abstractClass.PubJect;


/**
 * @author arl
 *
 */

public class FileCreator4D {

	private String log = "";

	public byte[] getLog(){
		return log.getBytes();
	}
	
	public byte[] toCSV(String input, String pid, String login, 
			String source, String author, String project, 
			String topology, String status, String savePath, 
			String reference, String fileName, String comment, int instanceId) throws Exception{
		
		try{

			StringBuilder log = new StringBuilder();

			JAXBContext ctx = JAXBContext.newInstance(Leg.class);

			Unmarshaller um = ctx.createUnmarshaller();
			StringReader sr = new StringReader(input);

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
			log.append("PubFlow_downloadableFile=" + savePath + "\n");
			log.append("=================================================== END JOB ===================================================\n\n");

			leg.add(Leg.LOGSTING, leg.getString(Leg.LOGSTING) + log.toString());
			
			this.log = leg.getString(Leg.LOGSTING);
			
			return fourDStringBuilder.toString().getBytes();

			
		}catch(Exception e){
			try {
				PrintStream fw = new PrintStream(PropLoader.getInstance().getProperty("FilePrinterExceptionFile", this.getClass().getCanonicalName(), "exceptionFilePrinter.txt"));
				e.printStackTrace(fw);

				fw.append(e.toString());
				fw.append(e.getMessage());
				fw.append(e.getLocalizedMessage());
				fw.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}

			e.printStackTrace();
			
			return new byte[0];
		}
	}
	
	public static void main (String[] args) throws Exception{

		String s = (new OCNDataLoader()).getData(3, 0);
		
		s = new OCNToPangaeaMapper().replaceArtefacts(s, 0);

		byte[] output = new FileCreator4D().toCSV(s, "333", "333", "333", "333", "333", "333", "333", "/tmp/" + "3_sortedBottleId.4d", "333", "333", "333", 0);

		FileWriter fw = new FileWriter("/tmp/lalala.txt");
		fw.append(new String(output));
		fw.close();	
	}

}
