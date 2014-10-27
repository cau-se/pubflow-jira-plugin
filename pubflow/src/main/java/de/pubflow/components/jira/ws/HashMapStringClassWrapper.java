package de.pubflow.components.jira.ws;

import java.util.HashMap;
import java.util.Map.Entry;

public class HashMapStringClassWrapper {

	private HashMap<String, String> basketMap = new HashMap<String, String>();

	public HashMapStringClassWrapper(HashMap<String, Class<?>> basketMap) {
		for(Entry<String, Class<?>> e : basketMap.entrySet()){
			this.basketMap.put((String) e.getKey(), ((Class<?>) e.getValue()).getSimpleName());
		}
	}

	public HashMap<String, String> getBasketMap() {
		return basketMap;
	}

	public void setBasketMap(HashMap<String, String> basketMap) {
		this.basketMap = basketMap;
	}
}
