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

package de.cau.tf.ifi.se.pubflow.common.entity;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public class ProcessLog {
	
	@Id
	@GeneratedValue
	private int id;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private BPELProcess myProcess;
	
	@ElementCollection 
	//@OneToMany 
//	@MapKey(name="file")
//    @Column(name="path") 
	private Map<String, String> files = new HashMap<String, String>();
	
	@ElementCollection 
//	@OneToMany 
//    @MapKey(name="parameter")
//    @Column(name="value") 
	private Map<String, String> runParameters = new HashMap<String, String>();
	
	@ElementCollection
//	@OneToMany 
//    @MapKey(name="key")
//	@Column(length = 1000000000)
	private List<String> logs = new LinkedList<String>();
	
	private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

	public ProcessLog(){}
	
	public String marshal() throws JAXBException{	
		JAXBContext ctx = JAXBContext.newInstance(ProcessLog.class);
		Marshaller m = ctx.createMarshaller();
		StringWriter sw = new StringWriter();
		//m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(this, sw);
		return sw.toString();
	}
	
	public static ProcessLog unmarshal(String s) throws JAXBException{
		JAXBContext ctx = JAXBContext.newInstance(ProcessLog.class);
		Unmarshaller um = ctx.createUnmarshaller();
		StringReader sr = new StringReader(s);			

		return (ProcessLog) um.unmarshal(sr);
	}
	
	public ProcessLog(BPELProcess myProcess){
		this.myProcess = myProcess;
	}
	
	@XmlTransient
	public BPELProcess getMyProcess() {
		return myProcess;
	}

	public void setMyProcess(BPELProcess myProcess) {
		this.myProcess = myProcess;
	}


	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, String> getDownloads() {
		return files;
	}

	public void setDownloads(Map<String, String> downloads) {
		this.files = downloads;
	}

	public Map<String, String> getRunParameters() {
		return runParameters;
	}

	public void setRunParameters(Map<String, String> runParameters) {
		this.runParameters = runParameters;
	}

	public List<String> getLogs() {
		return logs;
	}

	public void setLogs(List<String> logs) {
		this.logs = logs;
	}
}
