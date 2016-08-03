package de.pubflow.server.core.restMessages;

import java.util.UUID;

/**
 * @author Marc Adolf
 *
 */
public class WorkflowAnswer {

	/**
	 * 
	 */
	private UUID id;
	private String result;
	private String errorMessage;

	public WorkflowAnswer() {

	}

	public UUID getId() {
		return id;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setId(UUID id) {
		this.id = id;
	}

}
