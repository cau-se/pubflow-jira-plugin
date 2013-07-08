package de.cau.tf.ifi.se.pubflow.persistence.dao;

import java.util.List;

import org.apache.log4j.Logger;

import de.cau.tf.ifi.se.pubflow.persistence.PersistenceProvider;

public abstract class BasicDAO <T> {
	
	Class<T> clazz;
	String name;
	
	protected static Logger logger;
	
	static {
		logger = Logger.getLogger(BasicDAO.class);
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
