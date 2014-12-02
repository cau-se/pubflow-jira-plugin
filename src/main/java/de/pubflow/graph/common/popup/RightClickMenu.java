package de.pubflow.graph.common.popup;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

import de.pubflow.graph.client.JSCaller;

/**
 * A popup menu that appears upon right clicking anywhere on the graph.
 * @author Robin Weiss
 *
 */
public class RightClickMenu extends PopupPanel{
	private static final String ADD_NODE_TEXT = "Add Node";
	private static final String DELETE_TEXT = "Delete";
	private static final String EDIT_TEXT = "Edit";
	private static final String CLONE_TEXT = "Clone";

	private final NodeEditPopup nodeEditor;
	
	private final MenuBar content;
	private final MenuItem cloneButton;
	private final MenuItem addButton;
	private final MenuItem editButton;
	private final MenuItem deleteButton;

	private boolean isEditingNode;
	private String clickedID;
	
	/**
	 * Constructor that defines a RightClickMenu with buttons and tied commands
	 * @param nodeEditor the nodeEditor that pops up if a node is edited or created
	 */
	public RightClickMenu(final JSCaller jsCaller, final NodeEditPopup nodeEditor){
		super();
		this.content = new MenuBar(true);
		this.nodeEditor = nodeEditor;
		
		// define commands
		final Command cmdAdd = new Command() {
			public void execute() {
				nodeEditor.clearFields();
				nodeEditor.center();
				hide();
			}
		};
		final Command cmdEdit = new Command() {
			public void execute() {
				if(isEditingNode){
					nodeEditor.setEdit(true);
					nodeEditor.center();
				}else{
					if(clickedID == null){
						
					}else{
						jsCaller.markNode(clickedID);
					}
				}
				hide();
			}
		};
		final Command cmdDelete = new Command() {
			public void execute() {
				nodeEditor.deleteObject();
				hide();
			}
		};
		final Command cmdClone = new Command() {
			public void execute() {
				nodeEditor.setEdit(false);
				nodeEditor.center();
				hide();
			}
		};
		
		addButton = new MenuItem(ADD_NODE_TEXT, cmdAdd);
		cloneButton = new MenuItem(CLONE_TEXT, cmdClone);
		editButton = new MenuItem(EDIT_TEXT, cmdEdit);
		deleteButton = new MenuItem(DELETE_TEXT, cmdDelete);
		
		content.addItem(editButton);
		content.addItem(cloneButton);
		content.addItem(addButton);
		content.addItem(deleteButton);
		super.add(content);
	}
	
	
	/**
	 * Shows the RightClickMenu at the specified position, when clicking on a node.
	 * @param id the id of the clicked node
	 * @param name the displayed name of the clicked node
	 * @param iconID the iconID of the clicked node
	 * @param inputPorts the number of input ports of the clicked node
	 * @param outputPorts the number of output ports of the clicked node
	 * @param x the horizontal position where the RightClickMenu should appear
	 * @param y the vertical position where the RightClickMenu should appear
	 */
	public void showForNode(final String id, final String name, final String iconID, 
			final int inputPorts, final int outputPorts, final int x, final int y){
		cloneButton.setVisible(true);
		editButton.setVisible(true);
		deleteButton.setVisible(true);
		
		isEditingNode = true;
		clickedID = id;
		loadNodeData(id, name, iconID, inputPorts, outputPorts);
		show(x, y);
	}
	
	
	/**
	 * Shows the RightClickMenu at the specified position, when clicking on an edge.
	 * @param nodeFrom the id of the source node of the edge
	 * @param nodeTo the id of the target node of the edge
	 * @param label the displayed label of the edge
	 * @param x the horizontal position where the RightClickMenu should appear
	 * @param y the vertical position where the RightClickMenu should appear
	 */
	public void showForEdge(final String nodeFrom, final String nodeTo, final String label, 
			final int x, final int y){
		cloneButton.setVisible(false);
		editButton.setVisible(false);
		deleteButton.setVisible(true);
		
		isEditingNode = false;
		clickedID = null;
		// memorize the ids of the edge ports
		nodeEditor.setEditedObject(nodeFrom , nodeTo);
		show(x, y);
	}
	
	
	/**
	 * Shows the RightClickMenu at the specified position, when clicking on a port.
	 * @param id the id of the clicked port
	 * @param symbol the displayed short string of the clicked port
	 * @param x the horizontal position where the RightClickMenu should appear
	 * @param y the vertical position where the RightClickMenu should appear
	 */
	public void showForPort(final String id, final String symbol, final int x, final int y){
		cloneButton.setVisible(false);
		editButton.setVisible(true);
		deleteButton.setVisible(false);
		
		isEditingNode = false;
		clickedID = id;
		// memorize the id of the clicked port
		nodeEditor.setEditedObject(id , null);
		show(x, y);
	}
	
	
	/**
	 * Shows the RightClickMenu at the specified position, when clicking on an empty spot on the canvas.
	 * @param x the horizontal position where the RightClickMenu should appear
	 * @param y the vertical position where the RightClickMenu should appear
	 */
	public void showForVoid(final int x, final int y){
		cloneButton.setVisible(false);
		editButton.setVisible(false);
		deleteButton.setVisible(false);
		nodeEditor.clearFields();
		show(x, y);
	}
	
	
	/**
	 * Shows the right click menu at the specified position
	 * @param x x-coordinate of the top left corner of the menu
	 * @param y y-coordinate of the top left corner of the menu
	 */
	public void show(int x, int y){
		
		int menuHeight = super.getOffsetHeight();
		if(y > Window.getClientHeight() - menuHeight){
			y -= menuHeight;
		}else{
			y++;
		}
		int menuWidth = super.getOffsetWidth();
		if(x > Window.getClientWidth() - menuWidth){
			x -= menuWidth;
		}else{
			x++;
		}
		
		super.setPopupPosition(x, y);
		super.show();
	}
	
	/**
	 * Fills the fields of the node editor with data.
	 * @param id the id of the node
	 * @param name the displayed name of the node
	 * @param iconID the iconID of the node
	 * @param inputPorts the number of input ports of the node
	 * @param outputPorts the number of input ports of the node
	 */
	public void loadNodeData(final String id, final String name, final String iconID, 
			final int inputPorts, final int outputPorts){
		// memorize the id of the marked node
		nodeEditor.setEditedObject(id , null);
		// get icon name from url
		nodeEditor.setFields(name, iconID, inputPorts, outputPorts);
	}
}
