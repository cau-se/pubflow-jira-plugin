package de.pubflow.repository.abstractRepository.storageAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import de.pubflow.PropLoader;
import de.pubflow.shared.entity.ObjectEntity;

public class FSStorageAdapter extends StorageAdapter {

	protected Object onRestore(long id) throws IOException {

		Properties props = PropLoader.loadProperties();
		String path = props.getProperty("path");
		FileInputStream fin = new FileInputStream(path + "/FSStorageAdapter/" + id + ".pub");
		ObjectInputStream oos = new ObjectInputStream(fin);
		ObjectEntity oe;

		try {
			oe = (ObjectEntity) oos.readObject();

		} catch (ClassNotFoundException e) {
			throw new IOException(e);

		} finally {
			oos.close();
		}

		try {
			return oe.getObject();

		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	protected void onUpdate(ObjectEntity o) throws IOException {
		Properties props = PropLoader.loadProperties();
		String path = props.getProperty("path");
		FileOutputStream fout = new FileOutputStream(path + "/FSStorageAdapter/" +  o.getId() + ".pub");
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(o);
		oos.close();	
	}

	protected void onDelete(long id) throws IOException {
		Properties props = PropLoader.loadProperties();
		String path = props.getProperty("path");
		File fin = new File(path + "/FSStorageAdapter/" + id + ".pub");
		fin.delete();
	}


}
