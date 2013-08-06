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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

import de.pubflow.common.entity.workflow.BPELProcess;

@Entity
public class Parameter implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	@ManyToOne
	@JoinColumn(nullable = false)	
	private BPELProcess myProcess;

	private String name = "";
	private String binding  = "";
	private int instanceCount;	
	private String value = "";
	private Class<?> type = Integer.class;

	public void setType(String s){
		if(s.equals("string")){
			type = String.class;
		}else if(s.equals("int")){
			type = Integer.class;
		}else if(s.equals("double")){
			type = Double.class;
		}
	}

	public String getType(){
		if(type.equals(String.class))
			return "string";
		else if(type.equals(Integer.class))
			return "int";
		else if(type.equals(Double.class))
			return "double";
		else 
			return "";
	}
	
	public Class<?> getTypeAsClass(){
		if(type != null){
			return type;
		}else{
			return null;
		}
	}

	public Parameter(){}

	public Parameter(int i, BPELProcess myProcess){
		setInstanceCount(i);
		this.myProcess = myProcess;
	}

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBinding() {
		return binding;
	}
	public void setBinding(String binding) {
		this.binding = binding;
	}
	
	@XmlTransient
	public BPELProcess getMyProcess() {
	//	return myProcess;		
		return null;

	}

	public void setMyProcess(BPELProcess myProcess) {
		this.myProcess = myProcess;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getInstanceCount() {
		return instanceCount;
	}

	public void setInstanceCount(int instanceCount) {
		this.instanceCount = instanceCount;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String toString(){
		return id + ", " + name;
	}
}
