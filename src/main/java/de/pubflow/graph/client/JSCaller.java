package de.pubflow.graph.client;


import java.util.Stack;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

import de.pubflow.graph.common.NodePropertiesTable;
import de.pubflow.graph.common.NodeTree;
import de.pubflow.graph.common.popup.IconBrowserPopup;
import de.pubflow.graph.common.popup.RightClickMenu;

/**
 * This class serves as the main interface between the Java code and the JavaScript code.
 * It is instantiated to allow switching between multiple graphs in the future.
 * @author Robin Weiss
 *
 */
public class JSCaller {
	protected final double zoomInFactor;
	protected final double zoomOutFactor;
	protected int gridSize;
	protected boolean animation;
	protected boolean locked;
	protected final String graphName;
	protected boolean unsavedChanges;
	
	protected RightClickMenu rightClickMenu;
	protected NodeTree nodeTree;
	protected NodePropertiesTable nodePropsTable;
	protected final PubFlow_GraphViewer gui;
	
	/**
	 * Constructor that requires a javascript variable name and the client gui to allow for
	 * the most basic communication.
	 * @param graphName the name of the graph variable
	 * @param gui the user interface which is shown on the client side
	 */
	public JSCaller(final String graphName, final PubFlow_GraphViewer gui){
		
		// initialize parameters with standard values
		this.graphName = graphName;
		this.gridSize = 48;
		this.animation = false;
		this.locked = false;
		this.zoomInFactor = 1.5;
		this.zoomOutFactor = 2.0 / 3.0;
		this.unsavedChanges = false;
		
		this.gui = gui;
	}
	
	/**
	 * Attempts to save the graph in a text file, using a servlet.
	 * 
	 * @param asXML if true, the graph is saved as a XML file
	 */
	public void saveGraph(final boolean asXML){
		final String jsonString = saveGraphNative(asXML);
		
		final String url = GWT.getModuleBaseURL() + "downloadServlet?fileName=graph." + ((asXML) ? "xml" : "txt");
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);

