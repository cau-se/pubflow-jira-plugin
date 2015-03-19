package de.pubflow.server.services.cvoo;

import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Map.Entry;

import de.pubflow.server.core.jira.ComMap;
import de.pubflow.server.core.jira.JiraEndpoint;
import de.pubflow.server.core.jira.Entity.JiraAttachment;
import de.pubflow.server.core.jira.Entity.JiraComment;
import de.pubflow.server.services.ocn.FileCreator4D;
import de.pubflow.server.services.ocn.PangaeaMapper;

public class PluginAllocator {

	public static ComMap getData(ComMap data) throws Exception{
		//		if(PluginManifestValidator.check("de.pubflow.server.services.ocn.getData", data, PluginAllocator.class.getClass().getResourceAsStream("PluginManifest.xml"))){

		CVOODataLoader loader = new CVOODataLoader();

		try {
			JiraEndpoint.changeStatus(data.getDefaultIssueKey(), "There is no data for legid %s in the cvoo database or the view 'leg' has been changed. \n");

			data = loader.getData(data, 0);

			for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraCommentsAndFlush()){
				JiraEndpoint.addIssueComment(comment);
			} 

			for(JiraAttachment attachment : (data.getJiraAttachmentsAndFlush())){
				JiraEndpoint.addAttachment(attachment);
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			JiraEndpoint.changeStatus(data.getDefaultIssueKey(), "Data Needs Correction");
			e.printStackTrace();
			JiraEndpoint.addIssueComment(new JiraComment(data.getDefaultIssueKey(), e.getMessage()));
		}
		//		}
		return data;

	}

	public static ComMap convert(ComMap data) throws Exception{

		//		if(PluginManifestValidator.check("de.pubflow.server.services.ocn.convert", data, PluginAllocator.class.getResourceAsStream("PluginManifest.xml"))){

		PangaeaMapper mapper = new PangaeaMapper();

		try {
			data = mapper.mapValues(data, 0);

			for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraCommentsAndFlush()){
				JiraEndpoint.addIssueComment(comment);
			} 

			for(JiraAttachment attachment : (data.getJiraAttachmentsAndFlush())){
				JiraEndpoint.addAttachment(attachment);
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			JiraEndpoint.changeStatus(data.getDefaultIssueKey(), "Data Needs Correction");
			e.printStackTrace();
			JiraEndpoint.addIssueComment(new JiraComment(data.getDefaultIssueKey(), e.getMessage()));
		}
		//		}
		return data;
	}

	public static void toCSV(ComMap data) throws Exception{
		//		if(PluginManifestValidator.check("de.pubflow.server.services.ocn.toCSV", data, PluginAllocator.class.getResourceAsStream("PluginManifest.xml"))){

		FileCreator4D fc4d = new FileCreator4D();
		data.setDefaultIssueKey(data.getDefaultIssueKey());

		try {		
			data = fc4d.toCSV(data, 0);

			for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraCommentsAndFlush()){
				JiraEndpoint.addIssueComment(comment);
			} 

			for(JiraAttachment attachment : data.getJiraAttachmentsAndFlush()){
				JiraEndpoint.addAttachment(attachment); 
			}

			JiraEndpoint.changeStatus(data.getDefaultIssueKey(), "Ready for Pangaea-Import");

		} catch (Exception e) {
			JiraEndpoint.changeStatus(data.getDefaultIssueKey(), "Data Needs Correction");
			e.printStackTrace();
			JiraEndpoint.addIssueComment(new JiraComment(data.getDefaultIssueKey(), e.getMessage()));
		}
	}
	//	}

	public static void main(String[]a) throws Exception{
		ComMap data = new ComMap("");
		data.put("de.pubflow.services.cvoo.PluginAllocator.getData.legid", "321449");

		for(Entry<String, Object> e : data.entrySet()){
			FileWriter fw;
			fw = new FileWriter("/tmp/" + e.getKey());
			fw.append(e.getValue() + "");
			fw.close();	
		}
		
		new CVOODataLoader().getData(data, 0);
		data = new PangaeaMapper().mapValues(data, 0);

		for(Entry<String, Object> e : data.entrySet()){
			FileWriter fw;
			fw = new FileWriter("/tmp/" + e.getKey());
			fw.append(e.getValue() + "");
			fw.close();	
		}
		
//		data.put("de.pubflow.services.ocn.PluginAllocator.convert.leg", data.get("de.pubflow.services.ocn.PluginAllocator.getData.leg"));

		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.author", null);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.source", "source");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.reference", "reference");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.fileName", "filename");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.pid", "pid");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.comment", "legcomment");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.project", "project");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.topology", "topology");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.status", "status");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.login", "login");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.targetPath", "targetpath");

		new FileCreator4D().toCSV(data, 0);


		for(Entry<String, Object> e : data.entrySet()){
			FileWriter fw;
			fw = new FileWriter("/tmp/" + e.getKey());
			fw.append(e.getValue() + "");
			fw.close();	
		}

	}


}
