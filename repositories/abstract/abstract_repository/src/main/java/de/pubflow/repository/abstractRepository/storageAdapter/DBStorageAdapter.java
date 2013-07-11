package de.pubflow.repository.abstractRepository.storageAdapter;

import java.io.IOException;

import de.pubflow.persistence.PersistenceProvider;
import de.pubflow.persistence.dao.RepositoryDAO;
import de.pubflow.shared.entity.ObjectEntity;

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
