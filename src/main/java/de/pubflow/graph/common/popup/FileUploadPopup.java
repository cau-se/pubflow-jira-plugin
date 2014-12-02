package de.pubflow.graph.common.popup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

import de.pubflow.graph.client.JSCaller;
import de.pubflow.graph.client.PubFlow_GraphViewer;
import gwtupload.client.IUploader;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader.OnCancelUploaderHandler;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

public class FileUploadPopup extends TitledPopup{
	
	private static final String TITLE = "File Loader";
	private static final String CANCEL_BUTTON_TEXT = "Cancel";
	private final SingleUploader uploader;
	
	public FileUploadPopup(final JSCaller jsCaller, final PubFlow_GraphViewer gui, final IconBrowserPopup iconBrowser) {
		super(TITLE);
		super.setGlassEnabled(true);
		
		// Create a new uploader panel and attach it to the document
		SingleUploader.setUploadTimeout(5000);
	    uploader = new SingleUploader();
	    uploader.setAutoSubmit(true);
	    super.setContent(uploader);
	    	    
	    // Add a finish handler which will load the image once the upload finishes
	    uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler(){
			@Override
			public void onFinish(IUploader uploader) {
				if (uploader.getStatus() == Status.SUCCESS) {
					hide();
					
					
					UploadedInfo info = uploader.getServerInfo();
					final String fileType = info.ctype;
					final boolean isTXTFile = fileType.equals("text/plain");
					final boolean isXMLFile = (isTXTFile) ? false : fileType.equals("text/xml");
					
					// maybe a graph text file
					if(isTXTFile || isXMLFile){
						// replace &amp; sequences in XML strings, since they are falsely escaped &-chars
						final String JSONString = (isTXTFile) ? info.message : info.message.replace("&amp;", "&");
						
						gui.setControlsEnabled(true);
						gui.setGraphEnabled(true);
						jsCaller.loadGraph(JSONString, iconBrowser, isXMLFile);
						jsCaller.log("Success loading File!");
						return;
					}
					jsCaller.log("Failed to load File! Invalid Format!");
				}else{
					jsCaller.log("Failed to load File!");
				}
			}
		});
	    
	    uploader.addOnCancelUploadHandler(new OnCancelUploaderHandler(){
			@Override
			public void onCancel(IUploader uploader) {
				jsCaller.log("Cancelled loading File!");
				hide();
			}
	    });
	    
	    // Add cancel button
	    final Button popCancel = new Button(CANCEL_BUTTON_TEXT);
		popCancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				uploader.cancel();
				hide();
			}});
	    super.addBottomWidget(popCancel);
	    
	}
	
	/**
	 * Shows the popup while opening the filebrowser.
	 */
	public void show(){
		super.show();
		//TODO: uploader.browseFiles
	}
}
