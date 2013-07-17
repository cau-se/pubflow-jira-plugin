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

package de.pubflow.common.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


@Entity
public class BPELProcess implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	@OneToMany(mappedBy = "myProcess")
	@Cascade(value = { CascadeType.PERSIST, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private List <ProcessLog> logs;

	@OneToMany(mappedBy = "myProcess")
	@Cascade(value = { CascadeType.PERSIST, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private List <Parameter> parameters;

	@Lob
	@Column(length = 100000)
	@XmlTransient
	private byte[] zippedProcessFile;

	@Column(unique = true)
	private String processName = "";

	@Column(unique = true)
	private String portName = "";

	private boolean deployed = false;
	private String description = "";
	private String processMethod = "";

	private String odeName = "";


	public void setId(int id) {
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String process) {
		this.description = process;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String name) {
		this.processName = name;
	}

	@XmlTransient
	public byte[] getZippedProcessFile() {
		return zippedProcessFile;
	}

	public void setZippedProcessFile(byte[] zippedProcessFile) {
		this.zippedProcessFile = zippedProcessFile;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String toString(){
		return id + " " + processName;
	}

	public List <Parameter> getDetachedParameters() {
		return parameters;
	}

	public List <Parameter> getParameters() {
		return parameters;

	}

	public void setParameters(List <Parameter> parameters) {
		this.parameters = parameters;
	}

	public List <ProcessLog> getLogs() {
		return logs;
	}

	public void setLogs(List <ProcessLog> logs) {
		this.logs = logs;
	}

	public String getOdeName() {
		return odeName;
	}

	public void setOdeName(String odeName) {
		this.odeName = odeName;
	}

	public String getProcessMethod() {
		return processMethod;
	}

	public void setProcessMethod(String processMethod) {
		this.processMethod = processMethod;
	}
}
