package de.pubflow.graph.server;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException; 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List; 

import javax.servlet.http.HttpServletRequest; 
import org.apache.commons.fileupload.FileItem; 


public class UploadServlet extends UploadAction {

	private static final long serialVersionUID = 1L;
	public static int KB = 1024;
	
	@Override
	public String executeAction(HttpServletRequest request,
			List<FileItem> sessionFiles) throws UploadActionException {

		for (FileItem item : sessionFiles) {
			try {
				
				// get content type
				final String fileType;
				{
					final String compfileType = item.getContentType(); 
					final int slashIndex = compfileType.indexOf("/");
					fileType = compfileType.substring(0, slashIndex);
				}
				
				final BufferedReader br = new BufferedReader(new InputStreamReader(item.getInputStream()));
				
				// read text file
				if(fileType.equals("text") || fileType.equals("application")){
					final char[] chunk = new char[KB];
					final StringBuilder sb = new StringBuilder();
					int charsRead;
					
					while((charsRead = br.read(chunk)) == KB){
						sb.append(String.valueOf(chunk));
					}
					if(charsRead != -1){
						sb.append(String.valueOf(chunk, 0, charsRead));
					}
					
					br.close();
					return sb.toString();
				}
				else if(fileType.equals("image")){
					return null;
				}
				
				return fileType;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return  super.executeAction(request, sessionFiles);
	}
}
