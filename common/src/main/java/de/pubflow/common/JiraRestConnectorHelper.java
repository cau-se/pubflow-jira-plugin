package de.pubflow.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

public class JiraRestConnectorHelper implements IJiraRestConnector{

	private String baseUrl = "";

	public JiraRestConnectorHelper(String baseUrl){
		this.baseUrl = baseUrl;
	}

	public String getDocumentContent(String urlString, Map<String, Object> data){
		String msg = "";
		try {

			URL url = new URL(baseUrl + urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");

			JSONObject o = new JSONObject();

			for(Entry<String, Object> e : data.entrySet()){
				o.put(e.getKey(), e.getValue());
			}

			String input = "";		
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}


			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String part = "";
			System.out.println("Output from Server .... \n");

			while ((part = br.readLine()) != null) {
				msg += part;
			}

			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} catch (Exception e){
			e.printStackTrace();
		}

		return msg;

	}

	public Response changeStatus(String issueKey, String statusName) {
		String urlString = String.format("/%s/status", issueKey);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("issueKey", issueKey);
		data.put("statusName", statusName);
		
		getDocumentContent(urlString, data);
		return null;
	}

	public Response addAttachment(String issueKey, byte[] barray, String fileName, String type) {
		String urlString = String.format("/%s/attachments", issueKey);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("issueKey", issueKey);
		data.put("barray", barray);
		data.put("fileName", fileName);
		data.put("type", type);
		
		getDocumentContent(urlString, data);
		return null;
	}

	public Response addIssueComment(String issueKey, String comment) {
		String urlString = String.format("/%s/comments", issueKey);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("issueKey", issueKey);
		data.put("comment", comment);
		
		getDocumentContent(urlString, data);
		return null;
	}

	public Response createIssue(String issueTypeName, String summary, String description, HashMap<String, String>  parameters, String reporter) {
		String urlString = "/";

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("issueTypeName", issueTypeName);
		data.put("summary", summary);
		data.put("description", description);
		data.put("parameters", parameters);
		data.put("reporter", reporter);

		getDocumentContent(urlString, data);
		return null;
	}
}
