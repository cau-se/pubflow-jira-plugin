package de.pubflow.graph.common.popup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.pubflow.graph.client.JSCaller;
import de.pubflow.graph.common.IconCell;
import de.pubflow.graph.common.ImageGrid;

public class NodeEditPopup extends TitledPopup{
	// Strings
	private static final String CANCEL_BUTTON_TEXT = "Cancel";
	private static final String APPLY_BUTTON_TEXT = "Apply";
	private static final String ICON_TEXT = "Icon";
	private static final String NAME_TEXT = "Displayed Name";
	private static final String INPUT_PORTS_TEXT = "Input Ports";
	private static final String OUTPUT_PORTS_TEXT = "Output Ports";
	private static final String CREATE_BUTTON_TEXT = "Create";
	private static final String CHANGE_BUTTON_TEXT = "Change";
	private static final String ADD_NODE_TEXT = "Add Node";
	private static final String EDIT_NODE_TEXT = "Edit Node";
	private static final String CSS_VALID_INPUT_STYLE = "textField";
	private static final String CSS_INVALID_INPUT_STYLE = "textFieldInvalid";
	private static final String INVALID_FIELDS_TEXT = "Input- and output ports must be specified with a valid integer value!";
	
	private final WarningPopup inputErrorPopup;
	private final JSCaller jsCaller;
	private final IconCell iconCell;
	private final Button iconButton;
	private final TextArea nameField;
	private final IntegerBox inputField; 
	private final IntegerBox outputField;
	private final Button okButton;
	private final ImageGrid imageGrid;
	
	// used for deleting/editing nodes
	private String sourceID;
	private String targetID;
	private boolean isEditingObject;

