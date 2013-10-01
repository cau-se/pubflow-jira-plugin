package de.pubflow.common.entity.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ParameterList")
public class WFParamList{

	List<WFParameter> parameterList = new ArrayList<WFParameter>();
	
	

	/**
	 * @return the parameterList
	 */
	public List<WFParameter> getParameterList() {
		return parameterList;
	}

	/**
	 * @param parameterList the parameterList to set
	 */
	public void setParameterList(List<WFParameter> parameterList) {
		this.parameterList = parameterList;
	}

	/**
	 * @return
	 * @see java.util.ArrayList#size()
	 */
	public int size() {
		return parameterList.size();
	}

	/**
	 * @return
	 * @see java.util.ArrayList#isEmpty()
	 */
	public boolean isEmpty() {
		return parameterList.isEmpty();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return parameterList.contains(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return parameterList.indexOf(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return parameterList.containsAll(c);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return parameterList.lastIndexOf(o);
	}


	/**
	 * @return
	 * @see java.util.ArrayList#toArray()
	 */
	public Object[] toArray() {
		return parameterList.toArray();
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.ArrayList#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return parameterList.toArray(a);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#get(int)
	 */
	public WFParameter get(int index) {
		return parameterList.get(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.ArrayList#set(int, java.lang.Object)
	 */
	public WFParameter set(int index, WFParameter element) {
		return parameterList.set(index, element);
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return parameterList.toString();
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean add(WFParameter e) {
		return parameterList.add(e);
	}

	/**
	 * @param index
	 * @param element
	 * @see java.util.ArrayList#add(int, java.lang.Object)
	 */
	public void add(int index, WFParameter element) {
		parameterList.add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#remove(int)
	 */
	public WFParameter remove(int index) {
		return parameterList.remove(index);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return parameterList.remove(o);
	}

	/**
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	public void clear() {
		parameterList.clear();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends WFParameter> c) {
		return parameterList.addAll(c);
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.ArrayList#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends WFParameter> c) {
		return parameterList.addAll(index, c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.ArrayList#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return parameterList.removeAll(c);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.ArrayList#listIterator(int)
	 */
	public ListIterator<WFParameter> listIterator(int index) {
		return parameterList.listIterator(index);
	}

	/**
	 * @return
	 * @see java.util.ArrayList#listIterator()
	 */
	public ListIterator<WFParameter> listIterator() {
		return parameterList.listIterator();
	}

	/**
	 * @return
	 * @see java.util.ArrayList#iterator()
	 */
	public Iterator<WFParameter> iterator() {
		return parameterList.iterator();
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.ArrayList#subList(int, int)
	 */
	public List<WFParameter> subList(int fromIndex, int toIndex) {
		return parameterList.subList(fromIndex, toIndex);
	}

	public List<WFParameter> getParameter()
	{
		return parameterList;
	}

}
