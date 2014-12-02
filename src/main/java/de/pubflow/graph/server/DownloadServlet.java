package de.pubflow.graph.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class DownloadServlet extends HttpServlet{
	
	// TODO: REPLACE WITH A THREAD-SAFE METHOD
	private String storedGraph;
	private static final long serialVersionUID = 1L;
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                   throws ServletException, IOException {
		
		// copy request to temporary string
		final BufferedReader out = request.getReader();
		final ByteArrayOutputStream in = new ByteArrayOutputStream();
		IOUtils.copy(out, in);
		in.close();
		out.close();
		storedGraph = in.toString();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException{
			
			final String fileName = request.getParameter("fileName");
		
            // set response properties
            response.setContentType( "text/plain" );
            response.setHeader( "Content-Disposition:", "attachment;filename=\"" + fileName + "\"" );
        	
        	// stream byteArray to file
        	final ByteArrayInputStream out = new ByteArrayInputStream(storedGraph.getBytes());
        	final ServletOutputStream in = response.getOutputStream();
        	IOUtils.copy(out, in);
        	out.close();
            in.flush();
    }
	
}
            
