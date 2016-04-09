package de.pubflow.server.common.repository.abstractRepository.adapters;

import java.io.IOException;

import de.pubflow.server.common.persistence.PersistenceProvider;
import de.pubflow.server.common.persistence.daos.RepositoryDAO;
import de.pubflow.server.common.persistence.entities.ObjectEntity;


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
