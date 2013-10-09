package de.pubflow.wfCompUntis.ocn;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

public class FileCreator4D {
	
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

			String title = leg.getString("leg_name");
			String event = leg.getString("leg_name") + "-" + leg.getList("bottleList").get(0).getString("bottle_station");


			//METADATEN

			StringBuilder sb = new StringBuilder();
			sb.append("/* DATA DESCRIPTION:").append("\n");

			if(author != null)
				if(!author.equals("")){
					sb.append("Author:\t").append(author).append("\n");
				}else{
					log.append("Author was not set, skipping... \n");
				}

			if(source != null)
				if(!source.equals("")){
					sb.append("Source:\t").append(source).append("\n");
				}else{
					log.append("Source was not set, skipping... \n");
				}

			if(title != null)
				if(!title.equals("")){
					sb.append("Title:\t").append(title).append("\n");
				}else{
					log.append("Title was not set, skipping... \n");
				}

			if(reference != null)
				if(!reference.equals("")){
					sb.append("Reference:\t").append(reference).append("\n");
				}else{
					log.append("Reference was not set, skipping... \n");
				}

			if(fileName != null)
				if(!fileName.equals("")){
					sb.append("Export Filename:\t").append(fileName).append("\n");
				}else{
					log.append("Export Filename was not set, skipping... \n");
				}

			sb.append("Event:\t").append(event.replace(" ", "")).append("\n");
			sb.append("PI:\t").append(pid).append("\n");
			sb.append("Parameter:\t");

			sb.append("790").append(" * ")
			.append("PI: ").append(pid).append(" * ")
			.append("METHOD: 43").append(" * ")
			.append("FORMAT: #####0").append("\n");

			for(PubJect p : leg.getList("parameterList")){
				sb.append(p.getString(Parameter.c_pangaeaId)).append(" * ")
				.append("PI: ").append(pid).append(" * ")
				.append("METHOD: ").append(p.getString(Parameter.c_methodId)).append(" * ")
				.append("FORMAT: ").append(p.getString(Parameter.c_format)).append(" * ")
				.append(" COMMENT: ").append(p.getString(Parameter.c_comment)).append("\n");
			}

			if(comment != null)
				if(!comment.equals("")){
					sb.append("Comment:\t").append(comment).append("\n");
				}else{
					log.append("Comment was not set, skipping... \n");
				}

			if(project != null)
				if(!project.equals("")){
					sb.append("Project:\t").append(project).append("\n");
				}else{
					log.append("Project was not set, skipping... \n");
				}

			if(topology != null)
				if(!topology.equals("")){
					sb.append("Topologic Type:\t").append(topology).append("\n");
				}else{
					log.append("Topologic type was not set, skipping... \n");
				}

			if(status != null)
				if(!status.equals("")){
					sb.append("Status:\t").append(status).append("\n");
				}else{
					log.append("Status was not set, skipping... \n");
				}

			if(login != null)
				if(!login.equals("")){
					sb.append("Login:\t").append(login).append("\n");
				}else{
					log.append("Login was not set, skipping... \n");
				}

			sb.append("*/").append("\n");

			//DATEN

			sb.append("Event label\t790");

			for(PubJect p : leg.getList(Leg.c_parameterList))
				sb.append("\t").append(p.getString(Parameter.c_pangaeaId));

			sb.append("\n");

			String tmp_stationName = "";
			for(PubJect b : leg.getList(Leg.c_bottleList)){

				if(!tmp_stationName.equals(b.getString(Bottle.c_station)))
					sb.append(leg.getString(Leg.c_name)+ "-" + b.getString(Bottle.c_station).replace(" ", ""));

				sb.append("\t").append(b.getString(Bottle.c_label));

				for(PubJect parameter : leg.getList(Leg.c_parameterList)){
					boolean found = false; 

					for(PubJect sample : b.getList(Bottle.c_sampleList)){

						if(parameter.getString(Parameter.c_unitId).equals(sample.getString(Sample.c_parameterUnitId))) {

							String flagChar = "";

							if(sample.getString(Sample.c_flag).equals("3") || sample.getString(Sample.c_flag).equals("8")){
								flagChar = "?";
							}else if(sample.getString(Sample.c_flag).equals("4")){
								flagChar = "/";
							}else if(sample.getString(Sample.c_flag).equals("5") || sample.getString(Sample.c_flag).equals("7")){
								flagChar = "*";
							}else if(sample.getString(Sample.c_flag).equals("6")){
								flagChar = "#";
							}else if(sample.getString(Sample.c_flag).equals("1") || sample.getString(Sample.c_flag).equals("9")){
								log.append("Parameter in bottle ").append(b.getString(Bottle.c_station)).append("-").append(b.getString(Bottle.c_label)).append(", sample with pangaea parameter id ").append(parameter.getString(Parameter.c_pangaeaId)).append(" has flag (" + sample.getString(Sample.c_flag) + ")! \n");
							}

							sb.append("\t").append(flagChar + sample.getString(Sample.c_val));

							if(Double.parseDouble(sample.getString(Sample.c_val)) < 0.0d)
								log.append("Parameter in bottle ").append(b.getString(Bottle.c_station)).append("-").append(b.getString(Bottle.c_label)).append(", sample with pangaea parameter id ").append(parameter.getString(Parameter.c_pangaeaId)).append(" is negative (" + sample.getString(Sample.c_val) + ")! Measuring fault?\n");

							found = true;
						}
					}

					if (!found) sb.append("\t");
				}

				sb.append("\n");
				tmp_stationName = b.getString(Bottle.c_station);
			}

			log.append("Writing... ");
			log.append("done!\n");

			leg.add(Leg.c_logString, leg.getString(Leg.c_logString) + log.toString());
//			FileWriter fstream = new FileWriter(savePath);
//			BufferedWriter out = new BufferedWriter(fstream);
//			out.write(sb.toString());
//			out.close();

			File output = new File(savePath);
			FileWriter os = new FileWriter(output);
			BufferedWriter out = new BufferedWriter(os);
			out.write(sb.toString());
			out.close();
			
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			  FileInputStream fileInputStream = new FileInputStream(output);
			  
		        byte[] buffer = new byte[16384];
		 
		        for (int len = fileInputStream.read(buffer); len > 0; len = fileInputStream
		                .read(buffer)) {
		            byteOut.write(buffer, 0, len);
		        }
		 
		        fileInputStream.close();
			return byteOut.toByteArray();

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
			
			return "";
		}

	}

}
