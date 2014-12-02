package de.pubflow.graph.common.popup;


import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Image;

import de.pubflow.graph.client.JSCaller;
import de.pubflow.graph.common.ImageGrid;
import de.pubflow.graph.common.ImageHolder;
import gwtupload.client.IUploader;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader.OnCancelUploaderHandler;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

public class ImageUploadPopup extends TitledPopup{
	
	private static final String TITLE_ADD = "Define Node Icon";
	private static final String TITLE_EDIT = "Edit Node Icon";
	private static final String CANCEL_BUTTON_TEXT = "Cancel";
	private static final String ACCEPT_BUTTON_TEXT = "Apply";
	private static final String ID_TEXT = "ID";
	private static final String IMAGE_PATH_TEXT = "Image URL";
	
	private final int iconSize = 75;
	
	private final SingleUploader uploader;
	private final ImageHolder imageHolder;
	private final Image hiddenImage;
	private final TextBox fileField;
	private final TextBox idField;
	private final Button acceptButton;
	
	private boolean validImage;
	
	public ImageUploadPopup(final JSCaller jsCaller, final ImageGrid imageGrid) {
		super(TITLE_ADD);
		super.setGlassEnabled(true);
		
		validImage = false;
		
		// Create a new uploader panel and attach it to the document
		SingleUploader.setUploadTimeout(5000);
	    uploader = new SingleUploader();
	    uploader.setAutoSubmit(true);
	    // TODO: make sure the window does not resize if uploader loads something!
	    
	    // define layout structure
	    final VerticalPanel mainPanel = new VerticalPanel();
	    super.setContent(mainPanel);
	    
	    // preview image
	    {
	    	imageHolder = new ImageHolder(iconSize);
	    	
	    	hiddenImage = new Image();
	    	hiddenImage.setVisible(false);
	    	mainPanel.add(hiddenImage);
	    	
		    mainPanel.add(imageHolder);
		    
		    // dimensions are lost once manipulated, thus we load a hidden image
		    // and pass the original dimensions to a visible image, which is then
		    // manipulated
		    hiddenImage.addLoadHandler(new LoadHandler(){
				@Override
				public void onLoad(LoadEvent event) {
					imageHolder.setImage(hiddenImage);
					//jsCaller.log("Success loading Image!");
					validImage = true;
					validateInput();
				}
		    });
		    
		    // for now just send a log message if the image loading fails
		    hiddenImage.addErrorHandler(new ErrorHandler(){
				@Override
				public void onError(ErrorEvent event) {
					imageHolder.setImage(null);
					validImage = false;
					validateInput();
					//jsCaller.log("Failed to load Image!");
				}
		    });
	    }
	    	 
	    // define style names
	 	final String captionStyle = "popupCaption";
	 		
	    // nodeIconID - TextField
	    {
	    	final CaptionPanel idLabel = new CaptionPanel(ID_TEXT);
 			idLabel.setStyleName(captionStyle);
 			idField = new TextBox();
 			idField.getElement().getStyle().setWidth(100, Unit.PCT);
 			idLabel.add(idField);
 			mainPanel.add(idLabel);
 			
 			idField.addKeyUpHandler(new KeyUpHandler(){
				@Override
				public void onKeyUp(KeyUpEvent event) {
					validateInput();
				}
 			});
	    }
	    
	    // URL - TextField
 		{
 			final CaptionPanel fileLabel = new CaptionPanel(IMAGE_PATH_TEXT);
 			fileLabel.setStyleName(captionStyle);
 			fileField = new TextBox();
 			fileField.getElement().getStyle().setWidth(100, Unit.PCT);
 			fileLabel.add(fileField);
 			mainPanel.add(fileLabel);
 			
 			// attempt to load image on change
 			final String pattern = ".+\\.(?: bmp|gif|png|jpeg|jpg)";
 			fileField.addKeyUpHandler(new KeyUpHandler(){
				@Override
				public void onKeyUp(KeyUpEvent event) {
					final String text = fileField.getText();
					
					// only load url if enough chars are typed in
					if(text.length() > 9 && text.matches(pattern)){
						hiddenImage.setUrl("");
						hiddenImage.setUrl(text);
					}
					else{
						imageHolder.setImage(null);
						validImage = false;
						validateInput();
					}
				}
 			});
 		}
 		mainPanel.add(uploader);
	    
	    // Add a finish handler which will load the image once the upload finishes
	    uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler(){
			@Override
			public void onFinish(IUploader uploader) {
				if (uploader.getStatus() == Status.SUCCESS) {
					UploadedInfo info = uploader.getServerInfo();
					
					if(info.ctype.startsWith("image")){
						hiddenImage.setUrl("");
						hiddenImage.setUrl(uploader.fileUrl());
						fileField.setText(uploader.fileUrl());
					}
					
				}else{
					//jsCaller.log("Failed to load Image!");
					validImage = false;
					validateInput();
				}
			}
		});
	    
	    uploader.addOnCancelUploadHandler(new OnCancelUploaderHandler(){
			@Override
			public void onCancel(IUploader uploader) {
				//jsCaller.log("Cancelled loading Image!");
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
	    
	    // Add accept button
	    acceptButton = new Button(ACCEPT_BUTTON_TEXT);
	    acceptButton.setEnabled(false);
	    super.addBottomWidget(acceptButton);
	}
	
	/**
	 * Adds a ClickHandler to the AcceptButton.
	 * @param ah the Clickhandler
	 */
	public void addAcceptHandler(final ClickHandler ah){
		acceptButton.addClickHandler(ah);
	}
	
	/**
	 * 
	 * @return The selected image
	 */
	public Image getImage(){
		return imageHolder.getImage();
	}
	
	/**
	 * 
	 * @return The iconID specified in its idField
	 */
	public String getIconID(){
		return idField.getText();
	}
	
	/**
	 * Enables the AcceptButton if the image was loaded correctly and an ID was specified,
	 * and disables it otherwise.
	 */
	public void validateInput(){
		acceptButton.setEnabled(validImage && idField.getText().length() > 0);
	}
	
	/**
	 * Clears all fields and the image and disables the AcceptButton
	 */
	public void clearFields(){

		idField.setText("");
		fileField.setText("");
		acceptButton.setEnabled(false);
		imageHolder.setImage(null);
		validImage = false;
	}
	
	@Override
	public void center(){
		clearFields();
		idField.setEnabled(true);
		setTitle(TITLE_ADD);
		
		super.center();
	}
	
	/**
	 * Fills the TextFields and the icon with data and shows the Popup.
	 * @param iconID The iconID which will fill out a TextField
	 * @param iconURL The iconURL which will fill out a TextField and be loaded
	 * 			as an image
	 */
	public void center(final String iconID, final String iconURL){
		idField.setText(iconID);
		idField.setEnabled(false);
		setTitle(TITLE_EDIT);
		
		fileField.setText(iconURL);
		acceptButton.setEnabled(true);
		hiddenImage.setUrl("");
		hiddenImage.setUrl(iconURL);
		validImage = true;
		
		super.center();
	}
}
