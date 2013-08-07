package de.pubflow.common.entity.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReturnTypeList {
	
	ArrayList<ReturnType> types = new ArrayList<ReturnType>();

	/**
	 * 
	 * @see java.util.ArrayList#trimToSize()
	 */
	public void trimToSize() {
		types.trimToSize();
	}

	/**
	 * @param minCapacity
	 * @see java.util.ArrayList#ensureCapacity(int)
	 */
	public void ensureCapacity(int minCapacity) {
		types.ensureCapacity(minCapacity);
	}

	/**
	 * @return
	 * @see java.util.ArrayList#size()
	 */
	public int size() {
		return types.size();
	}

	/**
	 * @return
	 * @see java.util.ArrayList#isEmpty()
	 */
	public boolean isEmpty() {
		return types.isEmpty();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return types.contains(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return types.indexOf(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return types.containsAll(c);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return types.lastIndexOf(o);
	}

	/**
	 * @return
	 * @see java.util.ArrayList#clone()
	 */
	public Object clone() {
		return types.clone();
	}

	/**
	 * @return
	 * @see java.util.ArrayList#toArray()
	 */
	public Object[] toArray() {
		return types.toArray();
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.ArrayList#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return types.toArray(a);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public ReturnType get(int index) {
		return types.get(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	public ReturnType set(int index, ReturnType element) {
		return types.set(index, element);
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return types.toString();
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean add(ReturnType e) {
		return types.add(e);
	}

	/**
	 * @param index
	 * @param element
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	public void add(int index, ReturnType element) {
		types.add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#remove(int)
	 */
	public ReturnType remove(int index) {
		return types.remove(index);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return types.remove(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractList#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return types.equals(o);
	}

	/**
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	public void clear() {
		types.clear();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends ReturnType> c) {
		return types.addAll(c);
	}

	/**
	 * @return
	 * @see java.util.AbstractList#hashCode()
	 */
	public int hashCode() {
		return types.hashCode();
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends ReturnType> c) {
		return types.addAll(index, c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return types.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return types.retainAll(c);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#listIterator(int)
	 */
	public ListIterator<ReturnType> listIterator(int index) {
		return types.listIterator(index);
	}

	/**
	 * @return
	 * @see java.util.ArrayList#listIterator()
	 */
	public ListIterator<ReturnType> listIterator() {
		return types.listIterator();
	}

	/**
	 * @return
	 * @see java.util.ArrayList#iterator()
	 */
	public Iterator<ReturnType> iterator() {
		return types.iterator();
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.ArrayList#subList(int, int)
	 */
	public List<ReturnType> subList(int fromIndex, int toIndex) {
		return types.subList(fromIndex, toIndex);
	}

	/**
	 * @return the types
	 */
	public synchronized ArrayList<ReturnType> getTypes() {
		return types;
	}

	/**
	 * @param types the types to set
	 */
	public synchronized void setTypes(ArrayList<ReturnType> types) {
		this.types = types;
	}

}
