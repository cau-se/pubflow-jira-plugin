package de.pubflow.repository.contextRepository;

import java.io.Serializable;
import java.util.Map;

public class ContextEntity implements Serializable{

	private static final long serialVersionUID = 2746891162274753965L;
	
	private String serviceUrl;
	private Map<String, String> parameters;

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
}
