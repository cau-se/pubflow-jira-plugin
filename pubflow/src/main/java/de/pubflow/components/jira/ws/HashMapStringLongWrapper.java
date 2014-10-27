package de.pubflow.components.jira.ws;

import java.util.HashMap;

public class HashMapStringLongWrapper {

	private HashMap<String, Long> basketMap = null;

	public HashMapStringLongWrapper(HashMap<String, Long> basketMap) {
		this.setBasketMap(basketMap);
	}

	public HashMap<String, Long> getBasketMap() {
		return basketMap;
	}

	public void setBasketMap(HashMap<String, Long> basketMap) {
		this.basketMap = basketMap;
	}
}
