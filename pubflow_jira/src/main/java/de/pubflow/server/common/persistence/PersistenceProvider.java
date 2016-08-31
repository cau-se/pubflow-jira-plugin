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

package de.pubflow.server.common.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;

import org.hsqldb.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("unchecked")
public class PersistenceProvider {

	static Logger myLogger = LoggerFactory.getLogger(PersistenceProvider.class);
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
				hsqlServer.setLogWriter(null);
				hsqlServer.setSilent(false);
				hsqlServer.setDatabaseName(0, "pubflow");
				hsqlServer.setDatabasePath(0, "file:" + "etc/DB/pubflow");
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