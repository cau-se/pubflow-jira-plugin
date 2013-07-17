package de.cau.tf.ifi.se.pubflow.common.repository.abstractRepository.repository;

/**
 * @author arl
 *	Enumerator to define the modification states of ObjectEntities.  
 */
public enum EObjectModification {
	/**
	 * Use MODIFIED for modified or new created objects.
	 * Use DELETED for deleted objects. 
	 */
	MODIFIED, DELETED
}
