
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

package de.pubflow.server.common.persistence.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.persistence.PersistenceProvider;


public abstract class BasicDAO <T> {
	
	Class<T> clazz;
	String name;
	
	protected static Logger logger;
	
	static {
		logger = LoggerFactory.getLogger(BasicDAO.class);
		logger.info("Starting ProcessStarter.");
	}
	
	BasicDAO(Class<T> clazz, String name){
		this.clazz = clazz;
		this.name = name;
	}
	
	PersistenceProvider p = new PersistenceProvider();

	public void merge(T o){
		logger.info("Merge");
		
		p.startTransaction();
		p.merge(o);
		p.stopTransaction();
	}
	
	public T persist(T o){
		logger.info("Persist");
		
		p.startTransaction();
		p.persist(o);
		p.stopTransaction();
		return o;
	}
	
	public T getObject(int i){
		logger.info("Get");
		
		p.startTransaction();
		T result = p.<T>getObject(clazz, i);
		p.stopTransaction();
		return result;
	}

	public T getObject(T o){
		logger.info("Get");

		p.startTransaction();
		T result = p.getObject(clazz, o);
		p.stopTransaction();
		return result;
	}

	public List<T> execQuery(String s){
		logger.info("Query");

		p.startTransaction();
		List<T> results = p.execQuery(s);
		p.stopTransaction();	
		return results;
	}
	public List<T> getAll(){	
		List<T> results = execQuery("select p from " + name + " p");	
		return results;
	}

	public void remove(int i){
		logger.info("Remove");

		p.startTransaction();
		T o = p.getObject(clazz, i);
		p.remove(o);
		p.stopTransaction();
	}
	
	public boolean isLoaded(Object o){
		logger.info("isLoaded");

		p.startTransaction();
		boolean b = p.checkInit(o);
		p.stopTransaction();
		
		return b;
	}
	
	public void detach(Object o){
		logger.info("Detach");

		p.startTransaction();
		p.detach(o);
		p.stopTransaction();
	}
}
