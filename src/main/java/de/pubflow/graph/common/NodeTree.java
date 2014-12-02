package de.pubflow.graph.common;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import de.pubflow.graph.client.JSCaller;

/**
 * A Tree of NodeTreeItems, that can select graph nodes upon clicking an item.
 * @author Robin Weiss
 *
 */
public class NodeTree extends Tree{
	
	
	public NodeTree(final JSCaller jsCaller){
		super();
		
		this.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				final String id = ((NodeTreeItem) event.getSelectedItem()).id;
				jsCaller.markNode(id);
			}
		});
	}
	
	/**
	 * Adds an item to the tree.
	 * @param nodeID the id of the item
	 * @param name the displayed name of the item
	 */
	public void addItem(final String nodeID, final String name){
		final NodeTreeItem item = new NodeTreeItem(nodeID, name);
		super.addItem(item);
	}
	
	/**
	 * Removes an item from the tree.
	 * @param nodeID the id of the item
	 */
	public void removeItem(final String nodeID){
		final NodeTreeItem item = getItemById(nodeID);
		if(item != null){
			super.removeItem(item);
		}
	}
	
	/**
	 * Changes the name of an item.
	 * @param nodeID the id of the item
	 * @param name the displayed name of the item
	 */
	public void setItemName(final String nodeID, final String name){
		final TreeItem item = super.getSelectedItem();
		item.setText(name);
	}
	
	/**
	 * Selects an item with the specified id.
	 * @param id the id of the item
	 */
	public void selectID(final String id){
		final NodeTreeItem item = getItemById(id);
		// TODO: remove warning: [WARN] [pubflow_graphviewer]
		// Something other than an int was returned from JSNI method
		// Rounding double (99.1875) to int for int 
		super.setSelectedItem(item, false);
	}
	
	/**
	 * Looks for an item with the specified id.
	 * @param id the id of the item
	 * @return the item or null if no such item exists.
	 */
	public NodeTreeItem getItemById(final String id){
		if(id.equals("#null")){
			return null;
		}
		
		String sId;
		NodeTreeItem item;
		for(int i = 0, l = super.getItemCount(); i < l; i++){
			item = (NodeTreeItem) super.getItem(i);
			sId = item.id;
			
			if(sId.equals(id)){
				return item;
			}
		}
		
		return null;
	}
}
