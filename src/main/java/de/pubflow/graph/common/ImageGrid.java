package de.pubflow.graph.common;

import java.util.LinkedList;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import de.pubflow.graph.client.JSCaller;

public class ImageGrid extends Grid{
	
	private final JSCaller jsCaller;
	private final LinkedList<IconCell> iconList;
	public final int labelSize;
	public final int iconSize;
	private final int maxCols;
	private IconCell selected;
	
	public ImageGrid(final int cols, final int cellWidth, JSCaller jsCaller){
		super(1,1);
		super.setVisible(false);
		this.jsCaller = jsCaller;
		
		maxCols = cols;
		iconSize = (int) (cellWidth * 0.8);
		labelSize = (int) (cellWidth * 0.15);
		selected = null;
		iconList = new LinkedList<IconCell>();
		super.setCellPadding(0);
		super.setCellSpacing(0);
		super.setBorderWidth(1);
		super.setStyleName("imageGrid");
	}
	
	/**
	 * @param url - the URL of an existing image
	 * @return the first iconID that matches the url
	 */
	public String getIconID(final String url){
		final IconCell cell = getCellByURL(url);
		
		return (cell == null) ? null : cell.getTitle();
	}
	
	/**
	 * 
	 * @return The selected cell
	 */
	public IconCell getSelectedCell(){
		return selected;
	}
	
	/**
	 * @return the icon URL of the selected icon or an empty string if no icon is selected.
	 */
	public String getSelectedIconURL(){
		if(selected == null){
			return "";
		}
		return selected.getURL();
	}
	
	/**
	 * @return the icon id of the selected icon or an empty string if no icon is selected.
	 */
	public String getSelectedIconID(){
		if(selected == null){
			return "none";
		}
		return selected.getTitle();
	}
	
	/**
	 * 
	 * @param iconID the id or title of the IconCell
	 * @return the cell with the specified iconID or null if no such cell exists
	 */
	public IconCell getCell(final String iconID){
		
		IconCell cell;
		for(int i = 0, l = iconList.size(); i < l; i++){
			cell = iconList.get(i);
			if(iconID.equals(cell.getTitle())){
				return cell;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param url The URL of the image within an IconCell
	 * @return The cell with the specified URL or null if no such cell exists
	 */
	private IconCell getCellByURL(final String url){
		
		IconCell cell;
		for(int i = 0, l = iconList.size(); i < l; i++){
			cell = iconList.get(i);
			if(url.equals(cell.getURL())){
				return cell;
			}
		}
		return null;
	}
	
	/**
	 * Adds or changes a cell within the ImageGrid.
	 * @param iconID The id tied to an image
	 * @param icon The image
	 * @param addToGraph if true, adds the nodeIcon in Javascript
	 */
	public void addCell(final String iconID, final Image icon, final boolean addToGraph){

		int numElements = iconList.size();
		
		// look through the list to see if the iconID already exists
		IconCell cell = getCell(iconID);
		if(cell != null){
			cell.setIcon(icon);
		}
		// a new cell must be appended to the grid
		else{
			// save cell in list for easy access
			cell = new IconCell(icon, iconID, iconSize, (int)(1.5 * iconSize));
			iconList.add(cell);
			numElements++;
			
			// make room for the new cell
			final int rows = (int) Math.ceil(numElements / (float) maxCols);
			final int cols = Math.min(maxCols, numElements);
			super.resize(rows, cols);
			
			// add cell to grid
			super.setWidget(rows - 1, (numElements - 1) % maxCols, cell);
			super.setVisible(true);
		}
		
		// update the graph itself
		if(addToGraph){
			jsCaller.setNodeIcon(iconID, icon.getUrl());
		}
	}
	
	/**
	 * Removes an Entry.
	 * @param iconID - the key of the Entry
	 */
	public void removeCell(final String iconID){

		int cellNum = 0;
		
		IconCell cell;
		for(int l = iconList.size(); cellNum < l; cellNum++){
			cell = iconList.get(cellNum);
			
			if(iconID.equals(cell.getTitle())){
				
				iconList.remove(cellNum);
				super.remove(cell);
				
				if(iconList.isEmpty()){
					super.setVisible(false);
					return;
				}
				
				// update the graph itself
				jsCaller.removeNodeIcon(iconID);
				
				// shrink the grid to the new size
				l--;
				for(int c = cellNum; c < l; c++){
					int row = c / maxCols;
					int col = c % maxCols;
					
					super.setWidget(row, col, iconList.get(c));
				}
				
				final int rows = (int) Math.ceil(l / (float) maxCols);
				final int cols = Math.min(maxCols, l);
				super.resize(rows, cols);
				
				return;
			}
		}
	}
	
	/**
	 * Removes all cells from the grid.
	 */
	public void clear(){
		unselectCell();
		iconList.clear();
		super.setVisible(false);
		super.clear();
		super.resize(1, 1);
	}

	/**
	 * Marks a cell within the grid.
	 * @param iconID - the key of the url entry
	 */
	public void selectCell(final String iconID){
		
		//final Set<Entry<String, String>> entries = typeToUrlMap.entrySet();
		boolean noHit = true;
		
		IconCell cell;
		int cellNum = 0;
		for(int l = iconList.size(); cellNum < l; cellNum++){
			cell = iconList.get(cellNum);
			if(iconID.equals(cell.getTitle())){
				noHit = false;
				break;
			}
		}
		
		// unselect if iconID is not present
		if(noHit){
			unselectCell();
			return;
		}
		
		final int row = cellNum / super.numColumns;
		final int col = cellNum % super.numColumns;
		
		selectCell(row, col);
	}
	
	/**
	 * Marks a cell within the grid.
	 * @param cell the cell which is to be selected
	 */
	public void selectCell(final HTMLTable.Cell cell){
		if(cell == null){
			unselectCell();
			return;
		}
		final int row = cell.getRowIndex();
		final int col = cell.getCellIndex();
		selectCell(row, col);
	}
	
	/**
	 * Marks a cell within the grid.
	 * @param row the row of the cell
	 * @param col the col of the cell
	 */
	public void selectCell(final int row, final int col){
		// do nothing if out of bounds
		if(row >= super.numRows || col >= super.numColumns){
			return;
		}
		if(row < 0 || col < 0){
			return;
		}
		
		Widget newSelected = super.getWidget(row, col);
		final boolean select = (selected != newSelected);
		unselectCell();
		
		// select only if we did not select the same cell twice
		if(select && newSelected != null){
			selected = (IconCell) newSelected;
			selected.setStyleSelected();
		}
	}
	
	/**
	 * Unselects whichever cell is selected.
	 */
	public void unselectCell(){
		if(selected != null){
			selected.setStyleUnselected();
			selected = null;
		}
	}
	
}
