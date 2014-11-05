package de.pubflow.server.common.entity;

public interface StringSerializable {
	
	public String transformToString();
	public void initFromString(String content);

}
