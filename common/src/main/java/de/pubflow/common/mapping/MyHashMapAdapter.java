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

import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;


public class MyHashMapAdapter extends XmlAdapter<MyHashMapType,HashMap<String, String>>{

	@Override
	public HashMap<String, String> unmarshal(MyHashMapType v) throws Exception {
		HashMap<String, String> hm = new HashMap<String, String>();

		for(MyHashMapEntryType m : v.entryset()){
			hm.put(m.key, m.key);
		}

		return hm;
	}

	@Override
	public MyHashMapType marshal(HashMap<String, String> v) throws Exception {
		MyHashMapType mh = new MyHashMapType();
		
		for(Entry<String, String> m : v.entrySet()){
			mh.put(m.getKey(), m.getValue());
		}
		
		return mh;
	}

}
