package de.pubflow.common.mapping;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;

	public class MyHashMapType{
        public List<MyHashMapEntryType> map = new LinkedList<MyHashMapEntryType>();
        
        public MyHashMapType(){
        }

		public List<MyHashMapEntryType> entryset(){
        	return map;
        }
        
        public void put(String key, String value){
        	MyHashMapEntryType mhet = new MyHashMapEntryType();
        	mhet.key = key;
        	mhet.value = value;
        	map.add(mhet);
        }
        
        public MyHashMapEntryType get(String key){
        	for(MyHashMapEntryType entry : map){
        		if(entry.key.equals(key)){
        			return entry;
        		}
        	}
        	return null;
        }
    }

