package de.pubflow.wfCompUnits;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;


public class ByteRay {

	private static String COMMENT = "jiraComment_";
	private static String ATTACHMENT = "jiraAttachment_";
	
	public static HashMap<String, byte[]> newMap(){
		HashMap<String, byte[]> map =  new HashMap<String, byte[]>();
		map.put("commentCounter", Integer.toString(0).getBytes());
		return map;
	}
	

	public static HashMap<String, byte[]> newJiraComment(HashMap<String, byte[]> map, String String){
		int commentsId = Integer.parseInt(new String(map.get("commentCounter")));
		map.put(COMMENT + commentsId, String.getBytes());
		map.put("commentCounter", Integer.toString(commentsId + 1).getBytes());
		return map;
	}

	public static HashMap<String, byte[]> newJiraAttachment(HashMap<String, byte[]> map, String fileName, byte[] file){
		map.put(ATTACHMENT + fileName, file);
		return map;
	}

	public static LinkedList<String> getJiraComments(HashMap<String, byte[]> map){
		LinkedList<String> commentList = new LinkedList<String>();
		
		for(Entry<String, byte[]> e : map.entrySet()){
			if(e.getKey().startsWith(COMMENT)){
				commentList.add(new String(e.getValue()));
			}
		}
		return commentList;
	}
	
	public static HashMap<String, byte[]> flushData(HashMap<String, byte[]> map){
		HashMap<String, byte[]> result = new HashMap<String, byte[]>();
		
		for(Entry<String, byte[]> e : map.entrySet()){
			String key = e.getKey();
			if(!key.startsWith(ATTACHMENT) && !key.startsWith(COMMENT)){
				result.put(key, e.getValue());
			}
		}
		return result;
	}
	
	public static HashMap<String, byte[]> getJiraAttachments(HashMap<String, byte[]> map){
		HashMap<String, byte[]> attachments = new HashMap<String, byte[]>();
		
		for(Entry<String, byte[]> e : map.entrySet()){
			if(e.getKey().startsWith(ATTACHMENT)){
				attachments.put(e.getKey().replaceFirst(ATTACHMENT, "") , e.getValue());
			}
		}
		return attachments;
	}
}