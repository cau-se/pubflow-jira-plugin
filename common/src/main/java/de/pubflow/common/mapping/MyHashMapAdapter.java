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
