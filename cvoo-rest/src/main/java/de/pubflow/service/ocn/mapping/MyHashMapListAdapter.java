package de.pubflow.service.ocn.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.pubflow.service.ocn.entity.abstractClass.LBPSContainer;



public class MyHashMapListAdapter extends XmlAdapter<MyHashMapListType,HashMap<String, List<LBPSContainer>>>{

	@Override
	public HashMap<String, List<LBPSContainer>> unmarshal(MyHashMapListType v) throws Exception {
		HashMap<String, List<LBPSContainer>> hm = new HashMap<String, List<LBPSContainer>>();

		for(MyHashMapListEntryType m : v.entry){
			hm.put(m.key, m.value);
		}

		return hm;
	}

	@Override
	public MyHashMapListType marshal(HashMap<String, List<LBPSContainer>> v) throws Exception {
		MyHashMapListType mh = new MyHashMapListType();
		mh.entry = new ArrayList<MyHashMapListEntryType>();
		
		for( String m : v.keySet()){
			MyHashMapListEntryType mhet = new MyHashMapListEntryType();
			mhet.key = m;
			mhet.value = v.get(m);
			mh.entry.add(mhet);
		}
		
		return mh;
	}

}
