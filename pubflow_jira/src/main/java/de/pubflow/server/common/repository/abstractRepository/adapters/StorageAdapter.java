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
package de.pubflow.server.common.repository.abstractRepository.adapters;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.pubflow.server.common.persistence.entities.ObjectEntity;
import de.pubflow.server.common.repository.abstractRepository.misc.EObjectModification;
import de.pubflow.server.common.repository.abstractRepository.misc.IDPool;
import de.pubflow.server.common.repository.abstractRepository.misc.RepositoryMap;

public abstract class StorageAdapter {

	public Object restore(long id) throws IOException{
		return onRestore(id);
	}

	public void update(RepositoryMap repositoryMap, Map<Long, EObjectModification> modifiedEntries) throws IOException{
		try{

			for(Entry<Long, EObjectModification> entry : modifiedEntries.entrySet()){

				if(entry.getValue().equals(EObjectModification.DELETED)){
					onDelete(entry.getKey());

				}else{

					ObjectEntity oe = new ObjectEntity(entry.getKey(), repositoryMap.get(entry.getKey()));
					if(entry.getValue().equals(EObjectModification.MODIFIED)){
						if(oe.getId() == 0){
							oe.setId(IDPool.getUniqueID());
						}
						onUpdate(oe);


					}else{
						throw new IOException("Object is not serializable");
					}

				}
			}

			modifiedEntries.clear();
		}catch(Exception e){
			throw new IOException(e);
		}
	}

	public static void saveMapping(RepositoryMap rm, long id) throws IOException{
		List <Long> keyList = new LinkedList<Long>();

		for(ObjectEntity e : rm.getEntries()){
			keyList.add(e.getId());
		}

		ObjectEntity oe = new ObjectEntity(id, keyList);
		FSStorageAdapter fssa = new FSStorageAdapter();
		fssa.onUpdate(oe);
	}

	@SuppressWarnings("unchecked")
	public static List<ObjectEntity> loadMapping(RepositoryMap rm, long id) throws IOException{
		FSStorageAdapter fssa = new FSStorageAdapter();

		List<Long> keyList;
		keyList = (List<Long>)fssa.restore(id);

		List<ObjectEntity> rmel = new LinkedList<ObjectEntity>();

		for(Long l : keyList){
			rmel.add(new ObjectEntity(l, null));
		}

		return rmel;
	}

	protected abstract void onDelete(long id) throws IOException;
	protected abstract Object onRestore(long id) throws IOException;
	protected abstract void onUpdate(ObjectEntity o) throws IOException;

}
