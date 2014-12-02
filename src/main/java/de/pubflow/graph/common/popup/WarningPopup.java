package de.pubflow.graph.common.popup;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

public class WarningPopup extends TitledPopup{
	private static String WARNING_TEXT = "Warning";
	private static final String CANCEL_BUTTON_TEXT = "Cancel";
	private static final String OK_BUTTON_TEXT = "Ok";
	
	private final Label message;
	private final Button okButton; 
	private final Button cancelButton; 
	private HandlerRegistration okButtonHandler;
	private HandlerRegistration cancelButtonHandler;
	
	
	public WarningPopup(){
		super(WARNING_TEXT);
		super.setGlassEnabled(true);
		
		message = new Label(WARNING_TEXT);
		message.asWidget().getElement().getStyle().setMargin(0.5, Unit.EM);
		super.setContent(message);
		
		// cancel button
		{
		    cancelButton = new Button(CANCEL_BUTTON_TEXT);
		    cancelButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					hide();
				}});
		    super.addBottomWidget(cancelButton);
		}
		// accept button
		{
			this.okButton = new Button(OK_BUTTON_TEXT);
			okButtonHandler = okButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					hide();
				}});
			super.addBottomWidget(okButton);
		}
	}
	
	public WarningPopup(final String okText){
		super(WARNING_TEXT);
		super.setGlassEnabled(true);
		
		message = new Label(WARNING_TEXT);
		message.asWidget().getElement().getStyle().setMargin(0.5, Unit.EM);
		super.setContent(message);
		
		// cancel button
		{
		    cancelButton = new Button(CANCEL_BUTTON_TEXT);
		    cancelButtonHandler = cancelButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					hide();
				}});
		    super.addBottomWidget(cancelButton);
		}
		// accept button
		{
			this.okButton = new Button(okText);
			okButtonHandler = okButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					hide();
				}});
			super.addBottomWidget(okButton);
		}
	}
	
	/**
	 * Sets the message text of the warning.
	 * @param msg the new message text
	 */
	public void setText(final String msg){
		message.setText(msg);
	}
	
	/**
	 * Replaces the ClickHandler for the ok button.
	 * @param accept the accept handler
	 */
	public void setAcceptHandler(ClickHandler accept){
		okButtonHandler.removeHandler();
		okButtonHandler = okButton.addClickHandler(accept);
	}
	
	/**
	 * Replaces the ClickHandler for the cancel button.
	 * @param accept the accept handler
	 */
	public void setCancelHandler(ClickHandler cancel){
		cancelButtonHandler.removeHandler();
		cancelButtonHandler = cancelButton.addClickHandler(cancel);
	}
}
