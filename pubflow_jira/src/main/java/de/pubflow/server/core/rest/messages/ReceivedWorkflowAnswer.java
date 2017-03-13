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

package de.pubflow.server.core.rest.messages;

/**
 * Contains information about the result or the errors created during the
 * execution of one Workflow.
 * 
 * @author Marc Adolf
 *
 */
public class ReceivedWorkflowAnswer {

	/**
	 * 
	 */
	private String result;

	/**
	 * 
	 */
	private String errorMessage;

	/**
	 * 
	 */
	private String newStatus;

	/**
	 * 
	 */
	private String commentMessage;

	/**
	 * 
	 * @return
	 */
	public String getResult() {
		return result;
	}

	/**
	 * 
	 * @param result
	 */
	public void setResult(final String result) {
		this.result = result;
	}

	/**
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * 
	 * @return
	 */
	public String getNewStatus() {
		return newStatus;
	}

	/**
	 * 
	 * @param newStatus
	 */
	public void setNewStatus(final String newStatus) {
		this.newStatus = newStatus;
	}

	/**
	 * 
	 * @return
	 */
	public String getCommentMessage() {
		return commentMessage;
	}

	/**
	 * 
	 * @param commentMessage
	 */
	public void setCommentMessage(final String commentMessage) {
		this.commentMessage = commentMessage;
	}

}