	public NodeEditPopup(final JSCaller jsCaller, final IconBrowserPopup iconBrowser){
		super(ADD_NODE_TEXT);
		super.setGlassEnabled(true);
		
		this.isEditingObject = false;
		this.jsCaller = jsCaller;
		
		// define warning for invalid input
		this.inputErrorPopup = new WarningPopup();
		inputErrorPopup.setText(INVALID_FIELDS_TEXT);
		inputErrorPopup.setTitle("Error");
		
		// define style names
		final String captionStyle = "popupCaption";
		
		// panels
		final HorizontalPanel mainPanel = new HorizontalPanel();
		final CaptionPanel leftPanel = new CaptionPanel(ICON_TEXT);
		final VerticalPanel rightPanel = new VerticalPanel();
		
		//mainPanel.setStyleName("smallPadding");
		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
		leftPanel.setStyleName(captionStyle);
		rightPanel.setStyleName("popupButtonPanel");
		super.setContent(mainPanel);
		
		// icon chooser
		{
			// add handler for accepting an icon
			iconBrowser.addAcceptHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					iconCell.setData(imageGrid.getSelectedCell());
					iconBrowser.hide();
			}});
			
			final VerticalPanel leftPanelInner = new VerticalPanel();
			leftPanelInner.setStyleName("popupPanelInner");
			leftPanel.add(leftPanelInner);
			
			imageGrid = iconBrowser.getImageGrid();
			final int iSize = imageGrid.iconSize;
			
			new Image();
			this.iconCell = new IconCell(iSize, (int) (iSize * 1.5));
			leftPanelInner.add(iconCell);
			
			// add icon change button
			this.iconButton = new Button(CHANGE_BUTTON_TEXT);
			leftPanelInner.add(iconButton);
			iconButton.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					iconBrowser.center();
					imageGrid.selectCell(iconCell.getTitle());
				}
			});
		}
		
		// text box
		{
			// top label
			final CaptionPanel nameLabel = new CaptionPanel(NAME_TEXT);
			nameLabel.setStyleName(captionStyle);
			
			// text field
			this.nameField = new TextArea();
			nameField.setStyleName("textAreaBox");
			
			nameLabel.add(nameField);
			rightPanel.add(nameLabel);
		}		
		
		// input port count
		{
			this.inputField = new IntegerBox();
			final CaptionPanel inputLabel = new CaptionPanel(INPUT_PORTS_TEXT);
			inputLabel.setStyleName(captionStyle);
			
			inputField.setStyleName(CSS_VALID_INPUT_STYLE);
			inputLabel.add(inputField);
			rightPanel.add(inputLabel);
		}
		// output port count
		{
			this.outputField = new IntegerBox();
			final CaptionPanel outputLabel = new CaptionPanel(OUTPUT_PORTS_TEXT);
			outputLabel.setStyleName(captionStyle);
			
			outputField.setStyleName(CSS_VALID_INPUT_STYLE);
			outputLabel.add(outputField);
			rightPanel.add(outputLabel);
		}		
		// cancel button
		{
		    final Button popCancel = new Button(CANCEL_BUTTON_TEXT);
			popCancel.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					hide();
				}});
		   super.addBottomWidget(popCancel);
		}
		// accept button
		{
			this.okButton = new Button(APPLY_BUTTON_TEXT);
		    okButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					
					if(checkIntegerFields()){
						hide();
						if(isEditingObject){
							editObject();
						}else{
							jsCaller.addNodeInCenter(
									nameField.getText(), 
									inputField.getText(), 
									outputField.getText(), 
									(iconCell.isEmpty()) ? null : iconCell.getTitle());
						}
					}else{
						inputErrorPopup.center();
					}
				}});
		    super.addBottomWidget(okButton);
		}
	}
	
	/**
	 * Shows Popup in center and disables keypresses for the editor.
	 */
	public void center(){
		super.center();
		jsCaller.setKeyPressEnabledNative(false);
	}
	
	/**
	 * Shows Popup and disables keypresses for the editor.
	 */
	public void show(){
		super.show();
		jsCaller.setKeyPressEnabledNative(false);
	}
	
	/**
	 * Hides Popup and enables keypresses for the editor.
	 */
	public void hide(){
		super.hide();
		jsCaller.setKeyPressEnabledNative(true);
	}
	
	/**
	 * Memorizes up to two node ids. These are used for editing or
	 * deleting an edge or a node.
	 * @param sourceID - the id of the source port of an edge or a node.
	 * @param targetID - the id of the target port of an edge.
	 */
	public void setEditedObject(final String sourceID , final String targetID){
		this.sourceID = sourceID;
		this.targetID = targetID;
	}
	
	/**
	 * Deletes the right clicked object.
	 */
	public void deleteObject(){
		if(targetID == null){
			jsCaller.removeNodeNative(sourceID);
		}else{
			jsCaller.removeEdgeNative(sourceID, targetID);
		}
	}
	
	/**
	 * Edits the right clicked object.
	 */
	public void editObject(){
		if(targetID == null){
			jsCaller.editNodeNative(
				sourceID,
				nameField.getText(),
				(iconCell.isEmpty()) ? null : iconCell.getTitle());
			jsCaller.markNode(sourceID);
		}else{
			//JSCaller.editEdgeNative(nameField.getText());
		}
	}
	
	/**
	 * Replaces the title with either "Edit" or "Add Node"
	 * and the accept button with either "Add" or "Apply".
	 * @param edit if true, we edit a node
	 */
	public void setEdit(final boolean edit){
		if(edit){
			okButton.setText(APPLY_BUTTON_TEXT);
			super.setTitle(EDIT_NODE_TEXT);
			inputField.setEnabled(false);
			outputField.setEnabled(false);
		}else{
			okButton.setText(CREATE_BUTTON_TEXT);
			super.setTitle(ADD_NODE_TEXT);
			inputField.setEnabled(true);
			outputField.setEnabled(true);
		}
		isEditingObject = edit;
	}
	
	/**
	 * Fills all text fields and the icon with given data.
	 * @param name the displayed text of a node
	 * @param iconID the icon id of the icon
	 * @param iconUrl the url of the icon
	 * @param inputPorts the number of input ports
	 * @param outputPorts the number of output ports
	 * @param edit if true, changes the title text to "Edit Node"
	 */
	public void setFields(final String name, final String iconID, final int inputPorts, final int outputPorts){
		nameField.setText(name);
		inputField.setText(inputPorts+"");
		outputField.setText(outputPorts+"");
		imageGrid.unselectCell();
		imageGrid.selectCell(iconID);
		iconCell.setData(imageGrid.getSelectedCell());
	}
	
	
	/**
	 * Clears all text fields and resets the icon.
	 */
	public void clearFields(){
		okButton.setText(CREATE_BUTTON_TEXT);
		super.setTitle(ADD_NODE_TEXT);
		nameField.setText("");
		iconCell.clear();
		isEditingObject = false;
		
		inputField.setEnabled(true);
		inputField.setText("0");
		inputField.setStyleName(CSS_VALID_INPUT_STYLE);
		
		outputField.setEnabled(true);
		outputField.setText("0");
		outputField.setStyleName(CSS_VALID_INPUT_STYLE);
	}
	
	/**
	 * Checks both port input fields to see if they contain a natural number or zero.
	 * 
	 * @return true if both input fields contain valid numbers
	 */
	private boolean checkIntegerFields(){
		final String validIntRegex = "^0$|^[1-9]\\d*$";
		
		final boolean inputValid = inputField.getText().matches(validIntRegex);
		final boolean outputValid = outputField.getText().matches(validIntRegex);
		
		// mark invalid fields red
		inputField.setStyleName((inputValid) ? CSS_VALID_INPUT_STYLE : CSS_INVALID_INPUT_STYLE);
		outputField.setStyleName((outputValid) ? CSS_VALID_INPUT_STYLE : CSS_INVALID_INPUT_STYLE);
		
		return inputValid && outputValid;
	}
}
