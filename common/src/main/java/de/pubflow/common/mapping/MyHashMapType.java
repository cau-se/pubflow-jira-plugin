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

