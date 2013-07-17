/***************************************************************************
 * Copyright 2012 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
/**
 * @author arl
 *
 */

package de.cau.tf.ifi.se.pubflow.common.entity.messages;


public class Message {
	private Credential i_credential; 
	private Payload i_payload;
	
	public Credential getI_credential() {
		return i_credential;
	}
	public void setI_credential(Credential i_credential) {
		this.i_credential = i_credential;
	}
	public Payload getI_payload() {
		return i_payload;
	}
	public void setI_payload(Payload i_payload) {
		this.i_payload = i_payload;
	}
}