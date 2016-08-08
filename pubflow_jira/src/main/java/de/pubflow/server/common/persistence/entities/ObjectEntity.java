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
package de.pubflow.server.common.persistence.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class ObjectEntity implements Serializable{
	
	private static final long serialVersionUID = -3447380847055343763L;

	@Id
	private long id = 0l;
	
	@Lob
	@Column(length = 100000)
	private byte[] serializedObject;

	private long lastEdit = System.currentTimeMillis();
	
	@SuppressWarnings("unused")
	private ObjectEntity(){
	}
	
	public ObjectEntity(long id) {
		this.id = id;
	}

	public ObjectEntity(long id, Object o) throws IOException {
		this.id = id;
		setObject(o);
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

	public ObjectEntity(Object o) throws IOException {
		this.setObject(o);
	}
	
	public void setObject(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);		
		oos.writeObject(o); 
		serializedObject = baos.toByteArray();
	}

	public Object getObject() throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
		ObjectInputStream ios = new ObjectInputStream(bais);		
		return ios.readObject(); 
	}

	public long getLastEdit() {
		return lastEdit;
	}

	public void setLastEdit(long lastEdit) {
		this.lastEdit = lastEdit;
	}

}
