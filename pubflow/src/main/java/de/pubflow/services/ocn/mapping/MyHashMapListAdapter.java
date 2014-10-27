package de.pubflow.services.ocn.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.pubflow.services.ocn.entity.abstractClass.PubJect;



public class MyHashMapListAdapter extends XmlAdapter<MyHashMapType,HashMap<String, List<PubJect>>>{

	@Override
	public HashMap<String, List<PubJect>> unmarshal(MyHashMapType v) throws Exception {
		HashMap<String, List<PubJect>> hm = new HashMap<String, List<PubJect>>();

		for(MyHashMapEntryType m : v.entry){
			hm.put(m.key, m.value);
		}

		return hm;
	}

	@Override
	public MyHashMapType marshal(HashMap<String, List<PubJect>> v) throws Exception {
		MyHashMapType mh = new MyHashMapType();
		mh.entry = new ArrayList<MyHashMapEntryType>();
		
		for( String m : v.keySet()){
			MyHashMapEntryType mhet = new MyHashMapEntryType();
			mhet.key = m;
			mhet.value = v.get(m);
			mh.entry.add(mhet);
		}
		
		return mh;
	}

}
