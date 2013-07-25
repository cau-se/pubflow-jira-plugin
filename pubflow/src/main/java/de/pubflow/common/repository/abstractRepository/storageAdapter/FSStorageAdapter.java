package de.pubflow.common.repository.abstractRepository.storageAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.pubflow.PubFlowCore;
import de.pubflow.common.entity.ObjectEntity;
import de.pubflow.common.exception.PropAlreadySetException;
import de.pubflow.common.exception.PropNotSetException;


public class FSStorageAdapter extends StorageAdapter {

	protected Object onRestore(long id) throws IOException {

		String path= "";
		try {
			path = PubFlowCore.getInstance().getProperty("path", FSStorageAdapter.class.toString());
		} catch (PropNotSetException e1) {

			//TODO
			try {
				PubFlowCore.getInstance().setProperty("path", FSStorageAdapter.class.toString(), "etc/");
			} catch (PropAlreadySetException e) {
				e.printStackTrace();
			}
		}
		
		FileInputStream fin = new FileInputStream(path + "FSStorageAdapter/" + id + ".pub");
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
		String path= "";
		try {
			path = PubFlowCore.getInstance().getProperty("path", FSStorageAdapter.class.toString());
		} catch (PropNotSetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FileOutputStream fout = new FileOutputStream(path + "FSStorageAdapter/" +  o.getId() + ".pub");
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(o);
		oos.close();	
	}

	protected void onDelete(long id) throws IOException {
		String path = "";
		try {
			path = PubFlowCore.getInstance().getProperty("path", FSStorageAdapter.class.toString());
		} catch (PropNotSetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File fin = new File(path + "FSStorageAdapter/" + id + ".pub");
		fin.delete();
	}


}
