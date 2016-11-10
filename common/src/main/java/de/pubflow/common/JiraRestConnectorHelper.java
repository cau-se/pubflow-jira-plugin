/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow.common;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import de.pubflow.common.entity.DataContainer;
import de.pubflow.common.entity.JiraAttachment;
import de.pubflow.common.entity.JiraComment;
import de.pubflow.common.entity.JiraIssue;

public class JiraRestConnectorHelper{

	private String baseUrl = "";

	public JiraRestConnectorHelper(String baseUrl){
		this.baseUrl = baseUrl;
	}

	public Object getDocumentContent(String urlString, Object data, Class<?> clazz){
		Object response = null;
		try {
			URL url = new URL(baseUrl + urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("content-type", "application/json");
			conn.setDoOutput(true);
			conn.setConnectTimeout(50000);
			conn.setReadTimeout(50000);
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
			System.out.println("Request url : " + urlString);

			mapper.writeValue(out, data);
			out.close();

			if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
				System.out.println(mapper.writeValueAsString(data));
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String part = "";
			String msg = "";

			while ((part = br.readLine()) != null) {
				msg += part;
			}
			br.close();

			if(clazz != Integer.class){
				try{
					response = mapper.readValue(msg, clazz);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				response = conn.getResponseCode();
			}
			conn.disconnect();

			FileWriter fw = new FileWriter("/tmp/" + Math.random());
			fw.write(msg);
			fw.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} catch (Exception e){
			e.printStackTrace();
		}

		return response;
	}

	public String handleJiraTransmissions(DataContainer data){
		System.out.println("handleJiraTransmissions");
		System.out.println(data.getComments().size());

		if(data.getComments() != null){
			for(JiraComment comment : data.getComments()){
				addIssueComment(data.getDefaultIssueKey(), comment.getText());
			}
		}

		System.out.println(data.getIssues().size());

		if(data.getIssues() != null){
			for(JiraIssue issue : data.getIssues()){
				createIssue(issue.getIssueTypeName(), issue.getSummary(), issue.getDescription(), issue.getParameters(), issue.getReporter());
			}
		}

		System.out.println(data.getAttachments().size());

		if(data.getAttachments() != null){
			for(JiraAttachment attachment : data.getAttachments()){
				addAttachment(data.getDefaultIssueKey(), attachment.getData(), attachment.getFilename(), attachment.getType());
			}
		}

		if(data.getStatus() != null){
			changeStatus(data.getDefaultIssueKey(), data.getStatus());
		}

		data.flush();

		return "";
	}

	public Integer changeStatus(String issueKey, String statusName) {
		String urlString = String.format("/pubflow/issues/%s/status", issueKey);
		Integer responseCode = (Integer) getDocumentContent(urlString, statusName, Integer.class);
		System.out.println("changeStatus : " + statusName);
		return responseCode;
	}

	public Integer addAttachment(String issueKey, byte[] barray, String fileName, String type) {
		String urlString = String.format("/pubflow/issues/%s/attachments", issueKey);
		JiraAttachment attachment = new JiraAttachment();
		attachment.setData(barray);
		attachment.setType(type);
		attachment.setFilename(fileName);
		attachment.setIssueKey(issueKey);
		Integer responseCode = (Integer) getDocumentContent(urlString, attachment, Integer.class);
		System.out.println("addAttachment : " + attachment.toString());
		return responseCode;
	}

	public Integer addIssueComment(String issueKey, String comment) {
		String urlString = String.format("/pubflow/issues/%s/comments", issueKey);
		Integer responseCode = (Integer) getDocumentContent(urlString, comment, Integer.class);
		System.out.println("addIssueComment : " + comment);
		return responseCode;
	}

	public String createIssue(String issueTypeName, String summary, String description, HashMap<String, String>  parameters, String reporter) {
		String urlString = String.format("/pubflow/issues/");
		JiraIssue issue = new JiraIssue();
		issue.setSummary(summary);
		issue.setDescription(description);
		issue.setReporter(reporter);
		issue.setIssueTypeName(issueTypeName);
		issue.setParameters(parameters);
		String responseCode = (String) getDocumentContent(urlString, issue, String.class);
		System.out.println("createIssue : " + urlString);
		return responseCode;
	}
}
