package de.pubflow.common.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class JiraComment {
	private String issueKey;
	private String text;
	
	public JiraComment(){
		
	}
	
	public JiraComment(String issueKey, String comment) {
		super();
		this.issueKey = issueKey;
		this.text = comment;
	}
	
	public String getIssueKey() {
		return issueKey;
	}
	
	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}
