package de.cau.tf.ifi.se.pubflow.persistence;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMFactory {
	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("pubflow");

	public static EntityManagerFactory getEmf() {
		return emf;
	}
}
