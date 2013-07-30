package de.pubflow.common.repository.context;

import de.pubflow.common.entity.repository.ContextEntity;
import de.pubflow.common.repository.abstractRepository.interaction.BasicProvider;
import de.pubflow.common.repository.abstractRepository.repository.ERepositoryName;
import de.pubflow.common.repository.abstractRepository.storageAdapter.DBStorageAdapter;



public class ContextProvider extends BasicProvider<ContextEntity>{

	private static ContextProvider cp;

	private ContextProvider() {
		super(ERepositoryName.CONTEXT, new DBStorageAdapter());
	}

	public static ContextProvider getInstance(){
		if(cp == null){
			cp = new ContextProvider();
		}
		
		return cp;
	}
	
}
