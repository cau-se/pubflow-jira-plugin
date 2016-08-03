package de.pubflow.server.core.restMessages;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.pubflow.server.common.entity.workflow.WFParameter;

/**
 * Represents the update/event messages for existing Workflows
 * 
 * @author Marc Adolf
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class WorkflowUpdateCall {
	private UUID id;
	private String eventType;
	// TODO is the list of parameters fitting for this message?
	private List<WFParameter> data;

	public WorkflowUpdateCall() {

	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public List<WFParameter> getData() {
		return data;
	}

	public void setData(List<WFParameter> data) {
		this.data = data;
	}

}