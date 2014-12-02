package de.pubflow.graph.common;

import java.util.Stack;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.google.gwt.user.client.ui.Widget;

import de.pubflow.graph.client.JSCaller;
import de.pubflow.graph.common.popup.WarningPopup;

public class NodePropertiesTable extends Grid{

	private final static String TITLE_TEXT_LEFT = "Property Name";
	private final static String TITLE_TEXT_RIGHT = "Value";
	private final static String APPLY_BUTTON_TEXT = "Apply Changes";
	private final static String ADD_BUTTON_TEXT = "+ Entry";
	private final static String TEMP_KEY_TEXT = "NewKey";
	private final static String WARNING_DELETE_TEXT = "No key was specified.\nDo you want to delete the entry?";
	
	private final static String CSS_KEY = "nodePropertyTableKey";
	private final static String CSS_VALUE = "nodePropertyTableValue";
	private final static String CSS_KEY_TITLE = "nodePropertyTableTitleKey";
	private final static String CSS_VALUE_TITLE = "nodePropertyTableTitleValue";
	
	private final ClickHandler clickTextHandler;
	private final FocusHandler focusKeyHandler;
	private final FocusHandler focusValueHandler;
	private final BlurHandler blurValueHandler;
	private final BlurHandler blurKeyHandler;
	private final KeyUpHandler enterTextHandler;
	
	private final WarningPopup deleteWarning;
	private final Label titleLabelLeft;
	private final Label titleLabelRight;
	private final Button applyButton;
	private final Button addButton;
	
	private final Stack<String> deletionStack;
	private String editedObjectID;
	
	private DockLayoutPanel grandParent;
	private Widget parent;
	
