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
/**
 * @author arl
 *
 */

package de.pubflow.server.common.repository.abstractRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.repository.abstractRepository.adapters.StorageAdapter;
import de.pubflow.server.common.repository.abstractRepository.misc.ERepositoryName;

public abstract class BasicProvider<T> implements IProvider<T> {

	protected BasicRepository br;
	private static final Logger log = LoggerFactory.getLogger(BasicProvider.class);
	
	protected BasicProvider(ERepositoryName repositoryName, StorageAdapter storageAdapter){
		try {
			br = new BasicRepository(repositoryName, storageAdapter);
		} catch (IOException e) {
			System.out.println("Cannot create BasicProvider.");
			e.printStackTrace();
		}
	}

	public void remove(long l) {
		log.info("remove - l : " + l);
		br.remove(l);
	}

	public void clear() {
		log.info("clear");
		br.removeAll();
	}

	public long addEntry(T o) {
		log.info("addEntry - o.getClass() : " + o.getClass().getSimpleName());
		long l = br.add(o);
		log.info("addEntry - o.id : " + l);
		return l;
	}

	@SuppressWarnings("unchecked")
	public T getEntry(long id) {
		log.info("getEntry - id : " + id);
		return (T)br.get(id);

	}


	@SuppressWarnings("unchecked")
	public List<T> getAllEntries() {
		log.info("getAllEntries");

		List<Long> allIds = br.getAllIds();
		List<T> result = new ArrayList<T>();

		for(Long l : allIds){
			result.add((T) br.get(l));
		}

		return result;
	}
}
