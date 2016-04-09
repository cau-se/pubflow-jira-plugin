package de.pubflow.rest.cvoo;

import java.util.LinkedList;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.pubflow.common.DataContainer;
import de.pubflow.common.JiraRestConnectorHelper;
import de.pubflow.common.entity.JiraAttachment;
import de.pubflow.common.entity.JiraComment;
import de.pubflow.rest.ocn.FileCreator4D;
import de.pubflow.rest.ocn.PangaeaMapper;

@Path("/pubflow/cvoo")
public class PluginAllocator {

	private static JiraRestConnectorHelper restConnector = new JiraRestConnectorHelper("http://localhost:2990/jira/rest/myrestresource/1.0/pubflow/");

	@POST
	@Produces({MediaType.APPLICATION_XML})
	@Path("{issueKey}/getData")
	public static Response getData(@PathParam(value = "issueKey") String issueKey, @FormParam(value = "legId") String legId) throws Exception{
		//	if(PluginManifestValidator.check("de.pubflow.server.services.cvoo.getData", data, PluginAllocator.class.getClass().getResourceAsStream("PluginManifest.xml"))){

		CVOODataLoader loader = new CVOODataLoader();

		DataContainer data = new DataContainer(issueKey);
		data.put("de.pubflow.services.cvoo.PluginAllocator.getData.legid", legId);

		try {
			restConnector.changeStatus(data.getDefaultIssueKey(), "There is no data for legid %s in the cvoo database or the view 'leg' has been changed. \n");

			data = loader.getData(data, 0);

			for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraCommentsAndFlush()){
				restConnector.addIssueComment(comment.getIssueKey(), comment.getText());
			} 

			for(JiraAttachment attachment : (data.getJiraAttachmentsAndFlush())){
				restConnector.addAttachment(attachment.getIssueKey(), attachment.getData(), attachment.getFilename(), attachment.getType());
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			restConnector.changeStatus(data.getDefaultIssueKey(), "Data Needs Correction");
			e.printStackTrace();
			restConnector.addIssueComment(data.getDefaultIssueKey(), e.getMessage());
		}

		return Response.status(201).entity(data.get("de.pubflow.services.cvoo.PluginAllocator.getData.leg")).build();
	}

	@POST
	@Produces({MediaType.APPLICATION_XML})
	@Path("{issueKey}/convert")
	public static Response convert(@PathParam(value = "issueKey") String issueKey, @FormParam (value = "leg") String leg) throws Exception{

		//		if(PluginManifestValidator.check("de.pubflow.server.services.cvoo.convert", data, PluginAllocator.class.getResourceAsStream("PluginManifest.xml"))){

		PangaeaMapper mapper = new PangaeaMapper();
		DataContainer data = new DataContainer(issueKey);
		data.put("de.pubflow.services.cvoo.PluginAllocator.convert.log", "");
		data.put("de.pubflow.services.ocn.PluginAllocator.getData.leg", leg);

		try {
			data = mapper.mapValues(data, 0);

			for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraCommentsAndFlush()){
				restConnector.addIssueComment(comment.getIssueKey(), comment.getText());
			} 

			for(JiraAttachment attachment : (data.getJiraAttachmentsAndFlush())){
				restConnector.addAttachment(attachment.getIssueKey(), attachment.getData(), attachment.getFilename(), attachment.getType());
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			restConnector.changeStatus(data.getDefaultIssueKey(), "Data Needs Correction");
			e.printStackTrace();
			restConnector.addIssueComment(data.getDefaultIssueKey(), e.getMessage());
		}

		return Response.status(201).entity(data.get("de.pubflow.services.cvoo.PluginAllocator.getData.leg")).build();
	}

