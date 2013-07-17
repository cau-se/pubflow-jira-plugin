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

package de.pubflow.common.entity.component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class ComponentContainer {

	@Id
	@GeneratedValue
	private int i_compID;
	private int version;
	private String namespace; // The namespace of this component (used for updates)
	private String i_name; // should be unique for this namespace
	private boolean i_status;
	private String i_description;
	
	@Lob
	@Column(length = 100000)
	private byte[] i_ComponentFile;
	
	
	public ComponentContainer() {
		// TODO Auto-generated constructor stub
	}
	
	public ComponentContainer(int p_compID) {
		i_compID = p_compID;
	}

	/**
	 * @return the i_compID
	 */
	public int getI_compID() {
		return i_compID;
	}

	/**
	 * @param i_compID the i_compID to set
	 */
	public void setI_compID(int i_compID) {
		this.i_compID = i_compID;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the i_name
	 */
	public String getI_name() {
		return i_name;
	}

	/**
	 * @param i_name the i_name to set
	 */
	public void setI_name(String i_name) {
		this.i_name = i_name;
	}

	/**
	 * @return the i_status
	 */
	public boolean isI_status() {
		return i_status;
	}

	/**
	 * @param i_status the i_status to set
	 */
	public void setI_status(boolean i_status) {
		this.i_status = i_status;
	}

	/**
	 * @return the i_description
	 */
	public String getI_description() {
		return i_description;
	}

	/**
	 * @param i_description the i_description to set
	 */
	public void setI_description(String i_description) {
		this.i_description = i_description;
	}

	/**
	 * @return the i_ComponentFile
	 */
	public byte[] getI_ComponentFile() {
		return i_ComponentFile;
	}

	/**
	 * @param i_ComponentFile the i_ComponentFile to set
	 */
	public void setI_ComponentFile(byte[] i_ComponentFile) {
		this.i_ComponentFile = i_ComponentFile;
	}
	

}