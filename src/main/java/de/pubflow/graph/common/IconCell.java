package de.pubflow.graph.common;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A fixed-size VerticalPanel that contains a resized Image and an identifier for the image.
 * The superclass' title acts as the iconID.
 * @author Robin Weiss
 *
 */
public class IconCell extends VerticalPanel{
	
	private static final String EMPTY_ICON_TEXT = "<no icon>";
	private final static String CSS_CELL = "imageGridCell";
	private final static String CSS_CELL_SELECTED = "imageGridCellSelected";
	private final static String CSS_LABEL = "imageGridLabel";
	
	private ImageHolder iconHolder;
	private Label label;
	private String url;
	private boolean isEmpty;
	
	public IconCell(final int width, final int height){
		super();
		super.setStyleName(CSS_CELL);
		super.setTitle(EMPTY_ICON_TEXT);
		
		// add image
		iconHolder = new ImageHolder(width);
		super.add(iconHolder);
		isEmpty = true;
		
		// cull displayed String if it is too long
		label = 
			new Label(IconCell.cullString(EMPTY_ICON_TEXT, 10));
		label.setStyleName(CSS_LABEL);
		label.setStyleName("unselectable", true);
		super.add(label);
	}
	
	/**
	 * Specific constructor for IconCells.
	 * @param icon The image that is displayed
	 * @param title The mouse-over text of the cell
	 * @param width The cell width
	 * @param height The cell height
	 */
	public IconCell(final Image icon, final String iconID, final int width, final int height){
		super();
		super.setStyleName(CSS_CELL);
		super.setTitle(iconID);
		
		// add image
		iconHolder = new ImageHolder(width);
		iconHolder.setImage(icon);
		super.add(iconHolder);
		isEmpty = false;
		
		// save url
		url = icon.getUrl();
		
		// cull displayed String if it is too long
		label = 
			new Label(IconCell.cullString(iconID, 10));
		label.setStyleName(CSS_LABEL);
		//label.getElement().getStyle().setFontSize(labelSize, Unit.PX);
		super.add(label);
	}
	
	/**
	 * 
	 * @return true if no image is set
	 */
	public boolean isEmpty(){
		return isEmpty;
	}
	
	/**
	 * 
	 * @param input The String which is to be culled
	 * @param maxChars The maximum amount of chars before the String is cut off
	 * @return The input String or the first few characters appended with dots
	 */
	public static String cullString(final String input, final int maxChars){
		return (input.length() <= maxChars) 
				? input 
				: input.substring(0, maxChars - 3) + "...";
	}
	
	/**
	 * 
	 * @return The URL of the image contained in the ImageHolder.
	 */
	public String getURL(){
		return url;
	}
	
	/**
	 * Replaces the existing image, contained in the ImageHolder, with a new image.
	 * @param newIcon The new image
	 */
	public void setIcon(final Image newIcon){
		iconHolder.setImage(newIcon);
		url = newIcon.getUrl();
		isEmpty = false;
	}
	
	/**
	 * Replaces the iconID.
	 * @param newID The new iconID
	 */
	public void setIconID(final String newID){
		label.setText(IconCell.cullString(newID, 10));
		super.setTitle(newID);
	}
	
	/**
	 * Replaces the iconID and the image.
	 * @param newIcon The new image
	 * @param newID The new iconID
	 */
	public void setData(final Image newIcon, final String newID){
		setIcon(newIcon);
		setIconID(newID);
		isEmpty = false;
	}
	
	/**
	 * Replaces the iconID and the image.
	 * @param other Another IconCell, providing the data
	 */
	public void setData(final IconCell other){
		if(other == null){
			clear();
			return;
		}
		setIcon(other.iconHolder.getImage());
		setIconID(other.getTitle());
		isEmpty = other.isEmpty;
	}
	
	/**
	 * Sets the cell to an empty state without an image and the standard
	 * text.
	 */
	public void clear(){
		label.setText(IconCell.cullString(EMPTY_ICON_TEXT, 10));
		super.setTitle(EMPTY_ICON_TEXT);
		iconHolder.setImage(null);
		url = null;
		isEmpty = true;
	}
	
	/**
	 * Highlights the cell, changing its style.
	 */
	public void setStyleSelected(){
		setStyleName(CSS_CELL_SELECTED);
		label.getElement().getStyle().setColor("white");
	}
	
	/**
	 * Unhighlights the cell, restoring its style.
	 */
	public void setStyleUnselected(){
		setStyleName(CSS_CELL);
		label.getElement().getStyle().setColor("black");
	}
	
}
