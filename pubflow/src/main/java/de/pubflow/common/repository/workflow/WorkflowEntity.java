package de.pubflow.common.repository.workflow;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.pubflow.PubFlowCore;
import de.pubflow.common.entity.workflow.PubFlow;
import de.pubflow.common.enumerartion.WFType;
import de.pubflow.common.exception.PropNotSetException;

public class WorkflowEntity implements Serializable{

	private static final long serialVersionUID = 2553554966653892183L;

	private byte[] gBpmn;
	private String workflowName;
	private long PubFlowWFID;
	
	private String WFID;
	private WFType type;

	public byte[] getgBpmn() {
		return gBpmn;
	}

	public void setgBpmn(byte[] gBpmn) {
		this.gBpmn = gBpmn;
	}

	public void setgBpmn(File f) throws IOException{
		gBpmn = readFile(f);
	}

	private byte[] readFile(File f) throws IOException{
		try{
			FileInputStream fis = new FileInputStream(f);
			byte[] fileContent = new byte[(int) f.length()];
			fis.read(fileContent);
			fis.close();
			return fileContent;

		}catch(Exception e){
			throw new IOException("Unable to read file " + f.getName());
		}
	}

	public String getWorkflowName() {
		return workflowName;
	}

	/**
	 * @return Map with keys path and bpmn2 
	 * @throws IOException
	 */
	public WorkflowLocationInformation writeToTempFS() throws IOException{
		
		String pathProb = "";
		try {
			pathProb = PubFlowCore.getInstance().getProperty("path", WorkflowEntity.class.toString());
		} catch (PropNotSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String path = pathProb + "/Temp/" + workflowName;

		int i;
		File f;

		do{
			i = (int)(Math.random() * 1000);
			f = new File(path  + i);
		}while(f.exists());

		path += i;
		f.mkdir();

		File workflowDir = new File(path);
		File zipFile = new File(path + "/zippedWF.zip");
		FileOutputStream fos = new FileOutputStream(zipFile);
		fos.write(gBpmn);
		fos.close();

		String bpmn2Location = unzipArchive(zipFile, workflowDir);

		return new WorkflowLocationInformation(bpmn2Location, path + "/Temp", path + "/workflowbasefiles", workflowName + i);
	}

	public String unzipArchive(File archive, File outputDir) throws IOException {
		String bpmn2File = null;
		ZipFile	zipfile = new ZipFile(archive);

		try {
			zipfile = new ZipFile(archive);
			for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements(); ) {
				ZipEntry entry = (ZipEntry) e.nextElement();

				if(entry.getName().contains(".bpmn2")){
					bpmn2File = outputDir + "/" + entry.getName();
				}
				unzipEntry(zipfile, entry, outputDir);
			}
		} catch (Exception e) {
			throw new IOException(e);
		}finally{
			zipfile.close();
		}

		if(bpmn2File != null){
			return bpmn2File;
		}else{
			throw new IOException("Zip doesn't contain a bpmn2 file.");
		}
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		if (in == null)
			throw new NullPointerException("InputStream is null!");
		if (out == null)
			throw new NullPointerException("OutputStream is null");

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

		if (entry.isDirectory()) {
			if(!(new File(outputDir, entry.getName()).exists())){
				System.out.println("Erstelle : " + outputDir + entry.getName());
				createDir(new File(outputDir, entry.getName()));
			}
			return;
		}

		File outputFile = new File(outputDir, entry.getName());
		if (!outputFile.getParentFile().exists()){
			createDir(outputFile.getParentFile());
			System.out.println("Erstelle : " + outputFile.getParentFile());
		}

		BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

		try {
			copy(inputStream, outputStream);
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}

	private void createDir(File dir) {
		if(!dir.mkdirs()) throw new RuntimeException("Can not create dir "+dir);
	}


	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
	
	public WFType getType() {
		return type;
	}

	public void setType(WFType type) {
		this.type = type;
	}

	public String getWFID() {
		return WFID;
	}

	public byte[] getWFAsByteArray() {
		return null;
	}

	public void setWFID(String wFID) {
		WFID = wFID;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	public long getPubFlowWFID() {
		return PubFlowWFID;
	}

	public void setPubFlowWFID(long pubFlowWFID) {
		PubFlowWFID = pubFlowWFID;
	}
}
