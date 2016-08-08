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
package de.pubflow.server.common.entity.workflow;
import de.pubflow.server.common.enumeration.WFType;

public class PubFlow {

	private String WFID;
	private WFType type;
	private byte[] wfDef;
	
	/**
	 * @return the wfDef
	 */
	public synchronized byte[] getWfDef() {
		return wfDef;
	}

	/**
	 * @param wfDef the wfDef to set
	 */
	public synchronized void setWfDef(byte[] wfDef) {
		this.wfDef = wfDef;
	}

	public WFType getType() {
		return type;
	}

	public void setType(WFType type) {
		this.type = type;
	}

	public String getWFID() {
		return WFID;
	}

	public void setWFID(String wFID) {
		WFID = wFID;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
