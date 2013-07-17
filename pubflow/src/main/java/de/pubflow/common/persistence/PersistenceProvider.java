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

package de.pubflow.common.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;

import org.hsqldb.Server;

import de.pubflow.PubFlowCore;


@SuppressWarnings("unchecked")
public class PersistenceProvider {

	private EntityManager em;
	private EntityTransaction tx;
	private static Server hsqlServer = null;

	/**
	 * This method initializes the PubFlow database
	 */
	public static void initDB() {
		if(hsqlServer == null){
			try {

				hsqlServer = new Server();

				// HSQLDB prints out a lot of informations when
				// starting and closing, which we don't need now.
				// Normally you should point the setLogWriter
				// to some Writer object that could store the logs.
				hsqlServer.setLogWriter(null);
				hsqlServer.setSilent(true);

				// The actual database will be named 'xdb' and its
				// settings and data will be stored in files
				// testdb.properties and testdb.script
				hsqlServer.setDatabaseName(0, "pubflow");
				
				hsqlServer.setDatabasePath(0, "file:" + PubFlowCore.getInstance().getProperty("path", PersistenceProvider.class.toString()) + "/DB/pubflow");

				// Start the database!
				hsqlServer.start();

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public <T> List<T>  execQuery(String s){
		Query q = em.createQuery(s);
		List<T> l;
		l = q.getResultList();
		return l;
	}

	public <T> T getObject(Class<?> clazz, Object id){
		T t =  (T) em.find(clazz, id);
		return t;
	}

	public <T> T merge(Object o){
		return  (T) em.merge(o);
	}

	public void persist(Object o){
		em.persist(o);
	}

	public void remove(Object o){
		em.remove(o);
	}

	public void startTransaction(){
		em = EMFactory.getEmf().createEntityManager();
		tx = em.getTransaction();
		tx.begin();
	}

	public void stopTransaction(){
		tx.commit();
		em.close();
	}

	public boolean checkInit(Object o){
		PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
		return unitUtil.isLoaded(o);
	}

	public void detach(Object o){
		em.detach(o);
	}

	public void refresh(Object o){
		em.refresh(o);
	}
}