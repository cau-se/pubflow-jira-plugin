package de.pubflow.graph.common.popup;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.pubflow.graph.client.JSCaller;
import de.pubflow.graph.common.ImageGrid;

public class IconBrowserPopup extends TitledPopup{
	private static final String TITLE = "Manage Icons";
	private static final String CREATE_BUTTON_TEXT = "Add Icon";
	private static final String EDIT_BUTTON_TEXT = "Edit Icon";
	private static final String REMOVE_BUTTON_TEXT = "Remove Icon";
	private static final String CANCEL_BUTTON_TEXT = "Cancel";
	private static final String APPLY_BUTTON_TEXT = "Apply";
	private static final String DUPLICATE_ID_TEXT = "The ID already exists. Do you want to overwrite the existing image?";
	
	private static final int ICON_CELL_COL_COUNT = 5;
	private static final int ICON_CELL_WIDTH = 80;
	
	private boolean isEditingCell;
	
	private final WarningPopup warningPopup;
	private final ImageGrid imageGrid;
	private final ImageUploadPopup imageUploadPopup;
	private final Button removeCellButton;
	private final Button editCellButton;
	private final Button addCellButton;
	private final Button okButton;
	
	public IconBrowserPopup(final JSCaller jsCaller){
		super(TITLE);
		
		
		this.isEditingCell = false;
		
		// set up image grid
		this.imageGrid = new ImageGrid(ICON_CELL_COL_COUNT, ICON_CELL_WIDTH, jsCaller);
		this.imageGrid.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final Cell cell = imageGrid.getCellForEvent(event);
				imageGrid.selectCell(cell);
				
				final boolean isCellSelected = (imageGrid.getSelectedCell() != null);
				removeCellButton.setEnabled(isCellSelected);
				editCellButton.setEnabled(isCellSelected);
			}
		});
		super.setGlassEnabled(true);
		
		// define warning when id exists already
		warningPopup = new WarningPopup("Yes");
		warningPopup.setText(DUPLICATE_ID_TEXT);
		warningPopup.setAcceptHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				imageGrid.addCell(imageUploadPopup.getIconID(), 
						  imageUploadPopup.getImage(), true);
				imageUploadPopup.hide();
				warningPopup.hide();
			}
		});
		
		// define image uploader popup
		imageUploadPopup = new ImageUploadPopup(jsCaller, imageGrid);
		imageUploadPopup.addAcceptHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				final String iconID = imageUploadPopup.getIconID();
				
				// check if id exists already
				if(!isEditingCell && imageGrid.getCell(iconID) != null){
					warningPopup.center();
				}
				else{
					imageGrid.addCell(iconID, imageUploadPopup.getImage(), true);
					imageUploadPopup.hide();
				}
			}
		});
		
		final VerticalPanel mainPanel = new VerticalPanel();
		super.setContent(mainPanel);
		
		// image grid
		final int imageGridWidth = 5*(ICON_CELL_WIDTH + 5);
		{
			final ScrollPanel gridPanel = new ScrollPanel();
			gridPanel.setStyleName("noPadding");
			gridPanel.setSize(imageGridWidth+"px", 3*ICON_CELL_WIDTH+"px");
			gridPanel.add(imageGrid);
			mainPanel.add(gridPanel);
		}
		// file edit
		{
			
			removeCellButton = new Button(REMOVE_BUTTON_TEXT);
			removeCellButton.setEnabled(false);
			removeCellButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					final String iconID = imageGrid.getSelectedIconID();
					imageGrid.unselectCell();
					imageGrid.removeCell(iconID);
					removeCellButton.setEnabled(false);
					editCellButton.setEnabled(false);
				}});
			
			editCellButton = new Button(EDIT_BUTTON_TEXT);
			editCellButton.setEnabled(false);
			editCellButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					isEditingCell = true;
					imageUploadPopup.center(imageGrid.getSelectedIconID(), imageGrid.getSelectedIconURL());
				}});
			
			addCellButton = new Button(CREATE_BUTTON_TEXT);
			addCellButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					isEditingCell = false;
					imageUploadPopup.center();
				}});
			
			// minor layout changes
			addCellButton.getElement().getStyle().setMarginLeft(1.65, Unit.EM);
			addCellButton.getElement().getStyle().setMarginRight(0, Unit.EM);
			
			// assemble components
			final HorizontalPanel filePanel = new HorizontalPanel();
			//filePanel.setStyleName("popupButtonPanel");
			filePanel.add(removeCellButton);
			filePanel.add(editCellButton);
			filePanel.add(addCellButton);
			mainPanel.add(filePanel);
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
		    super.addBottomWidget(this.okButton);
		}
	}
	
	/**
	 * 
	 * @return the ImageGrid.
	 */
	public ImageGrid getImageGrid(){
		return imageGrid;
	}
	
	/**
	 * Shows and centers the IconBrowser and reloads the icons.
	 */
	public void center(){
		imageGrid.unselectCell();
		removeCellButton.setEnabled(false);
		editCellButton.setEnabled(false);
		super.center();
	}
	
	/**
	 * Adds a ClickHandler to the OK-Button.
	 * @param accept the ClickHandler
	 */
	public void addAcceptHandler(ClickHandler accept){
		okButton.addClickHandler(accept);
	}
	
	/**
	 * 
	 * @param url the URL of the image
	 * @return the icon id that is linked to the URL
	 */
	public String getIconID(final String url){
		return imageGrid.getIconID(url);
	}
	
	/**
	 * 
	 * @return the URL of the selected cell
	 */
	public String getSelectedURL(){
		return imageGrid.getSelectedIconURL();
	}
	
	/**
	 * 
	 * @return the icon id of the selected cell
	 */
	public String getSelectedID(){
		return imageGrid.getSelectedIconID();
	}
	
	/**
	 * Removes all icons from the grid.
	 */
	public void clear(){
		imageGrid.clear();
		removeCellButton.setEnabled(false);
		editCellButton.setEnabled(false);
		
	}
	 
	/**
	 * Adds a group of icon id / url pairs to the imageGrid.
	 * 
	 * @param iconIDs an array of icon ids
	 * @param urls an array of urls
	 * @return true if the icons were added successfully
	 */
	public boolean addIcons(final String[] iconIDs, final String[] urls){
		
		final int l = iconIDs.length;
		
		// if the number of iconIDs differs from the number of urls,
		// we have invalid input
		if(l != urls.length){
			return false;
		}
		
		Image iconTemp;
		for(int i = 0; i < l; i++){
			iconTemp = new Image(urls[i]);
			imageGrid.addCell(iconIDs[i], iconTemp, false);
		}
		return true;
	}
}
