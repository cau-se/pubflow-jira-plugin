package de.pubflow.graph.common;

import com.google.gwt.user.client.ui.TreeItem;

/**
 * A small extension of TreeItems, that has a String-ID.
 * @author Robin Weiss
 *
 */
public class NodeTreeItem extends TreeItem{
	
	public final String id;
	
	public NodeTreeItem(final String id, final String name){
		super();
		super.setText(name);
		this.id = id;
	}
}
