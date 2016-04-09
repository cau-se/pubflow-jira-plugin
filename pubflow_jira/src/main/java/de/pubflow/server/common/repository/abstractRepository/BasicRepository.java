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

package de.pubflow.server.common.repository.abstractRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.pubflow.server.common.persistence.entities.ObjectEntity;
import de.pubflow.server.common.repository.abstractRepository.adapters.StorageAdapter;
import de.pubflow.server.common.repository.abstractRepository.misc.EObjectModification;
import de.pubflow.server.common.repository.abstractRepository.misc.ERepositoryName;
import de.pubflow.server.common.repository.abstractRepository.misc.RepositoryMap;

public class BasicRepository {
	private long id;
	private RepositoryMap repositoryMap = new RepositoryMap();
	private Map<Long, EObjectModification> modifiedEntries = new HashMap<Long, EObjectModification>();
	private final StorageAdapter storageAdapter;
	private boolean cachingEnabled = false;

	public BasicRepository(ERepositoryName repositoryName, StorageAdapter storageAdapter) throws IOException {
		this.storageAdapter = storageAdapter;		
		id = repositoryName.getType();

		try{
			repositoryMap.setEntries(StorageAdapter.loadMapping(repositoryMap, id));
		}catch(FileNotFoundException e){
			repositoryMap.setEntries(new LinkedList<ObjectEntity>());
		}
	}

	public StorageAdapter getStorageAdapter() {
		return storageAdapter;
	}

	public Object get(long l){
		try {
			if (repositoryMap.get(l) == null){
				if (cachingEnabled){
					repositoryMap.add(restore(l), l);
				}
				return restore(l);
			}else{
				return repositoryMap.get(l);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void removeAll(){
		for(long l : getAllIds()){
			remove(l);
		}
	}
	
	public void remove(long l){
		try{
			repositoryMap.remove(l);
			modifiedEntries.put(l, EObjectModification.DELETED);
			persist();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long add(Object object) {
		try{
			long id = repositoryMap.add(object);
			modifiedEntries.put(id, EObjectModification.MODIFIED);
			persist();
			return id;

		} catch (Exception e) {
			e.printStackTrace();
			return 0l;
		}
	}

	public void add(Object object, long id) {
		try{
			repositoryMap.add(object, id);
			modifiedEntries.put(id, EObjectModification.MODIFIED);
			persist();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void persist() throws IOException{
		storageAdapter.update(repositoryMap, modifiedEntries);
		StorageAdapter.saveMapping(repositoryMap, id);
	}

	private Object restore(long id) throws IOException{
		return storageAdapter.restore(id);
	}

	public List<Long> getAllIds(){
		return repositoryMap.getAllIds();
	}
}