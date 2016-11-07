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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.google.gson.Gson;

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

			System.out.println("Request url : " + urlString);

			Gson gson = new Gson();
			out.write(gson.toJson(data));
			out.close();

			if (conn.getResponseCode() != 200 && conn.getResponseCode() != 202) {
				System.out.println(gson.toJson(data));
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String part = "";
			String msg = "";

			while ((part = br.readLine()) != null) {
				msg += part;
			}
			br.close();

			if(!clazz.getClass().equals(String.class)){
				try{
					response = gson.fromJson(msg, clazz);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				response = conn.getResponseCode();
			}
			conn.disconnect();


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
		for(JiraComment comment : data.getJiraCommentsAndFlush()){
			addIssueComment(data.getDefaultIssueKey(), comment.getText());
		}
		
		for(JiraIssue issue : data.getJiraIssuesAndFlush()){
			createIssue(issue.getIssueTypeName(), issue.getSummary(), issue.getDescription(), issue.getParameters(), issue.getReporter());
		}
		
		for(JiraAttachment attachment : data.getJiraAttachmentsAndFlush()){
			addAttachment(data.getDefaultIssueKey(), attachment.getData(), attachment.getFilename(), attachment.getType());
		}
		
		return "";
	}
	
	public String changeStatus(String issueKey, String statusName) {
		String urlString = String.format("/pubflow/issues/%s/status", issueKey);
		String responseCode = (String) getDocumentContent(urlString, statusName, String.class);
		return responseCode;
	}

	public String addAttachment(String issueKey, byte[] barray, String fileName, String type) {
		String urlString = String.format("/pubflow/issues/PUB-1/attachments", issueKey);
		JiraAttachment attachment = new JiraAttachment();
		attachment.setData(barray);
		attachment.setType(type);
		attachment.setFilename(fileName);
		attachment.setIssueKey(issueKey);

		String responseCode = (String) getDocumentContent(urlString, attachment, JiraAttachment.class);
		return responseCode;
	}

	public String addIssueComment(String issueKey, String comment) {
		String urlString = String.format("/pubflow/issues/%s/comments", issueKey);
		String responseCode = (String) getDocumentContent(urlString, comment, String.class);
		
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
		
		String responseCode = (String) getDocumentContent(urlString, issue, JiraIssue.class);
		return responseCode;
	}
}
