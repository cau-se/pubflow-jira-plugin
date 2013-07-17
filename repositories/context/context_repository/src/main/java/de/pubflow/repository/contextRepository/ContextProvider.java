package de.pubflow.repository.contextRepository;

import de.pubflow.repository.abstractRepository.interaction.BasicProvider;
import de.pubflow.repository.abstractRepository.repository.ERepositoryName;
import de.pubflow.repository.abstractRepository.storageAdapter.DBStorageAdapter;

public class ContextProvider extends BasicProvider<ContextEntity>{

	private static ContextProvider cp;

	public ContextProvider() {
		super(ERepositoryName.CONTEXT, new DBStorageAdapter());
	}

	public static ContextProvider getInstance(){
		if(cp == null){
			cp = new ContextProvider();
		}
		
		return cp;
	}
	
}