	    try {
	      /*Request response =*/ builder.sendRequest(jsonString, new RequestCallback() {

	        public void onError(Request request, Throwable exception) {
	        	Window.alert("Failed to save graph file: " + exception.getMessage());
	        }

			@Override
			public void onResponseReceived(Request request, Response response) {
				Window.Location.replace(url);
				unsavedChanges = false;
			}
	      });
	    } catch (RequestException e) {
	      Window.alert("Failed to save graph file: " + e.getMessage());
	    }
	}
	
	/**
	 * Loads a graph from a JSON or XML string.
	 * @param saveString a string containing graph data
	 * @param iconBrowser the iconBrowser where the icons need to be added
	 */
	public void loadGraph(final String saveString, final IconBrowserPopup iconBrowser, final boolean asXML){
		// disable animation while loading
		setAnimationNative(false);
		
		deleteAll();
		setGhostNodesEnabled(false);
		loadGraphNative(saveString, asXML);
		setGhostNodesEnabled(true);
		unsavedChanges = false;
		setAnimationNative(animation);
		
		// get icon-url-pairs
		final JsArrayString jso = getNodeIconsNative();
		
		if(jso.length() % 2 != 0){
			return;
		}
		
		final int iconCount = jso.length() / 2;
		
		final String[] nodeIDs = new String[iconCount];
		final String[] URLs = new String[iconCount];
		for(int t = 0; t < iconCount; t++){
			nodeIDs[t] = jso.get(t);
			URLs[t] = jso.get(t + iconCount);
		}
				
		// update iconBrowser
		iconBrowser.clear();
		iconBrowser.addIcons(nodeIDs, URLs);
	}

	
	/**
	 * Extracts data from a graph node and writes it to the NodePropertiesTable.
	 * @param nodeID the node from which the data is loaded
	 */
	public void loadNodeProperties(final String nodeID){
		
		final JsArrayString nodeData = getNodePropertiesNative(nodeID);
		
		if(nodeData == null){
			nodePropsTable.clear();
			return;
		}
		// return if the node is already loaded
		if(nodePropsTable.isNodeBeingEdited(nodeID)){
			return;
		}
		
		if(nodeData.length() % 2 != 0){
			return;
		}
		
		final int dataCount = nodeData.length() / 2;
		
		final String[] keys = new String[dataCount];
		final String[] values = new String[dataCount];
		for(int t = 0; t < dataCount; t++){
			keys[t] = nodeData.get(t);
			values[t] = (String) nodeData.get(t + dataCount).toString();
		}
		
		// set data in table
		nodePropsTable.setKeyValuePairs(nodeID, keys, values);
	}
	
	/**
	 * Overwrites the properties of a node.
	 * @param nodeID the id of the node of which the data is changed
	 * @param keys an array of keys
	 * @param values an array of values, corresponding to the keys
	 */
	public void setNodeProperties(final String nodeID, final String[] keys, final String[] values){
		
		final int dataCount = keys.length;
		if(dataCount != values.length){
			return;
		}
		
		final JsArrayString keysJS = (JsArrayString) JsArrayString.createArray();
		keysJS.setLength(dataCount);
		final JsArrayString valuesJS = (JsArrayString) JsArrayString.createArray();
		valuesJS.setLength(dataCount);
		
		for(int i = 0; i < dataCount; i++){
			keysJS.set(i, keys[i]);
			valuesJS.set(i, values[i]);
		}
		
		setNodePropertiesNative(nodeID, keysJS, valuesJS, dataCount);
	}
	
	
	/**
	 * Removes properties of a node.
	 * @param nodeID the id of the node of which the data is deleted
	 * @param keys an array of keys that are to be deleted
	 */
	public void removeNodeProperties(final String nodeID, final Stack<String> deletionStack) {
		final JsArrayString keysJS = (JsArrayString) JsArrayString.createArray();
		keysJS.setLength(deletionStack.size());
		
		int i = 0;
		while(!deletionStack.isEmpty()){
			keysJS.set(i++, deletionStack.pop());
		}
		
		removeNodePropertiesNative(nodeID, keysJS);
	}
	
	
	/**
	 * @return true if the graph has been manipulated since the last save
	 */
	public boolean hasUnsavedChanges(){
		return unsavedChanges;
	}
	
	/**
	 * Marks a node visually in the graph. Also marks the node in the NodeTree and
	 * shows its data in the NodePropertiesTable.
	 * @param nodeID the id of the marked node
	 */
	public void markNode(final String nodeID){
		nodeTree.selectID(nodeID);
		markNodeNative(nodeID);
		loadNodeProperties(nodeID);
	}
	
	/**
	 * @return true if a node is marked
	 */
	public boolean isNodeMarked(){
		return isNodeMarkedNative();
	}
	
	/**
	 * Doubles the grid size.
	 */
	public void increaseGridSize(){
		if(gridSize < 1536){
			gridSize *= 2;
		}
		setGridSizeNative(gridSize);
	}
	
	/**
	 * Halves the grid size.
	 */
	public void decreaseGridSize(){
		if(gridSize > 2){
			gridSize /= 2;
		}
		setGridSizeNative(gridSize);
	}
	
	/**
	 * Toggles graph animation.
	 * @return the new state of the boolean value
	 */
	public boolean toggleAnimation(){
		animation = !animation;
		setAnimationNative(animation);
		return animation;
	}
	
	/**
	 * Toggles graph readOnly mode.
	 * @return the new state of the boolean value
	 */
	public boolean toggleLocked(){
		locked = !locked;
		setLockedNative(locked);
		return locked;
	}
	
	/**
	 * Zooms out.
	 */
	public void zoomOut(){
		zoomNative(zoomOutFactor);
	}
	
	/**
	 * Zooms in.
	 */
	public void zoomIn(){
		zoomNative(zoomInFactor);
	}
	
	/**
	 * Removes all nodes from the graph.
	 */
	public void deleteAll(){
		removeAllNative();
	}
	
	/**
	 * Removes all nodes from the graph, but sets
	 * unchanged to false.
	 */
	public void newGraph(){
		deleteAll();
		unsavedChanges = false;
	}
	
	/** Adds a node with ports to the graph.
	 * @param x the horizontal position of the node
	 * @param y the vertical position of the node
	 * @param label the displayed name of the node
	 * @param inputCount the number of input ports or an empty String
	 * @param outputCount the number of output ports or an empty String
	 * @param repositoryCount the number of repository ports or an empty String
	 * @param iconID the ID of the displayed icon (nodeType)
	 */
	public void addNode(final int x, final int y, final String label, final String inputCount, final String outputCount, final String iconID){
		final int inputInt = (inputCount.equals("")) ? 0 : Integer.parseInt(inputCount);
		final int outputInt = (outputCount.equals("")) ? 0 : Integer.parseInt(outputCount);
		addNodeNative(x, y, label, inputInt, outputInt, 0, iconID);
	}
	
	/** Adds a node with ports to the center of the graph.
	 * @param label the displayed name of the node
	 * @param inputCount the number of input ports or an empty String
	 * @param outputCount the number of output ports or an empty String
	 * @param repositoryCount the number of repository ports or an empty String
	 * @param iconID the ID of the displayed icon (nodeType)
	 */
	public void addNodeInCenter(final String label, final String inputCount, final String outputCount, final String iconID){
		final int inputInt = (inputCount.equals("")) ? 0 : Integer.parseInt(inputCount);
		final int outputInt = (outputCount.equals("")) ? 0 : Integer.parseInt(outputCount);
		
		addNodeNative(label, inputInt, outputInt, 0, iconID);
	}
	
	/**
	 * Links an iconID with an image. All Nodes that share this iconID
	 * display the very same icon.
	 * @param iconID - unique ID-String
	 * @param iconURL - an URL that targets an image
	 */
	public void setNodeIcon(final String iconID, final String iconURL){
		setNodeIconNative(iconID, iconURL);
	}
	
	/**
	 * Removes all icons with the specified iconID.
	 * @param iconID - unique ID-String
	 */
	public void removeNodeIcon(final String iconID){
		setNodeIconNative(iconID, null);
	}
	
	/**
	 * Defines Java methods that can be called from within JavaScript code.
	 * @param nodeTree the NodeTree Object that can be manipulated from Javascript
	 * @param rightClickMenu the menu that appears upon right clicking the canvas 
	 */
	protected void initJavaScriptInterface(final NodeTree nodeTree, final RightClickMenu rightClickMenu){
		initJavaScriptInterfaceNative(nodeTree, rightClickMenu);
	}
	
	  						////////////////////
	////////////////////////// NATIVE METHODS /////////////////////////////////
							////////////////////
	
	/**
	 * Defines Java methods that can be called from within JavaScript code.
	 * @param nodeTree the NodeTree Object that can be manipulated from Javascript
	 * @param rightClickMenu the menu that appears upon right clicking the canvas 
	 */
	private native void initJavaScriptInterfaceNative(final NodeTree nodeTree, final RightClickMenu rightClickMenu)/*-{
		var g = $wnd[this.@de.pubflow.graph.client.JSCaller::graphName];
		var gui = this.@de.pubflow.graph.client.JSCaller::gui;
		var that = this;
		
		// converts the string arrays from displayed names
		// to a single string
		var arrToStr = function(arr){
			var str = "";
			if(!arr){
				return str;
			}
			var a, l;
			for(a = 0, l = arr.length - 1; a < l; a++){
				str += arr[a] + "\n";
			}
			str += arr[a];
			
			return str;
		};
		
		g.gwtExt.notification = function(message, duration){
			@de.pubflow.graph.common.Notification::display(Ljava/lang/String;I)(message, duration);
		};
		
		g.gwtExt.zoomIn = function(){
			that.@de.pubflow.graph.client.JSCaller::zoomIn()();
		};
		
		g.gwtExt.zoomOut = function(){
			that.@de.pubflow.graph.client.JSCaller::zoomOut()();
		};
		
		this.@de.pubflow.graph.client.JSCaller::rightClickMenu = rightClickMenu;
		var rcm = this.@de.pubflow.graph.client.JSCaller::rightClickMenu;
		
		g.gwtExt.loadMarkedNodeData = function(){
			var node = $wnd[g].getMarkedNode();
			if(!node){ return;}
			
			var data = node.data;
			var id = node.id;
			var name = arrToStr(node.name);
			var iconID = (data.$icon) ? data.$nodeType : "";
			var inputPorts = 0;
			var outputPorts = 0;
			var children = data.$children;
			// count ports
			for(var t in children){
				t = children[t].data.$type;
				if(t == "inputPort"){
					inputPorts++;
				}
				else if(t == "outputPort"){
					outputPorts++;
				}
			}
			rcm.@de.pubflow.graph.common.popup.RightClickMenu::loadNodeData(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)
				(id, name, iconID, inputPorts, outputPorts);
		};
		
		g.gwtExt.hideRightClickMenu = function(){
			rcm.@de.pubflow.graph.common.popup.RightClickMenu::hide()();
		};
		
		g.gwtExt.showRightClickMenu = function(node, x, y){
			
			if(node){
				var data = node.data;
				if(node.nodeFrom){
					// is edge
					var nodeFrom = node.nodeFrom.id;
					var nodeTo = node.nodeTo.id;
					var label = arrToStr(node.data.$label);
					rcm.@de.pubflow.graph.common.popup.RightClickMenu::showForEdge(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)
						(nodeFrom, nodeTo, label, x, y);
					
				}else if(data.$type == "inputPort" || data.$type == "outputPort" || data.$type == "repositoryPort"){
					// is port	
					var id = node.id;
					var symbol = data.$symbol;
					rcm.@de.pubflow.graph.common.popup.RightClickMenu::showForPort(Ljava/lang/String;Ljava/lang/String;II)
						(id, symbol, x, y);
					
				}else if(data.$type == "nodeFamily"){
					// is family
					var id = node.id;
					var name = arrToStr(node.name);
					var iconID = (data.$icon) ? data.$nodeType : "";
					var inputPorts = 0;
					var outputPorts = 0;
					var children = data.$children;
					// count ports
					for(var t in children){
						t = children[t].data.$type;
						if(t == "inputPort"){
							inputPorts++;
						}
						else if(t == "outputPort"){
							outputPorts++;
						}
					}
					rcm.@de.pubflow.graph.common.popup.RightClickMenu::showForNode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIII)
						(id, name, iconID, inputPorts, outputPorts, x, y);
				}
				// TODO: implement custom nodes
				return;
    		}
    		rcm.@de.pubflow.graph.common.popup.RightClickMenu::showForVoid(II)(x, y);
		};
		
		// nodeTree communication
		this.@de.pubflow.graph.client.JSCaller::nodeTree = nodeTree;
		var np = this.@de.pubflow.graph.client.JSCaller::nodePropsTable;
		
		g.gwtExt.setChanged = function(){
			that.@de.pubflow.graph.client.JSCaller::unsavedChanges = true;
		};
		g.gwtExt.addTreeNode = function(id, nameArray){
			var name = (nameArray) ? nameArray.join(" | ") : "<no name>";
			nodeTree.@de.pubflow.graph.common.NodeTree::addItem(Ljava/lang/String;Ljava/lang/String;)(id, name);
		};
		g.gwtExt.selectTreeNode = function(id){
			that.@de.pubflow.graph.client.JSCaller::markNode(Ljava/lang/String;)(id);
		};
		g.gwtExt.removeTreeNode = function(id){
			nodeTree.@de.pubflow.graph.common.NodeTree::removeItem(Ljava/lang/String;)(id);
		};
		g.gwtExt.editTreeNode = function(id, nameWithLinebreaks){
			var name = nameWithLinebreaks.replace(/\n/g, " | ");
			nodeTree.@de.pubflow.graph.common.NodeTree::setItemName(Ljava/lang/String;Ljava/lang/String;)(id, name);
		};
	}-*/;
	
	
	/**
	 * Initializes the graph, adding additional Javascript to Java communication functions.
	 * @param nodeTree the list of nodes on the left hand side
	 * @param nodePropsTable the table of node properties on the right hand side
	 * @param rightClickMenu the menu that appears upon right clicking the canvas 
	 */
	public native void initGraph(final NodeTree nodeTree, final NodePropertiesTable nodePropsTable, final RightClickMenu rightClickMenu)/*-{
		this.@de.pubflow.graph.client.JSCaller::nodePropsTable = nodePropsTable;
		
		// call graph constructor
		$wnd.paintGraph();
		
		// define functions for javascript
		this.@de.pubflow.graph.client.JSCaller::initJavaScriptInterface(Lde/pubflow/graph/common/NodeTree;Lde/pubflow/graph/common/popup/RightClickMenu;)(nodeTree, rightClickMenu);
		
		// draw graph
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].updateCanvasSize();
		
		// set some properties
		$wnd[g].setGridSize(this.@de.pubflow.graph.client.JSCaller::gridSize);
		$wnd[g].setAnimation(this.@de.pubflow.graph.client.JSCaller::animation);
		var isLocked = this.@de.pubflow.graph.client.JSCaller::locked;
		$wnd[g].setReadOnly(!isLocked, !isLocked);
	}-*/;
	
	
	/**
	 * Overwrites the properties of a node.
	 * @param nodeID the id of the node of which the data is changed
	 * @param keys an array of keys
	 * @param values an array of values, corresponding to the keys
	 * @param dataCount the number of key/value pairs
	 */
	public native void setNodePropertiesNative(final String nodeID, final JsArrayString keys, final JsArrayString values, final int dataCount)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		
		// create data object
		var data = {};
		for(var i = 0; i < dataCount; i++){
			data[keys[i]] = values[i];
		}
		
		// set properties of marked node
		$wnd[g].setNodeData(nodeID, data);
	}-*/;
	
	
	/**
	 * Removes properties of a node.
	 * @param nodeID the id of the node of which the data is changed
	 * @param keys an array of keys
	 */
	public native void removeNodePropertiesNative(final String nodeID, final JsArrayString keys)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		
		// set properties of marked node
		$wnd[g].removeNodeData(nodeID, keys);
	}-*/;
	
	/**
	 * Fills the nodeEditor popup with all information, gathered from the marked node.
	 */
	public native void loadMarkedNodeDataNative()/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].gwtExt.loadMarkedNodeData();
	}-*/;
	
	/**
	 * Returns node icon data as a concatenated array.
	 * @param jsonString a String created by saving the graph
	 * @return node icon data as a concatenated array. The first half contains the icon ids
	 */
	private native JsArrayString getNodeIconsNative()/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		var arrayObject = $wnd[g].getNodeIcons();
		return arrayObject.types.concat(arrayObject.urls);
	}-*/;
	
	/**
	 * @return true if a node is marked
	 */
	private native boolean isNodeMarkedNative()/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		return !!$wnd[g].getMarkedNode();
	}-*/;
	
	
	/**
	 * Extracts the data and name of a node.
	 * @param nodeId the id of the node
	 * @return 
	 * 		a concatenated array. The first half contains keys, the second half 
	 * 		of the array contains values
	 */
	private native JsArrayString getNodePropertiesNative(final String nodeId)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		
		var node = $wnd[g].getNode(nodeId);
		
		if(!node){
			return null;
		}
		
		var keys = new Array();
		var values = new Array();
		var k = node.name;
		
		// get name
		if(k){
			keys.push("name");
			values.push(k.join(" | "));
		}
		// get data
		var data = node.data;
		var val;
		for(k in data){
			val = data[k];
			if(typeof val == "string" || typeof val == "name"){
				keys.push(k);
				
				// remove unneccesary prefix of font strings
				if(k == "$font" && val.indexOf("px ") == 0){
				val = val.substring(3, val.length);
				}
				values.push(val);
			}
		}
		
		return keys.concat(values);
	}-*/;
	
	/**
	 * Enables / Disables Ghost Nodes. If enabled, upon creating a new node, it becomes translucent and
	 * is dragged automatically. This behaviour can be bothersome when loading multiple nodes.
	 * @param state - if true, ghost nodes are enabled
	 */
	public native void setGhostNodesEnabled(final boolean state)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].gwtExt.setGhostNodesEnabled(state);
	}-*/;
	
	/**
	 * Enables / Disables Ghost Nodes. If enabled, upon creating a new node, it becomes translucent and
	 * is dragged automatically. This behaviour can be bothersome when loading multiple nodes.
	 * @param state - if true, ghost nodes are enabled
	 */
	public native void setKeyPressEnabledNative(final boolean state)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].gwtExt.setKeyPressEnabled(state);
	}-*/;
	
	/**
	 * Loads Graph data from a JSON or XML string.
	 * @param saveString a string, saved via the saveGraphNative() method
	 */
	private native void loadGraphNative(final String saveString, final boolean asXML)/*-{
		var g = $wnd[this.@de.pubflow.graph.client.JSCaller::graphName];
		if(asXML){
			g.loadGraphXML(saveString, true);
		}else{
			g.loadGraph(saveString, true);
		}
		// add nodes to tree
		var data;
		g.iterateAllNodes(function(node){
			data = node.data;
			if(data.$custom || data.$type == "nodeFamily"){
				g.gwtExt.addTreeNode(node.id, node.name);
			}
		});
		g.gwtExt.setChanged();
	}-*/;

	
	/**
	 * Saves all graph data as a json or xml string.
	 * @param asXML if true, returns an xml string
	 * 
	 * @return the graph string
	 */
	private native String saveGraphNative(final boolean asXML)/*-{
		var g = $wnd[this.@de.pubflow.graph.client.JSCaller::graphName];
		// deselect node to prevent wrong colouring
		g.gwtExt.selectTreeNode("#null");
		
		var jsonString = (asXML) ? g.saveGraphXML() : g.saveGraph();
		return jsonString;
	}-*/;
	
	
	/**
	 * Updates the size of the canvas.
	 */
	public native void updateCanvasNative()/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].updateCanvasSize();
	}-*/;
	
	
	/**
	 * Selects a node from the graph.
	 * @param nodeID the id of the selected node
	 */
	private native void markNodeNative(final String nodeID)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		var node = $wnd[g].getNode(nodeID);
		$wnd[g].markNode(node);
	}-*/;
	
	
	/**
	 * A JavaScript function caller:
	 * Changes some node properties.
	 * @param id the id of the node that needs changing
	 * @param name the new name of the node
	 * @param iconID the new icon id of the node
	 */
	public native void editNodeNative(final String id, final String name, final String iconID)/*-{
		var data = {
			'name' : name,
			'$nodeType' : iconID
		};
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].gwtExt.editTreeNode(id, name);
		$wnd[g].setNodeData(id, data);
	}-*/;
	
	
	/**
	 * A JavaScript function caller:
	 * Extracts graph information and calls the KIELER autolayout algorithm.
	 */
	public native void autoLayoutNative()/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		
		var layout = $wnd[g].autoLayout();
		
		// check if graph is empty
		if(layout[0].indexOf(";") == -1){
			return;
		}
		var newPos = @graphflowlayouter.layout.GraphLayoutService::layoutGraph(Ljava/lang/String;Ljava/lang/String;)(layout[0], layout[1]);
		$wnd[g].loadPositions(newPos);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Changes the behaviour of mouse clicks inside the graph.
	 * @param mode - the name of the clickMode Enum
	 */
	public native void setClickModeNative(final String mode)/*-{
		$wnd.setClickMode(mode);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Layouts the graph, moving nodes to specified positions and adding
	 * Bend-Points to Edges.
	 * @param layout - A string containing all necessary layout information
	 */
	private native void loadPositionsNative(final String layout)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].loadPositions(layout);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Zooms in or out to show the complete graph.
	 */
	public native void zoomToFitNative()/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].scaleToFit();
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Zooms in or out, depending on the factor;
	 * 
	 */
	private native void zoomNative(double zoomFactor)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].zoom(zoomFactor);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Changes the grid visibility.
	 * 
	 * @param state - if true, the grid becomes visible
	 */
	public native void setGridVisibilityNative(final boolean state)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].setGridVisible(state);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Changes the grid visibility.
	 * 
	 * @param state - if true, the grid becomes visible
	 */
	public native void setGridSnapNative(final boolean state)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].setGridSnap(state);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Changes the grid size.
	 * 
	 * @param size - if true, the grid becomes visible
	 */
	private native void setGridSizeNative(final int size)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].setGridSize(size);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Writes a message to the browser console.
	 * 
	 * @param message - the printed message
	 */
	public native void log(final String message)/*-{
		$wnd.console.log(message);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Links an iconID with an image. All Nodes that share this iconID
	 * display the very same icon.
	 * @param iconID - unique ID-String
	 * @param iconURL - an URL that targets an image
	 */
	private native void setNodeIconNative(final String iconID, final String iconURL)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].setNodeIcon(iconID, iconURL);
	}-*/;
	
	
	/**
	 * A JavaScript function caller:
	 * Enables or disables fancy graph animations.
	 * @param enabled - if true, animates the graph
	 */
	public native void setAnimationNative(final boolean enabled)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].setAnimation(enabled);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Enables or disables node movement and crossboxes.
	 * @param enabled - if true, prevents node movement and deletion.
	 */
	public native void setLockedNative(final boolean enabled)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].setReadOnly(!enabled, !enabled);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Removes a node and all its children.
	 * @param id - unique id of the node
	 */
	public native void removeNodeNative(final String id)/*-{
		var g = $wnd[this.@de.pubflow.graph.client.JSCaller::graphName];
		var node = g.getNode(id);
		g.removeNode(node, true);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Removes an edge.
	 * @param sourceID - unique id of the source port
	 * @param sourceID - unique id of the target port
	 */
	public native void removeEdgeNative(final String sourceID, final String targetID)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		$wnd[g].removeEdge(sourceID, targetID);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Removes all nodes.
	 */
	public native void removeAllNative()/*-{
		var g = $wnd[this.@de.pubflow.graph.client.JSCaller::graphName];
		g.clear();
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Adds a node with ports to the graph.
	 * @param id - unique id of the node
	 * @param label - the displayed name of the node
	 * @param inputCount - the number of input ports
	 * @param outputCount - the number of output ports
	 * @param repositoryCount - the number of repository ports
	 * @param iconID - the ID of the displayed icon (nodeType)
	 */
	private native void addNodeNative(final String label, final int inputCount, final int outputCount, 
			final int repositoryCount, final String iconID)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		var center = $wnd[g].getScreenCenter();
		
		this.@de.pubflow.graph.client.JSCaller::addNodeNative(IILjava/lang/String;IIILjava/lang/String;)(Math.round(center.x), Math.round(center.y), label, inputCount, outputCount, repositoryCount, iconID);
	}-*/;
	
	/**
	 * A JavaScript function caller:
	 * Adds a node with ports to the graph.
	 * @param id - unique id of the node
	 * @param label - the displayed name of the node
	 * @param inputCount - the number of input ports
	 * @param outputCount - the number of output ports
	 * @param repositoryCount - the number of repository ports
	 * @param iconID - the ID of the displayed icon (nodeType)
	 */
	private native void addNodeNative(final int x, final int y, final String label, final int inputCount, final int outputCount, 
			final int repositoryCount, final String iconID)/*-{
		var g = this.@de.pubflow.graph.client.JSCaller::graphName;
		var node = {
			'name' : label
		};
		
		var i;
		var inputPorts = [];
		for(i = 0; i < inputCount; i++){
			inputPorts.push({'id' : 'ip' + i});
		}
		
		var outputPorts = [];
		for(i = 0; i < outputCount; i++){
			outputPorts.push({'id' : 'op' + i});
		}
		
		var repositoryPorts = [];
		for(i = 0; i < repositoryCount; i++){
			repositoryPorts.push({'id' : 'rp' + i});
		}
		var iconIDTemp = (iconID == null) ? undefined : iconID;
		
		$wnd[g].addNode(x, y, node, repositoryPorts, inputPorts, outputPorts, iconID);
	}-*/;
	
}
