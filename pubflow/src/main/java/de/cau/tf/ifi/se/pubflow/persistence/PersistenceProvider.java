package de.cau.tf.ifi.se.pubflow.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;

/**
 * @author arl
 *
 */
public class PersistenceProvider {

	private EntityManager em;
	private EntityTransaction tx;
	
	public <T> List<T>  execQuery(String s){
		Query q = em.createQuery(s);
		List<T> l;
		l = q.getResultList();
		return l;
	}

	public <T> T getObject(Class clazz, Object id){
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