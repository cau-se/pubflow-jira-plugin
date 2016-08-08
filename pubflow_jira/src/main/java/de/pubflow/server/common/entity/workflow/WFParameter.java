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
package de.pubflow.server.common.entity.workflow;

import java.io.Serializable;

public class WFParameter implements Serializable{
	
	private static final long serialVersionUID = -7467369540437772314L;
	private String key;
	private ParameterType payloadClazz;
	private Object value;

	public WFParameter(String key, Object o) throws Exception{
		setKey(key);
		setValue(o);
		
		if(o instanceof Integer){
			setPayloadClazz(ParameterType.INTEGER);

		}else if(o instanceof String){
			setPayloadClazz(ParameterType.STRING);

		}else if (o instanceof Long){
			setPayloadClazz(ParameterType.LONG);

		}else if (o instanceof Double){
			setPayloadClazz(ParameterType.DOUBLE);
		}else{
			throw new Exception("Object has to be complex type");
		}
	}

	public Object getValue() {
		return value;
	}
	
	private void setValue(Object value) {
		this.value = value;
	}
	
	public ParameterType getPayloadClazz() {
		return payloadClazz;
	}
	
	private void setPayloadClazz(ParameterType payloadClazz) {
		this.payloadClazz = payloadClazz;
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return "WFParameter [key=" + key + ", payloadClazz=" + payloadClazz
				+ ", value=" + value + "]";
	}
}