	/**
	 * Constructor for an empty table
	 */
	public NodePropertiesTable(final JSCaller jsCaller){
		super(1,2);
		super.setStyleName("imageGrid");
		super.setStyleName("nodePropertyTable", true);
		super.setBorderWidth(1);
		super.setVisible(false);
		
		deletionStack = new Stack<String>();
		
		// apply CSS rules
		titleLabelLeft = new Label(TITLE_TEXT_LEFT);
		titleLabelLeft.setStyleName(CSS_KEY_TITLE);
		titleLabelRight = new Label(TITLE_TEXT_RIGHT);
		titleLabelRight.setStyleName(CSS_VALUE_TITLE);
		
		// init Accept Button
		applyButton = new Button(APPLY_BUTTON_TEXT);
		applyButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				final int keyValueCount = numRows - 2;
				final String[] keys = new String[keyValueCount];
				final String[] values = new String[keyValueCount];
				
				// is edited object an edge?
				if(editedObjectID == null){
					//final String nodeFrom;
					//final String nodeTo;
					
					//TODO: jsCaller.setEdgeProperties(editedObjectID, keys, values);
				}else{
					
					// remove unused properties
					if(!deletionStack.isEmpty()){
						jsCaller.removeNodeProperties(editedObjectID, deletionStack);
					}
					
					// add new properties / change existing ones
					for(int i = 0; i < keyValueCount; i++){
						keys[i] = getWidget(i + 1, 0).getTitle();
						values[i] = ((TextBox) getWidget(i + 1, 1)).getText();
					}
					jsCaller.setNodeProperties(editedObjectID, keys, values);
				}
				applyButton.setEnabled(false);
				applyButton.setFocus(false);
			}
		});
		
		// init Add Button
		addButton = new Button(ADD_BUTTON_TEXT);
		addButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				addKeyValuePair(TEMP_KEY_TEXT,	"");
			}
		});
		
		// init TextBox Handlers
		blurKeyHandler = initBlurKeyHandler(jsCaller);
		focusKeyHandler = initFocusKeyHandler(jsCaller);
		focusValueHandler = initFocusValueHandler(jsCaller);
		clickTextHandler = initClickTextHandler(jsCaller);
		blurValueHandler = initBlurValueHandler(jsCaller, applyButton);
		enterTextHandler = initEnterTextHandler(applyButton);
		
		// init DeleteWarning
		deleteWarning = new WarningPopup("Yes");
		deleteWarning.setText(WARNING_DELETE_TEXT);
	}
	
	/**
	 * @return a ClickHandler for TextBoxes that selects the whole text
	 */
	private static ClickHandler initClickTextHandler(final JSCaller jsCaller) {
		return new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				final TextBox src = (TextBox) event.getSource();
				src.selectAll();
			}
		};
	}
	
	/**
	 * @return a FocusHandler for key TextBoxes that enables shortcut-key presses
	 * 		for the canvas and signals the removal of the entry if the key has changed
	 */
	private FocusHandler initFocusKeyHandler(final JSCaller jsCaller) {
		return new FocusHandler(){
			@Override
			public void onFocus(FocusEvent event) {
				jsCaller.setKeyPressEnabledNative(false);
				
				final TextBox src = (TextBox) event.getSource();
				// push key to list of possible deletions
				deletionStack.push(src.getText());
			}
		};
	}
	
	/**
	 * @return a FocusHandler for value TextBoxes that enables shortcut-key presses
	 * 		for the canvas
	 */
	private static FocusHandler initFocusValueHandler(final JSCaller jsCaller) {
		return new FocusHandler(){
			@Override
			public void onFocus(FocusEvent event) {
				jsCaller.setKeyPressEnabledNative(false);
			}
		};
	}
	
	/**
	 * @return a BlurHandler for TextBoxes that enables shortcut-key presses
	 * 		for the canvas
	 */
	private static BlurHandler initBlurValueHandler(final JSCaller jsCaller, final Button applyButton) {
		return new BlurHandler(){
			@Override
			public void onBlur(BlurEvent event) {
				final TextBox src = (TextBox) event.getSource();
				src.setTitle(src.getText());
				applyButton.setEnabled(true);
				jsCaller.setKeyPressEnabledNative(true);
			}
		};
	}
	
	/**
	 * @return a BlurHandler for key TextBoxes that signals the text has changed
	 */
	private BlurHandler initBlurKeyHandler(final JSCaller jsCaller) {
		return new BlurHandler(){
			@Override
			public void onBlur(BlurEvent event) {
				final TextBox src = (TextBox) event.getSource();
				final String newKey = getAllowedString(src.getText());
				
				// if no key is specified, attempt to delete the entry
				if(newKey.isEmpty()){
					deleteWarning.setAcceptHandler(new ClickHandler(){
						@Override
						public void onClick(ClickEvent event) {
							deleteWarning.hide();
							removeKeyValuePair(src);
							applyButton.setEnabled(true);
							jsCaller.setKeyPressEnabledNative(true);
						}
					});
					deleteWarning.setCancelHandler(new ClickHandler(){
						@Override
						public void onClick(ClickEvent event) {
							deleteWarning.hide();
							src.setFocus(true);
							src.selectAll();
							jsCaller.setKeyPressEnabledNative(true);
						}
					});
					deleteWarning.center();
				}
				else{
					src.setText(newKey);
					src.setTitle(newKey);
					applyButton.setEnabled(true);
					jsCaller.setKeyPressEnabledNative(true);
					
					// if the key did not change, remove it from deletion stack
					if(newKey.equals(deletionStack.peek())){
						deletionStack.pop();
					}
				}
			}
		};
	}
	
	/**
	 * Removes all $-signs that come before the actual text
	 * @param text the input text
	 * @return the input text without $-signs
	 */
	private static String getAllowedString(final String text){
		final String regex = "(\\$?)([^\\$]?)";//TODO: fix multiple $
		final String cleanText = text.replaceAll(regex, "$2");
		return cleanText;
	}
	
	/**
	 * @return a KeyUpHandler for TextBoxes that blurs the Textbox on Enter
	 */
	private static KeyUpHandler initEnterTextHandler(final Button applyButton) {
		return new KeyUpHandler(){
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if(event.getNativeKeyCode() == 13){
					((TextBox)event.getSource()).setFocus(false);
					applyButton.setFocus(true);
				}
			}
		};
	}
	

	/**
	 * Removes all cells from the table and empties the deletion stack.
	 */
	public void clear(){
		// clear deletion stack
		deletionStack.clear();
		
		// clear cells
		for(int r = 0, l = super.numRows; r < l; r++){
			final Widget left = super.getWidget(r, 0);
			final Widget right = super.getWidget(r, 1);
			
			if(left != null){
				super.remove(left);
			}
			if(right != null){
				super.remove(right);
			}
		}
		super.setVisible(false);
		
		// remove widget from parents
		grandParent.setWidgetHidden(parent, true);
	}
	
	/**
	 * Removes a row from the table.
	 * @param keyBox the TextBox Object which holds the key of the row which is to be removed
	 */
	public void removeKeyValuePair(final TextBox keyBox){
		for(int row = 1; row < numRows; row++){
			if(super.getWidget(row, 0) == keyBox){
				super.removeRow(row);
				return;
			}
		}
	}
	
	/**
	 * Retrieves some parents from the NodePropertiesTable in order to properly hide it.
	 */
	public void initParents(){
		this.parent = getParent().getParent();
		this.grandParent = (DockLayoutPanel) parent.getParent();
	}
	
	/**
	 * This method is called to prevent unnecessary data transmission if a node is being clicked
	 * repeatedly.
	 * @param nodeID - the ID of the node in question
	 * @return true if the node is already loaded in the table
	 */
	public boolean isNodeBeingEdited(final String nodeID){
		return super.isVisible() && editedObjectID.equals(nodeID);
	}
	
	/**
	 * Adds a number of key value pairs to the table, overriding old data.
	 * @param nodeID the id of the node which owns the keys and values
	 * @param keys an array of key-Strings
	 * @param values an array of value-Strings, tied to their respective keys
	 */
	public void setKeyValuePairs(final String nodeID, final String[] keys, final String[] values){
		// abort if number of keys does not match number of values
		final int entryCount = keys.length ;
		if(entryCount != values.length){
			return;
		}
		// clear old data and reserve space
		clear();
		super.resizeRows(entryCount + 2);
		editedObjectID = nodeID;
		
		// add table back to main panel
		grandParent.setWidgetHidden(parent, false);
		
		// set top titles
		super.setWidget(0, 0, titleLabelLeft);
		super.setWidget(0, 1, titleLabelRight);
		
		// fill in new data
		TextBox key;
		TextBox value;
		for(int e = 0; e < entryCount; e++){
			key = new TextBox();
			final String keyText = keys[e];
			key.setText(keyText);
			key.setTitle(keyText);
			key.setStyleName(CSS_KEY);
			key.setAlignment(TextAlignment.RIGHT);
			if(keyText.startsWith("$") || keyText.equals("name")){
				key.setEnabled(false);
			}else{
				key.addClickHandler(clickTextHandler);
				key.addBlurHandler(blurKeyHandler);
				key.addKeyUpHandler(enterTextHandler);
				key.addFocusHandler(focusKeyHandler);
			}
			super.setWidget(e + 1, 0, key);
			
			value = new TextBox();
			value.setStyleName(CSS_VALUE);
			value.addClickHandler(clickTextHandler);
			value.addFocusHandler(focusValueHandler);
			value.addBlurHandler(blurValueHandler);
			value.addKeyUpHandler(enterTextHandler);
			
			value.setText(values[e]);
			super.setWidget(e + 1, 1, value);
		}
		
		// set apply and add buttons
		super.setWidget(entryCount + 1, 0, addButton);
		super.setWidget(entryCount + 1, 1, applyButton);
		applyButton.setEnabled(false);
		
		super.setVisible(true);
	}
	
	/**
	 * Appends a new key-value-pair to the end of the table
	 * @param key the new key
	 * @param value the new value
	 */
	public void addKeyValuePair(final String key, final String value){
		super.resizeRows(super.numRows + 1);
		
		// new temporarily editable key box
		final TextBox tempKeyBox = new TextBox();
		tempKeyBox.setText(key);
		tempKeyBox.setTitle(key);
		tempKeyBox.setStyleName(CSS_KEY);
		tempKeyBox.setAlignment(TextAlignment.RIGHT);
		tempKeyBox.addClickHandler(clickTextHandler);
		tempKeyBox.addFocusHandler(focusKeyHandler);
		tempKeyBox.addBlurHandler(blurKeyHandler);
		tempKeyBox.addKeyUpHandler(enterTextHandler);
		super.setWidget(super.numRows - 2, 0, tempKeyBox);
		
		// new temporarily disabled value box
		final TextBox valueBox = new TextBox();
		valueBox.setText(value);
		valueBox.setTitle(value);
		valueBox.setStyleName(CSS_VALUE);
		valueBox.addClickHandler(clickTextHandler);
		valueBox.addFocusHandler(focusValueHandler);
		valueBox.addBlurHandler(blurValueHandler);
		valueBox.addKeyUpHandler(enterTextHandler);
		super.setWidget(super.numRows - 2, 1, valueBox);
		
		// rearrange apply and add button
		super.setWidget(super.numRows - 1, 0, addButton);
		super.setWidget(super.numRows - 1, 1, applyButton);
		applyButton.setEnabled(true);
		
		// focus key box
		tempKeyBox.setFocus(true);
		tempKeyBox.selectAll();
	}
	
}
