package de.pubflow.service.eprints.entity;

public class MetaMsg {

	private String name;
	private String content;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "MetaMsg [name=" + name + ", content=" + content + "]";
	}

	
}
