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
package de.pubflow.server.common.repository.abstractRepository.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.pubflow.server.common.persistence.entities.ObjectEntity;


public class RepositoryMap {
	private List<ObjectEntity> entries = new ArrayList<ObjectEntity>();

	public List<Long> getAllIds(){
		List<Long> l = new LinkedList<Long>();
		for(ObjectEntity e : entries){
			l.add(e.getId());
		}
		return l;
	}
	
	public List<ObjectEntity> getEntries() {
		return entries;
	}

	public void setEntries(List<ObjectEntity> entries) {
		this.entries = entries;
	}

	public long add(Object o) throws IOException{
		long id = IDPool.getUniqueID();		
		entries.add(new ObjectEntity(id, o));
		return id;
	}

	public void add(Object o, long id) throws IOException{
		boolean success = false;
		
		for(ObjectEntity oe : entries){
			if(oe.getId() == id){
				oe.setObject(o);
				success = true;
				break;
			}
		}
		
		if(!success){
			throw new IOException("No entry with id " + id);
		}
	}

	public void remove(long l) throws IOException{		
		for(ObjectEntity entry : entries){
			if(entry.getId() == l){
				entries.remove(entry);
				break;
			}
		}
	}

	public Object get(long l) throws IOException, ClassNotFoundException{
		for(ObjectEntity entry : entries){
			if(entry.getId() == l){
				return entry.getObject();
			}
		}
		return null;
	}
}
