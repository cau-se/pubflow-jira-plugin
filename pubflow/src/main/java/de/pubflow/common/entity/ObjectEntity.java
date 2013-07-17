package de.pubflow.common.entity;

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
