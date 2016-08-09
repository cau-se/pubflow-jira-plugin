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
package de.pubflow.server.core.restMessages;

import java.util.List;
import java.util.UUID;

import de.pubflow.server.common.entity.workflow.WFParameter;

/**
 * Represents the update/event messages for existing Workflows
 * 
 * @author Marc Adolf
 *
 */
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