	@POST
	@Produces({MediaType.APPLICATION_XML})
	@Path("{issueKey}/toCSV")
	public static Response toCSV(@PathParam(value = "issueKey") String issueKey, @FormParam(value = "leg") String leg, 
			@FormParam(value = "author") String author, @FormParam(value = "source") String source, 
			@FormParam(value = "reference") String reference, @FormParam(value = "filename") String filename, 
			@FormParam(value = "pid") String pid, @FormParam(value = "legcomment") String legcomment, 
			@FormParam(value = "project") String project, @FormParam(value = "topology") String topology, 
			@FormParam(value = "status") String status, @FormParam(value = "login") String login, 
			String targetpath) throws Exception{
		//		if(PluginManifestValidator.check("de.pubflow.server.services.cvoo.toCSV", data, PluginAllocator.class.getResourceAsStream("PluginManifest.xml"))){

		FileCreator4D fc4d = new FileCreator4D();
		DataContainer data = new DataContainer(issueKey);

		data.setDefaultIssueKey(data.getDefaultIssueKey());
		data.put("de.pubflow.services.ocn.PluginAllocator.convert.leg", leg);
		data.put("de.pubflow.services.ocn.PluginAllocator.convert.log", "");
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.author", author);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.source", source);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.reference", reference);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.fileName", filename);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.pid", pid);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.comment", legcomment);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.project", project);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.topology", topology);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.status", status);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.login", login);
		data.put("de.pubflow.services.ocn.PluginAllocator.toCSV.targetPath", targetpath);

		try {		
			data = fc4d.toCSV(data, 0);

			for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraCommentsAndFlush()){
				restConnector.addIssueComment(comment.getIssueKey(), comment.getText());
			} 

			for(JiraAttachment attachment : (data.getJiraAttachmentsAndFlush())){
				restConnector.addAttachment(attachment.getIssueKey(), attachment.getData(), attachment.getFilename(), attachment.getType());
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			restConnector.changeStatus(data.getDefaultIssueKey(), "Data Needs Correction");
			e.printStackTrace();
			restConnector.addIssueComment(data.getDefaultIssueKey(), e.getMessage());
		}
		return Response.status(201).entity(data.get("de.pubflow.services.cvoo.PluginAllocator.getData.leg")).build();
	}
}

//	public static void main(String[]a) throws Exception{
//		ComMap data = new ComMap("");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.getData.legid", "12013");
//
//		try {
//			data = new OCNDataLoader().getData(data, 0);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		data = new Panpublic static void main(String[]a) throws Exception{
//	ComMap data = new ComMap("");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.getData.legid", "12013");
//
//	try {
//		data = new OCNDataLoader().getData(data, 0);
//	} catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//
//	data = new PangaeaMapper().mapValues(data, 0);
//
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.author", null);
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.source", "source");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.reference", "reference");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.fileName", "filename");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.pid", "pid");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.comment", "legcomment");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.project", "project");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.topology", "topology");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.status", "status");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.login", "login");
//	data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.targetPath", "targetpath");
//
//	try {
//		new FileCreator4D().toCSV(data, 0);
//	} catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//
//	try {
//		for(Entry<String, String> e : data.entrySet()){
//			FileWriter fw;
//			fw = new FileWriter("/tmp/" + e.getKey());
//			fw.append(e.getValue() + "");
//			fw.close();	
//		}
//	} catch (IOException e1) {
//		// TODO Auto-generated catch block
//		e1.printStackTrace();
//	}
//}gaeaMapper().mapValues(data, 0);
//
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.author", null);
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.source", "source");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.reference", "reference");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.fileName", "filename");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.pid", "pid");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.comment", "legcomment");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.project", "project");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.topology", "topology");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.status", "status");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.login", "login");
//		data.put("de.pubflow.services.cvoo.PluginAllocator.toCSV.targetPath", "targetpath");
//
//		try {
//			new FileCreator4D().toCSV(data, 0);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		try {
//			for(Entry<String, String> e : data.entrySet()){
//				FileWriter fw;
//				fw = new FileWriter("/tmp/" + e.getKey());
//				fw.append(e.getValue() + "");
//				fw.close();	
//			}
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//	}

