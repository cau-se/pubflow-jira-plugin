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
import java.util.Map;

import javax.ws.rs.core.Response;

import com.google.gson.Gson;

import de.pubflow.common.entity.DataContainer;

public class JiraRestConnectorHelper{

	private String baseUrl = "";

	public JiraRestConnectorHelper(String baseUrl){
		this.baseUrl = baseUrl;
	}

	public DataContainer getDocumentContent(String urlString, DataContainer data){
		DataContainer response = null;
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
			
			response = gson.fromJson(msg, DataContainer.class);
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
