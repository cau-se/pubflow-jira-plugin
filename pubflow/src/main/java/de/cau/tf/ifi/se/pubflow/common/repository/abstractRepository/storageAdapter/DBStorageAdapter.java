package de.cau.tf.ifi.se.pubflow.common.repository.abstractRepository.storageAdapter;

import java.io.IOException;

import de.cau.tf.ifi.se.pubflow.common.entity.ObjectEntity;
import de.cau.tf.ifi.se.pubflow.common.persistence.PersistenceProvider;
import de.cau.tf.ifi.se.pubflow.common.persistence.dao.RepositoryDAO;


public class DBStorageAdapter extends StorageAdapter{

	public DBStorageAdapter(){
		PersistenceProvider.initDB();
	}
	
	protected ObjectEntity onRestore(long id) throws IOException {
		RepositoryDAO repositoryDAO = new RepositoryDAO();
		return repositoryDAO.getObject(id);
	}
	
	protected void onDelete(long id) throws IOException {
		RepositoryDAO repositoryDAO = new RepositoryDAO();
		repositoryDAO.remove(id);				
	}

	protected void onUpdate(ObjectEntity o) throws IOException {
		RepositoryDAO repositoryDAO = new RepositoryDAO();
		repositoryDAO.persist(o);	
	}
}
