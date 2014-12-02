/**************************************************************************

Copyright [2012] [Software Engineering Group - Kiel University]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

***************************************************************************/


var labelType, useGradients, nativeTextSupport, animate;

/**
	Set up Error Text and Navigator
*/

(function() {
  var agent = navigator.userAgent,
      iStuff = agent.match(/iPhone/i) || agent.match(/iPad/i),
      typeOfCanvas = typeof HTMLCanvasElement,
      nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),
      textSupport = nativeCanvasSupport 
        && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');
  //I'm setting this based on the fact that ExCanvas provides text support for IE
  //and that as of today iPhone/iPad current text support is lame
  labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';
  nativeTextSupport = labelType == 'Native';
  useGradients = nativeCanvasSupport;
  animate = !(iStuff || !nativeCanvasSupport);
  
  // fix IE script crashes
  if(!window.console){
	  window.console = { 'log' : function(){}};
	  }
})();


var Log = {
  elem: false,
  write: function(text){
    if (!this.elem) 
      this.elem = document.getElementById('log');
    this.elem.innerHTML = text;
    this.elem.style.left = (500 - this.elem.offsetWidth / 2) + 'px';
  }
};

/**
 * Fetches the CSS rules of an input String.
 * 
 *   className - name of the css rule
 */
function getCSSObject(className) {
    var classes = document.styleSheets[0].rules || document.styleSheets[0].cssRules
    for(var x=0;x<classes.length;x++) {
        if(classes[x].selectorText==className) {
                return classes[x].style;
        }
    }
    return null;
};


function GraphFlow(containerName){
	//////////////////////////////////////
	//				VARIABLES				   //
	//////////////////////////////////////
	
	/** Counter that provides unique ID's for nodes */
	var nodeIDCounter = 0;
	
	/** Overall size modifier: the width of node labels in pixels */
	var vertexLabelSize = 12;
	
	var visContainer = 'infovis';

	/** Colors */
	var markFillColor = false;
	var markStrokeColor = "#FF0000";
	
	var nodeFillColor = {
		'nodeFamily': "#DEDEDE",
		'inputPort' : "#DEDEDE",
		'outputPort' : "#DEDEDE",
		'repositoryPort' : "#DEDEDE"
	};
	
	var nodeTextColor = {
		'nodeFamily': "#4D4D4D"
	};
	
	var nodeTextFamily = {
		'nodeFamily': "Lucida Console"
	}
	
	var nodeStrokeColor = {
		'inputPort' : "#AA0000",
		'outputPort' : "#AA0000",
		'repositoryPort' : "#AA0000",
		'nodeFamily' : "#4D4D4D",
		'crossBox' : "#4D4D4D"
	};
		
	var nodeColorFocus = "#0098BE",
		edgeColor 		 = "#114270",
		edgeColorFocus  = "#0098BE";
		
	/** Custom Node Edges **/
	var customEdgeMap = {
		'none' : 
		{
			'getOffset' : function(){return {'x' : 0, 'y' : 0}}
		},
		'inputPort' : 
			{
			'getOffset' : function(){return {'x' : -vertexLabelSize, 'y' : vertexLabelSize / 2}}
			},
		'outputPort' : 
			{
			'getOffset' : function(){return {'x' : vertexLabelSize, 'y' : vertexLabelSize / 2}}
			}
	};
	customEdgeMap['repositoryPort'] = customEdgeMap['outputPort'];
	
	/** Icon Images */
	var nodeIcons = new Object();
	
	/** GraphFlow Object */
	var fd;

	/** Nodes and edges that are highlighted when the mouse moves over them */
	var hover = null;
	var markedNode = null;

	/** selected node:	{id : the id of the selected node, 
						 from : the data.$type of the selected node,
						 label: the label of a selected edge}
						 
		When clicking a node or edge, this variable will contain node
		information, that is required for attaching/detaching edges.
		*/
	var selectedNode = null;
	
	/** This points to an an invisible dummy node which is
		 used for moving an edge with the mouse
	*/
	var mouseNode = null;

	/** A hashtable, mapping mouse and node events to arrays of functions that
		are called when these events happen
		*/
	var listener = new Object();
	
	/** Can be changed to allow or prohibit node movement or deletion/creation */
	var readOnly = {'deleteCreate' 	: true,
					'move' 			: true};
	
	/** Grid dummy-node which is drawn over the graph if it is visible */
		
	var grid;
	
	/** Stores information about the canvas, such as its dimensions and its position.
		This information is needed to correctly draw the Grid and may come in handy in
		future functions.
		*/
	var navi = { 
		 // screen position
		 'centerX' : 0,
		 'centerY' : 0,
		 'isDragging' : false,
		 'isDraggingEdge' : false,
		 
		 // how far a the screen is dragged / where a dragged node is touched
		 'grabOffsetX' : null,
		 'grabOffsetY' : null,
		 'dragX' : null,
		 'dragY' : null,
		 'dragEdgeSourceX' : false,
		 'dragEdgeSourceY' : false
	};
	
	var orthogonalEdges = false;
	
	var animation = {
		'enabled' : true,
		'duration' : 1000,
		'busyCanvas' : false
	};
	
	////////////////////////////////
	//				FUNCTIONS			//
	////////////////////////////////

	
	
	//////////////
	//	GLOBAL	//
	//////////////
	
	/**
	 *	Forces a redraw of the graph.
	 */
	this.plot = function(){
		fd.plot();
	}
	
	/**
	 * Permits or prohibits node and edge deletion/creation and hides the deletion-cross
	 * accordingly.
	 * 
	 * Parameters: 
	 *  deleteCreate - if true, one delete graph components via mouse operations
	 *  move - if true, nodes can be dragged via mouse
	 */
	this.setReadOnly = function(deleteCreate, move){
				
		if(deleteCreate != readOnly.deleteCreate){
			// set crossBoxes (in)visible
			var data;
			
			iterateAllNodes(function(node){
				data = node.data;
				if(data.$type == "crossBox"){
					data.$visible = deleteCreate;
				}
			});
			
			readOnly.deleteCreate = deleteCreate;
		}
		
		if(move != undefined){
			readOnly.move = move;
		}
		fd.plot();
	}
	
	
	/**
	 * Enables / Disables graph animation and sets the mean duration of animations.
	 * 
	 * Parameters:
	 *  enabled - if true, animation is enabled
	 *  duration - the duration of animations in ms
	 */
	this.setAnimation = function(enabled, duration){
		
		animation.enabled = enabled;
		if(duration){
			animation.duration = duration;
		}
	}
	
	/**
	 * Changes the appearance of the mouse cursor. 
	 * 
	 * Parameters:
	 * newCursor - the String-name of the new cursor. 
	 *			   If false, the standard cursor is restored.
	 */
	this.setMouseCursor = function(newCursor){
		if(!newCursor){
			fd.canvas.getElement().style.cursor = '';
		}
		else{
			fd.canvas.getElement().style.cursor = newCursor;
		}
	}
	
	/**
	 * Adds a functionality to the following MouseEvents:
	 *	onClick(node, eventInfo, e),
	 *	onRightClick(node, eventInfo, e),
	 *	onMiddleClick(node, eventInfo, e),
	 *	onMouseMove(node, eventInfo, e),
	 *	onMouseEnter(node, eventInfo, e),
	 *	onMouseLeave(node, eventInfo, e),
	 *	onDragStart(node, eventInfo, e),
	 *	onDragMove(node, eventInfo, e),
	 *	onDragCancel(node, eventInfo, e),
	 *	onDragEnd(node, eventInfo, e)
	 * Or one of these events:
	 *	onError(errorMessage)
	 *	onCreateEdge(sourceNode, targetNode, sourcePort, targetPort) : boolean,
	 *	onCreateNode(node) : boolean,
	 *	onRemoveEdge(sourceNode, targetNode, sourcePort, targetPort) : boolean,
	 *	onRemoveNode(node) : boolean,
	 *	onKeyDown(keyCode)
	 *  onLoadPositionsStart : boolean,
	 *	onLoadPositionsComplete
	 *	onDragFar(node, eventInfo, e)
	 *	onCreateDanglingEdge(connectedNode, isIncoming, data) : boolean
	 *
	 * Parameters:
	 *	eventName - the name of the event (see list above)
	 *	listenerFunction - a function that is called when the event happens
	 */
	this.addListener = function(eventName, listenerFunction){
		if(listener[eventName] == undefined){
			listener[eventName] = new Array();
		}
		listener[eventName].push(listenerFunction);
	}
	
	/**
	 * Adds an onClick listener function. The corresponding event is called when the left mouse button is released.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the clicked node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addClickListener = function(listenerFunction){
		addListener('onClick', listenerFunction);
	}
	
	/**
	 * Adds an onClick listener function. The corresponding event is called when the middle mouse button is released.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the clicked node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addMiddleClickListener = function(listenerFunction){
		addListener('onMiddleClick', listenerFunction);
	}
	
	/**
	 * Adds an onRightClick listener function. The corresponding event is called when the right mouse button is released.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the clicked node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addRightClickListener = function(listenerFunction){
		addListener('onRightClick', listenerFunction);
	}
	
	/**
	 * Adds an onMouseMove listener function. The corresponding event is called whenever the mouse is moved.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the node or edge over which the mouse moves
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addMouseMoveListener = function(listenerFunction){
		addListener('onMouseMove', listenerFunction);
	}
	
	/**
	 * Adds an onMouseEnter listener function. The corresponding event is called when the mouse enters a node or edge.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the entered node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addMouseEnterListener = function(listenerFunction){
		addListener('onMouseEnter', listenerFunction);
	}
	
	/**
	 * Adds an onMouseLeave listener function. The corresponding event is called when the mouse leaves a node or edge.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the node or edge which the mouse leaves
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addMouseLeaveListener = function(listenerFunction){
		addListener('onMouseLeave', listenerFunction);
	}
	
	/**
	 * Adds an onDragStart listener function. The corresponding event is called when the mouse button is pressed down.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the dragged node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addDragStartListener = function(listenerFunction){
		addListener('onDragStart', listenerFunction);
	}
	
	/**
	 * Adds an onDragMove listener function. The corresponding event is called while a node is dragged.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the dragged node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addDragMoveListener = function(listenerFunction){
		addListener('onDragMove', listenerFunction);
	}
	
	/**
	 * Adds an onDragCancel listener function. The corresponding event is called when a node dragging is cancelled.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the dragged node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addDragCancelListener = function(listenerFunction){
		addListener('onDragCancel', listenerFunction);
	}
	
	/**
	 * Adds an onDragEnd listener function.  The corresponding event is called when a dragged node is released.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the dragged node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addDragEndListener = function(listenerFunction){
		addListener('onDragEnd', listenerFunction);
	}
	
	/**
	 * Adds an onError listener function. The corresponding event is sometimes called at invalid
	 * function arguments or other errors.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(errorMessage) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	errorMessage - a message that describes the error
	 */
	this.addErrorListener = function(listenerFunction){
		addListener('onError', listenerFunction);
	}
	
	/**
	 * Adds an onCreateEdge listener function. The corresponding event is called when an edge is created.
	 * 
	 * Parameters:
	 *	listenerFunction - 	the function(sourceNode, targetNode, sourcePort, targetPort) 
	 *						that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	sourceNode - the node or nodeFamily from where the edge originates 
	 * 	targetNode - the node or nodeFamily from where the edge ends 
	 * 	sourcePort - if the node is a nodeFamily, this is the port where the edge originates 
	 * 	targetPort - if the node is a nodeFamily, this is the port where the edge ends 
	 * 
	 *  return : false, if the edge should not be created
	 */
	this.addCreateEdgeListener = function(listenerFunction){
		addListener('onCreateEdge', listenerFunction);
	}
	
	/**
	 * Adds an onCreateNode listener function. The corresponding event is called when a node is created.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the created node
	 * 
	 * 	return: false, if the node should not be created
	 */
	this.addCreateNodeListener = function(listenerFunction){
		addListener('onCreateNode', listenerFunction);
	}
	
	/**
	 * Adds an onAddChild listener function. The corresponding event is called when a child node is added to a parent.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	child - the child node
	 * 	parent - the parent node
	 *
	 * 	return: false, if the child should not be attached to the parent
	 */
	this.addAddChildListener = function(listenerFunction){
		addListener('onAddChild', listenerFunction);
	}
	
	/**
	 * Adds an onRemoveChild listener function. The corresponding event is called when a child node is removed from a parent.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	child - the child node
	 * 	parent - the parent node
	 *
	 * 	return: false, if the child should not be removed from the parent
	 */
	this.addRemoveChildListener = function(listenerFunction){
		addListener('onRemoveChild', listenerFunction);
	}
	
	/**
	 * Adds an onRemoveEdge listener function. The corresponding event is called when an edge is removed.
	 * 
	 * Parameters:
	 *	listenerFunction - 	the function(sourceNode, targetNode, sourcePort, targetPort) 
	 *						that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	sourceNode - the node or nodeFamily from where the edge originates 
	 * 	targetNode - the node or nodeFamily from where the edge ends 
	 * 	sourcePort - if the node is a nodeFamily, this is the port where the edge originates 
	 * 	targetPort - if the node is a nodeFamily, this is the port where the edge ends 
	 * 
	 * 	return: false, if the edge should not be removed
	 */
	this.addRemoveEdgeListener = function(listenerFunction){
		addListener('onRemoveEdge', listenerFunction);
	}
	
	/**
	 * Adds an onRemoveNode listener function. The corresponding event is called when a node is removed.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	node - the removed node
	 * 
	 * 	return: false, if the node should not be removed
	 */
	this.addRemoveNodeListener = function(listenerFunction){
		addListener('onRemoveNode', listenerFunction);
	}
	
	/**
	 * Adds an onKeyDown listener function. The corresponding event is called when a key is pressed.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(keyCode) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	keyCode - the code of the pressed key
	 */
	this.addKeyDownListener = function(listenerFunction){
		addListener('onKeyDown', listenerFunction);
	}
	
	/**
	 * Adds an onKeyUp listener function. The corresponding event is called when a key is released.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(keyCode) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	keyCode - the code of the released key
	 */
	this.addKeyUpListener = function(listenerFunction){
		addListener('onKeyUp', listenerFunction);
	}
	
	/**
	 * Adds an onLoadPositionsStart listener function. The corresponding event is called when the loadPositions() function
	 * executes.
	 * 
	 * Parameters:
	 *	listenerFunction - the function() that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 * 	return:	false, if the positions should not be changed
	 */
	this.addLoadPositionsStartListener = function(listenerFunction){
		addListener('onLoadPositionsStart', listenerFunction);
	}
	
	/**
	 * Adds an onLoadPositionsComplete listener function. The corresponding event is called when the loadPositions() function
	 * has finished.
	 * 
	 * Parameters:
	 *	 listenerFunction - the function() that is called when the event happens
	 */
	this.addLoadPositionsCompleteListener = function(listenerFunction){
		addListener('onLoadPositionsComplete', listenerFunction);
	}
	
	/**
	 * Adds an onLoadGraph listener function. The corresponding event is called when the loadGraph() or loadGraphXML() function
	 * ends.
	 * 
	 * Parameters:
	 *	 listenerFunction - the function() that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 *  isXML - true if the loaded string is an XML-String
	 */
	this.addLoadGraphListener = function(listenerFunction){
		addListener('onLoadGraph', listenerFunction);
	}
	
	/**
	 * Adds an onSaveGraph listener function. The corresponding event is called when the saveGraph() or saveGraphXML() function
	 * ends.
	 * 
	 * Parameters:
	 *	 listenerFunction - the function() that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 *	 saveString - the string representation of the saved graph
	 *  isXML - true if the graph is saved as an XML-String
	 */
	this.addSaveGraphListener = function(listenerFunction){
		addListener('onSaveGraph', listenerFunction);
	}
	
	/**
	 * Adds an onDragFar listener function. The corresponding event is called when an object is dragged 24 pixels away
	 * from its original position.
	 * 
	 * Parameters:
	 *	listenerFunction - the function(node, eventInfo, e) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 *  node - the dragged node or edge
	 *  eventInfo - an object that provides information about the clicked objects
	 *  e - an object that provides information about pressed buttons and the mouse position
	 */
	this.addDragFarListener = function(listenerFunction){
		addListener('onDragFar', listenerFunction);
	}
	
	/**
	 * Adds an onCreateDanglingEdge listener function. The corresponding event is called when a dangling edge is created,
	 * e.g. when clicking on ports.
	 * 
	 * 
	 * Parameters:
	 *	listenerFunction - the function(connectedNode, isIncoming, data) that is called when the event happens
	 *
	 * ListenerFunction Parameters:
	 *  connectedNode - the CustomNode or port to which the dangling edge is connected
	 *  isIncoming - if true, the edge points towards the connected node
	 *  data - the data object of the dangling edge
	 */
	this.addCreateDanglingEdgeListener = function(listenerFunction){
		addListener('onCreateDanglingEdge', listenerFunction);
	}
	
	/**
	 * Adds a listener that prohibits edge creation if...
	 *	... an edge with the same source and target exists.
	 *	... the target node is the source node.
	 *	... the source and target node are of the same port type.
	 *	... a repository node is not connected to a repository and vice versa.
	 */
	this.addEdgeConstraints = function(){
		addListener("onCreateEdge", 
		function(sourceFamily, targetFamily, sourcePort, targetPort){
			// remove Edge if it does not lead to an inputPort
			if(targetPort.data.$type != "inputPort"){
				return false;
			}
		
			// remove Edge if the connected nodes share the same type
			if(sourcePort.data.$type == targetPort.data.$type){
				return false;
			}
			
			var goesToRepo = (targetFamily.data.$nodeType == "Repository"),
				isRepoPort = (sourcePort.data.$type == "repositoryPort");
			
			// remove Edge if it is falsely connected to repository ports
			if(goesToRepo != isRepoPort){
				return false;
			}
			
			// remove duplicate Edge
			var adja = sourcePort.adjacencies,
				duplicate = false;
			for(var a=0; a < adja.length; a++){
				if(adja[a].nodeTo == targetPort.id){
					if(duplicate){
						return false;
					}
					else{
						duplicate = true;
					}
				}
			}
			return true;
		});
	}
	
	/**
	* Defines a new type of node, a CustomNode. These are used by extending Graph plugins.
	* 
	* Parameters:
	*  nodeType - the name of the nodeType
	*  fillColor - fill color of the custom node
	*  strokeColor - color of the outline of the custom node
	*	font - the font of the node label
	*	fontColor - the font color of the node label
	*  edgeOffsetFun(ajacency, isSource) - a function which calculates the offset of a connecting edge
	*  									  to the center of the node
	*/
	this.registerCustomNode = function(nodeType, fillColor, strokeColor, font, fontColor, edgeOffsetFun){
		// define the color
		setNodeStyle(nodeType, fillColor, strokeColor, false, false, fontColor, font);
		// define the edge and offset
		customEdgeMap[nodeType] = {'getOffset' : edgeOffsetFun};
	}
	
	/**
	 *	Checks if the container  of the GraphFlow  graph has changed in size, 
	 *	and resizes the canvas area if necessary
	 */
	this.updateCanvasSize = function(){
		var container = document.getElementById(visContainer);
		var widthOld = grid.data.$width,
			heightOld = grid.data.$height,
			widthNew = container.clientWidth,
			heightNew = container.clientHeight;
			
		// check if the dimensions changed
		if( widthOld != widthNew || heightOld != heightNew){
		
			// calculate translation parameters
			var scale = fd.canvas.scaleOffsetX,
				transX = fd.canvas.translateOffsetX + (widthOld - widthNew)/2,
				transY = fd.canvas.translateOffsetY + (heightOld - heightNew)/2;
			
			// resize canvas, scale and translate content to maintain its position
			fd.canvas.resize(container.clientWidth, container.clientHeight, true);
			fd.canvas.scale(scale, scale);
			fd.canvas.translate(transX / scale, transY / scale);
			
			// update grid dimensions
			grid.data.$width = widthNew;
			grid.data.$height = heightNew;
			
			fd.plot();
		}
	}
	
	/**
	 * Return:
	 *  a size modifier that is used to scale the graph.
	 */
	this.getSizeModifier = function(){ 
		return vertexLabelSize;
	}
	
	/**
	 * Returns the bounds and the exact center of the graph.
	 *
	 * Parameters:  
	 *  checkEndPos - if true, checks the endPos instead of pos
	 *  			  of the node. This is used by autoLayout.
	 *  
	 * Return:
	 *  a {x, y, left, right, top, bottom}-Object   
	 */
	this.getGraphBounds = function(checkEndPos){
		
		// init loop variables
		var data, 
			width  = 0,
			height = 0;
		
		// the position of the node which is checked
		var pos = 'pos';
		if(checkEndPos){
			pos = 'endPos';
		}
		
		var x, y;
		var b, bendPoints;
		
		// init the outer bounds of the displayed graph
		var left  	= Number.POSITIVE_INFINITY,
			right 	= Number.NEGATIVE_INFINITY, 
			top		= Number.POSITIVE_INFINITY, 
			bottom	= Number.NEGATIVE_INFINITY;
		
		// this function checks the position of bendPoints
		var checkBendPoints = function(node){
			node.eachAdjacency(function(adja){
				
				data = adja.data;
				
				// is the first node the source node?
				if(adja.nodeFrom.id == data.$direction[0]){
					bendPoints = data.$bendPoints;
					for(b in bendPoints){
						b = bendPoints[b];
						
						x = b.x;
						y = b.y;
						
						left = Math.min(left, x);
						right = Math.max(right, x);
						top = Math.min(top, y);
						bottom = Math.max(bottom, y);
					}
					
				}
			});
		};
		
		// check nodeFamilies for position
		iterateAllNodes(function(node){
			data = node.data;
			
			if(!data.$noLayout && (data.$custom || data.$type == "nodeFamily")){
			
				x = node[pos].x;
				y = node[pos].y;
				
				width  = data.$width/2;
				height = data.$height/2;
				
				left = Math.min(left, x - width);
				right = Math.max(right, x + width);
				top = Math.min(top, y - height);
				bottom = Math.max(bottom, y + height);
				
				if(data.$custom){
					checkBendPoints(node);
				}
			}
			// is a port
			else{
				checkBendPoints(node);
			}
		});
		
		var midX = (left + right) / 2,
			midY = (top + bottom) / 2;
		
		var bounds = 
			{
				'x' : midX,
				'y' : midY,
				'left' : left,
				'right' : right,
				'top' : top,
				'bottom' : bottom
			};
		
		return bounds;
	}
	
	
	/**
	 * Returns the exact center of the canvas where the screen is currently located.
	 * 
	 * Return:
	 *  a {x, y}-Object
	 */
	this.getScreenCenter = function(){
		var canvas = fd.canvas,
			scale = canvas.scaleOffsetX,
			centerX = -canvas.translateOffsetX / scale,
			centerY = -canvas.translateOffsetY / scale;
		
		return {'x' : centerX, 
				'y' : centerY};
	}
	
	/**
	 * Returns the exact bounds of the canvas where the screen is currently located.
	 *  
	 * Return:
	 *  a {left, right, top, bottom}-Object  
	 */
	this.getScreenBounds = function(){
		var canvas = fd.canvas,
		scale = canvas.scaleOffsetX,
		centerX = -canvas.translateOffsetX / scale,
		centerY = -canvas.translateOffsetY / scale,
		hw = grid.data.$width / (2 * scale),
		hh = grid.data.$height / (2* scale),
		left = centerX - hw,
		right = centerX + hw,
		top = centerY - hh,
		bottom = centerY + hh;
	
		return {'left' : left,
				'right' : right,
				'top' : top,
				'bottom' : bottom};
	}
	
	/**
	 * Measures the width of a text on the canvas
	 * 
	 * Parameters:
	 *   text - the string representation of the text that is measured
	 *   font - the size and font of the text
	 * 
 	 * Return:
 	 *  the measured width
	 */
	this.getTextWidth = function(text, font){
		// get canvas context
		var ctx = fd.canvas.getCtx(),
			width = 0;
		
		// measure width
		ctx.save();
		ctx.font = font;
		width = ctx.measureText(text).width;
		ctx.restore();
		
		return width;
	}
	
	/**
	 * Zooms in or out on the graph, depending on the factor
	 * 
	 * Parameters:
	 *  factor - the graph zooms out if it is smaller than 1
	 */
	this.zoom = function(factor){
		
		// zoom
		var canvas = fd.canvas;
		canvas.scale(factor, factor);
		
		// show text only if it is big enough
		canvas.showLabels = (canvas.scaleOffsetX > canvas.labelThreshold);
		fd.plot();
	}
	
	/**
	 * Scales and centers the graph to fit the space provided by the container
	 * in which it is drawn.
	 * 
	 * Parameters:
	 *  forLoadPositions - if exists and true, the animation won't be called,
	 *	 		   		   and the endPos of nodes will be checked. These are
	 *			   		   the positions where the nodes will be after the 
	 *			   		   animation.
	 */
	this.scaleToFit = function(forLoadPositions){
		// do nothing if the graph is empty
		var isEmpty = true;
		
		iterateAllNodes(function(node){
			isEmpty = false;
			return;
		});
		if(isEmpty){
			return;
		}
		
		// prevent division by zero if canvas is invisible
		if(grid.data.$width <= 0 || grid.data.$height <= 0){
			return;
		}
		
		var isAnimated = animation.enabled && !animation.busyCanvas;
		
		// get graph bounds
		var bounds = getGraphBounds(forLoadPositions && isAnimated);
		
		// scaling values
		var canvas = fd.canvas,
			oldScale = canvas.scaleOffsetX,
			scaleX = grid.data.$width / (bounds.right - bounds.left + 4*vertexLabelSize),
			scaleY = grid.data.$height / (bounds.bottom - bounds.top + 4*vertexLabelSize)
			scale = Math.min(scaleX, scaleY);
		
		// translation values
		var midX = - bounds.x,
			midY = - bounds.y;
		
		
		if(isAnimated){
			
			// set animation parameters
			canvas.translateOffsetXEnd = midX;
		  	canvas.translateOffsetYEnd = midY;
		  	
		  	canvas.scaleOffsetOld = oldScale;
		  	canvas.scaleOffsetEnd = scale;
		  	
		  	if(!forLoadPositions){
		  		animation.busyCanvas = true;
				fd.animate({  
					modes: ['canvas:zoom:translate'],
					transition: $jit.Trans.Cubic.easeInOut,
					duration: animation.duration,
					onComplete: function(){animation.busyCanvas = false; fd.plot();}
				});
		  	}
			
		}else{
			// translate to the center of the graph and scale
			midX -= canvas.translateOffsetX / oldScale;
			midY -= canvas.translateOffsetY / oldScale;
			scale /= oldScale;
			
			canvas.translate(midX, midY);
			if(!forLoadPositions || oldScale - scale > 0){
				canvas.scale(scale, scale);
			}
			
			if(!animation.busyCanvas){
				fd.plot();
			}
		}
		
		// show text only if it is big enough
		canvas.showLabels = (canvas.scaleOffsetX > canvas.labelThreshold);
	}
	
	/**
	 * Removes all nodes and edges and icons from the graph.
	 * 
	 * Parameters:
	 *   forced - if true, no onRemove events are fired
	 */
	this.clear = function(forced){
		clearNodeIcons();
		iterateAllNodes(function(node){
			if(node){
				removeNode(node, false, forced);
			}
		});
		
		// reset unique ID counter if all nodes have been deleted
		if(forced){
			nodeIDCounter = 0;
			return;
		}
		var nodeCount = 0;
		iterateAllNodes(function(node){
			nodeCount++;
		});
		if(nodeCount == 0){
			nodeIDCounter = 0;
		}
	}
	
	
	
	////////////////////////////
	//	SAVE - LOAD - LAYOUT  //
	////////////////////////////
	
	/**
	 * Return:
	 *  a JSON string representation of the graph
	 */
	this.saveGraph = function(){
		
		// replaces objects with reference strings
		function refCensor(key, value){
			// save undefined values anyway
			if (value == undefined && key != "$tooltip") {
				return false;
			}
			else if(typeof(value) == "object" && value.id){
				return "#ref:" + value.id;
			}
			else if(key == "$icon"){
				return false;
			}
			return value;
		};		
		
		// converts a node to a json string
		function stringifyNode(node){
			var jsonNode = {"id"  : node.id,
					"name": node.name,
					"pos" : node.pos,
					"data": JSON.stringify(node.data, refCensor)
					};
			return jsonNode;
		};
		
		// fetch node AND edge information
		var jsonNodes = new Array();
		var jsonEdges = new Array();
		iterateAllNodes(function(node){
			// get node information
			jsonNodes.push(stringifyNode(node));
			
			// get edge information
			node.eachAdjacency(function(adja){
				if(adja.data.$direction[0] == adja.nodeFrom.id){
					jsonEdges.push(JSON.stringify(adja.data, refCensor));
				}
			});
		});
		
		// fetch icon information
		var jsonIcons = new Object();
		for(var i in nodeIcons){
			jsonIcons[i] = nodeIcons[i].src;
		}
		
		var jsonGraph = {
			"nodes" : jsonNodes,
			"edges" :jsonEdges,
			"icons" : jsonIcons
		};
		var saveString = JSON.stringify(jsonGraph);
		delete jsonGraph;
		
		callListener("onSaveGraph", [saveString, false]);
		return saveString;
	}
	
	/**
	 * Return:
	 *  a XML string representation of the graph
	 */
	this.saveGraphXML = function(){
		var xmlArray = new Array();
		xmlArray.push('<?xml version="1.0" encoding="UTF-8"?>');
		xmlArray.push('<Graph xmlns:p="http://www.pubflow.uni-kiel.de" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.pubflow.uni-kiel.de ../../../.temp/graphflow.xsd">');
		xmlArray.push(nodesToXML());
		xmlArray.push(edgesToXML());
		xmlArray.push(layoutToXML());
		xmlArray.push('</Graph>');
		
		// merge array of tags to a single string
		var saveString = xmlArray.join("\n");
		delete xmlArray;
		
		callListener("onSaveGraph", [saveString, true]);
		return saveString;
	}
	
	/**
	 * Loads a graph, represented as a XML string.
	 * 
	 * Parameters:
	 *  xmlString - TODO a string created with saveGraphXML()
	 *  forced - if true, no events are called in the process of creating the graph
	 */
	this.loadGraphXML = function(xmlString, forced){
		clear();
		
		var xmlDoc;
		if (window.DOMParser){
		  var parser = new DOMParser();
		  xmlDoc = parser.parseFromString(xmlString,"text/xml");
		}
		else{ // Internet Explorer
		  xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
		  xmlDoc.async = false;
		  xmlDoc.loadXML(xmlString); 
		}
		
		// make nodes
		var oldIDTable;
		{
			var xmlNodes = xmlDoc.getElementsByTagName("Graph")[0].children;
			for(var tag in xmlNodes){
				if(xmlNodes[tag].localName == "NodeList"){
					xmlNodes = xmlNodes[tag].children;
					break;
				}
			}
			oldIDTable = parseXMLNodes(xmlNodes, false, forced);
		}
		// make edges
		{
			var xmlEdges = xmlDoc.getElementsByTagName("Edge");
			parseXMLEdges(oldIDTable, xmlEdges, forced);
		}
		// set layout
		{
			var xmlLayout = xmlDoc.getElementsByTagName("LayoutData");
			if(xmlLayout.length > 0){
				parseXMLLayout(xmlLayout[0]);
			}
		}
		// resolve node references
		var nodesWithRef = new Array();
		iterateAllNodes(function(node){
			var data = node.data;
			for(var d in data){
				var val = data[d];
				if(typeof val == "string" && val.indexOf("#NODEREF:") == 0){
					data[d] = getNode(oldIDTable[val.substr(9)]);
				}
			}
		});
		
		// remove old IDs
		delete oldIDTable;
		
		callListener("onLoadGraph", [true]);
		
		console.log("nodeIcon:");
		for(var i in nodeIcons){
			console.log(i);
		}
	}
	
	/**
	 * Loads a graph, represented as a JSON string.
	 * 
	 * Parameters:
	 *  jsonString - a string created with saveGraph()
	 *  forced - if true, no "onCreateNode" events are called
	 */
	this.loadGraph = function(jsonString, forced){
		clear();
		
		var isAnimated = animation.enabled;
		var nodeData, edgeData, iconData;
		
		// parse top data layer
		{
			var graphData = JSON.parse(jsonString);
			nodeData = graphData.nodes;
			edgeData = graphData.edges;
			iconData = graphData.icons;
			delete graphData;
		}
		var g = fd.graph;
		
		var tempNodeCounter = 0;
		var oldIDTable = new Object();
		
		// load nodes
		var node, pos, x, y;
		for(var n in nodeData){
			node = nodeData[n];
			
			node.data = JSON.parse(node.data);
			oldIDTable[node.id] = "_tempNode" + tempNodeCounter;//node.data.$type + nodeIDCounter;
			node.id = oldIDTable[node.id];
			tempNodeCounter++;
			//nodeIDCounter++;
			
			x = node.pos.x;
			y = node.pos.y;
			g.addNode(node);
			
			node = g.getNode(node.id);
			if(isAnimated){
				pos =  $jit.Complex(x, y);
				node.setPos(pos, 'end');
			}else{
				node.pos.setc(x, y);
			}
			if(!forced && node.data.$type == "nodeFamily"){
				callListener("onCreateNode", [node]);
			}
		}
		
		// replace all "#ref:nodeID" with a reference to the actual node
		function refToObject(data){
			var val;
			for(var d in data){
				val = data[d];
				if(typeof val == "object"){
					refToObject(val);
				}
				else if(typeof val == "string" && val.indexOf("#ref:") == 0){
					var id = val.substring(5, val.length);
					data[d] = g.getNode(oldIDTable[id]);
				}
			}
		};
		iterateAllNodes(function(node){
			refToObject(node.data);
		});
		
		// load edges
		var edge, dir;
		for(var e in edgeData){
			edge = JSON.parse(edgeData[e]);
			refToObject(edge);
			
			dir = edge.$direction;
			addEdge(oldIDTable[dir[0]], oldIDTable[dir[1]], null, edge, forced);
		}
		
		// rename temporary node IDs
		delete oldIDTable;
		iterateAllNodes(function(node){
			var type = node.data.$type;
			if((node.data.$custom || type == "nodeFamily") && node.id.indexOf("_tempNode") == 0){
				var newID = type + nodeIDCounter;
				renameNodeID(node, newID)
				nodeIDCounter++;
			}
		});
		
		// load icons
		for(var nt in iconData){
			setNodeIcon(nt, iconData[nt], true);
		}
		
		// scale to fit the new graph
		scaleToFit(true);
		// animate if necessary
		if(isAnimated){
			var canvasMove = 'canvas:translate';
			// only zoom out if the graph is larger than the screen, do not zoom in
			if(fd.canvas.scaleOffsetX - fd.canvas.scaleOffsetEnd > 0){
				canvasMove += ':zoom';
			}
		  	
		  	animation.busyCanvas = true;
			fd.animate({  
				modes: ['node-property:width:height', 'linear', canvasMove],
				transition: $jit.Trans.Cubic.easeInOut,
				duration: animation.duration,
				onComplete: function(){animation.busyCanvas = false; fd.plot();}
			});
			return;
		}
		
		callListener("onLoadGraph", [false]);
	}
	
	/**
	 * Concatenates node positions and dimensions as well as bendPoints 
	 * to a single Layout-String, that can be passed as the input parameter 
	 * of the loadPositions() function.
	 * 
	 * Return:
	 *  the resulting position string
	 */
	 this.savePositions = function(){
		var nodeLayout = new Array(),
			edgeLayout = new Array();
		var nodeProps, edgeProps;
		 
		var data, nodePos;
		var a, adja;
		var b, bendPoints;
		
		// map nodeIDs to indices
		iterateAllNodes(function(node){
			data = node.data;
			
			// retrieve position and dimension of all relevant nodes
			if(node.data.$custom || data.$type == 'nodeFamily'){
				nodePos = node.pos.getc(true);
				
				nodeProps = [
				   node.id,
				   Math.round(nodePos.x),
				   Math.round(nodePos.y),
				   data.$width,
				   data.$height];
				nodeLayout.push(nodeProps.join(" "));
			}
			
			// retrieve bendPoints of edges
			adja = node.adjacencies;
			
			for(a in adja){
				data = adja[a].data;
				
				if(data.$direction[0] == node.id){
					edgeProps = new Array();
					
					// save connected ids
					edgeProps.push(node.id);
					edgeProps.push(data.$direction[1]);
					
					// extract bendPoints
					bendPoints = data.$bendPoints;
					
					for(b in bendPoints){
						edgeProps.push(bendPoints[b].x);
						edgeProps.push(bendPoints[b].y);
					}
					
					if(edgeProps.length > 2){
						edgeLayout.push(edgeProps.join(" "));
					}
				}
			}
		});
		
		// fuse all information to a single string
		nodeLayout = nodeLayout.join(";");
		edgeLayout = edgeLayout.join(";");
		nodeLayout += ";#" + edgeLayout + ";";
		
		callListener("onSavePositions", [nodeLayout]);
		
		return nodeLayout;
	}
	
	/**
	 * Reads a string containing new x,y positions, as well as dimensions which are 
	 * separated by spaces and moves nodefamilies to the specified positions.
	 *	
	 * Parameters:
	 *  input - the layout string
	 */
	this.loadPositions = function(input){
		if(callListener("onLoadPositionsStart", []) == false){
			return;
		}
		
		// check if animation is in progress
		var isAnimated = animation.enabled;
		var usePortIndex;
		var edgeInfo, nodeInfo;
		var p = 0;
		
		{
			var inputSplit = input.split("#");
			// catch Java Errors here
			if(inputSplit[0] == "Error"){
				callListener("onError", ["Error in GraphFlowLayouter.jar : " + inputSplit[1]]);
				return;
			}
			
			// check to see if we got the layout string from
			// the autolayouter, because it looks a bit different
			usePortIndex = inputSplit[0] == "autoLayout";
			if(usePortIndex){
				p++;
			}
			
			nodeInfo = inputSplit[p++].split(";");
			// bend points are optional
			if(inputSplit[p] && inputSplit[p].length > 0){
				edgeInfo = inputSplit[p].split(";");
			}

		}
		
		// prepare loop variables
		var node, data, adja, x, y;
		
		var id, width, height;
		var xOffset = -grid.data.$width/2,
			yOffset = -grid.data.$height/2;
		
		var nodeProps;
		var n, l;
		
		var endPos = new $jit.Complex(0,0);
		var g = fd.graph;
		
		// iterate node positions
		for(n = 0, l = nodeInfo.length - 1; n < l; n++){
			
			nodeProps = nodeInfo[n].split(" ");
			p = 0;
			
			// lookup node in graph
			id = nodeProps[p++];
			node = g.getNode(id);
			data = node.data;
			
			if(!node){
				callListener("onError", ["Error in loadPositions() : Node with ID '"+ id + "' does not exist!"]);
			}
			else{
				// extract positions from string array
				x = parseFloat(nodeProps[p++]) + xOffset;
				y = parseFloat(nodeProps[p++]) + yOffset;
				width = parseFloat(nodeProps[p++]);
				height = parseFloat(nodeProps[p]);
				
				// is custom node
				if(data.$custom){
					
					// set new position and dimensions
					if(isAnimated){
						endPos.x = x;
						endPos.y = y;
						if(data.$tempLayoutPos){
							data.$tempLayoutPos = {"x": x, "y" : y};
							endPos = node.pos;
						}
						node.setPos(endPos, 'end');
						
						if(data.$tempLayoutWidth){
							data.$tempLayoutWidth = width;
							width = data.$width;
						}
						node.setData('width', width, 'end');
						
						if(data.$tempLayoutHeight){
							data.$tempLayoutHeight = height;
							height = data.$height;
						}
						node.setData('height', height, 'end');
					}else{
						if(data.$tempLayoutPos){
							endPos.x = x;
							endPos.y = y;
							data.$tempLayoutPos = endPos;
						}else{
							node.pos.setc(x, y);
						}
						if(data.$tempLayoutWidth){
							data.$tempLayoutWidth = width;
						}else{
							data.$width = width;
						}
						if(data.$tempLayoutHeight){
							data.$tempLayoutHeight = height;
						}else{
							data.$height = height;
						}
					}
				}
				// is nodeFamily
				else{
					if(data.$tempLayoutPos){
							endPos.x = x;
							endPos.y = y;
							data.$tempLayoutPos = endPos;
					}else{
						moveNode(node, x, y, isAnimated, true);
					}
				}	
			}
		}
				
		if(edgeInfo){
			var adja;
			var from, to;
			var bl, b, bendArray, bendPoints;
			var portIndex;
			
			for(n = 0, l = edgeInfo.length - 1; n < l; n++){
				bendArray = edgeInfo[n].split(" ");
				bl = bendArray.length;
				b = 0;
				
				// get portIDs, using the familyID and the portIndex
				if(usePortIndex){
					node = g.getNode(bendArray[b++]);
					data = node.data;
					if(data.$custom){
						from = node.id;
					}else{
						portIndex = parseInt(bendArray[b]);
						from = data.$children[++portIndex].id
					}
					b++;
					
					node = g.getNode(bendArray[b++]);
					data = node.data;
					if(data.$custom){
						to = node.id;
					}else{
						portIndex = parseInt(bendArray[b]);
						to = data.$children[++portIndex].id
					}
					b++;
				
				// simply read the exact portIDs
				}else{
					from = bendArray[b++];
					to = bendArray[b++];
				}
				
				adja = g.getAdjacence(from, to);
				if(!adja){
					callListener("onError", ["Error in loadPositions() : Edge between '"
					                         + from + "' and '" + to + "' does not exist!"]);
				}else{
					// new bend points exist
					if(b < bl){
						
						// read from bend point string
						bendPoints = new Array();
						while(b < bl){
							x = parseFloat(bendArray[b++]) + xOffset;
							y = parseFloat(bendArray[b++]) + yOffset;
							bendPoints.push({'x':x, 'y':y});
						}
						adja.data.$bendPoints = bendPoints;	
					}
					// remove bend points
					else{
						adja.data.$bendPoints = null;	
					}
					
				}
			}
			
		}
		
		// scale to fit the new graph
		scaleToFit(true);
		
		// animate if necessary
		if(isAnimated){
			
			var canvasMove = 'canvas:translate';
			
			// only zoom out if the graph is larger than the screen, do not zoom in
			if(fd.canvas.scaleOffsetX - fd.canvas.scaleOffsetEnd > 0){
				canvasMove += ':zoom';
			}
		  	
		  	animation.busyCanvas = true;
			fd.animate({  
				modes: ['node-property:width:height', 'linear', canvasMove],
				transition: $jit.Trans.Cubic.easeInOut,
				duration: animation.duration,
				onComplete: function(){
					animation.busyCanvas = false; 
					iterateAllNodes(function(node){updateEdgeOffset(node)});
					fd.plot();
					callListener("onLoadPositionsComplete", []);
					}
			});
		}else{
			iterateAllNodes(function(node){updateEdgeOffset(node)});
			fd.plot();
			callListener("onLoadPositionsComplete", []);
		}
		
		return;
	}
	
	/**
	 *	Extracts certain node and edge information and fuses it to two
	 *	Strings of which the elements look like this:
	 *	 NodeString: nodeID parentID width height isCustomNode inputPortCount firstInputPortYPos
	 *		repositoryPortCount firstRepositoryPortYPos outputPortCount firstOutputPortYPos
	 *	 EdgeString: sourceNode sourceNodePort targetNode targetNodeID
	 *	The resulting strings will be arguments of a listener event and can be used
	 *	by the KIELER autolayouting algorithm.
	 */
	this.autoLayout = function(){
		// link port IDs to their family index
		var c, id, children, portMap = new Object();
		
		// list all parents of custom nodes
		var data, parentMap = new Object();
		iterateAllNodes(function(node){
			
			id = node.id;
			data = node.data;
			
			if(data.$custom){
				children = data.$children;
				for(c in children){
					c = children[c];
					parentMap[c.id] = id;
				}
			}
		});
		
		var nodes = [vertexLabelSize];
		var edge, edgeMap = new Object();
		var	nf, parentID, srcId;
		var nodeY, height;
		// map nodeIDs to indices
		iterateAllNodes(function(node){
			
			data = node.data;
			height = data.$height;
			c = data.$custom;
			nf = data.$type == 'nodeFamily';
			
			if(!data.$noLayout && (nf || c)){
				id = node.id;
				
				// check for parent
				if(!(parentID = parentMap[id])){
					parentID = -1;
				}
				localArr = [id,
				            parentID,
							data.$width,
							height];
				
				// is Custom Node
				if(c){
					localArr.push('c'); 	// custom tag
					
					// create entry in edges
					if(!(edge = edgeMap[id])){
						edgeMap[id] = [id, -1];
					}else{
						edge[0] = id;
						edge[1] = -1;
					}
					
					// save edge info in edges-object
					adja = node.adjacencies;
					
					for(a in adja){
						a = adja[a].data.$direction;
						srcId = a[0];
						
						// fill in info for all edges that have this node
						// as a target
						if(a[1] == id){
							
							if(!edgeMap[srcId]){
								edgeMap[srcId] = ["", -1]
							}
							// add target info to edge
							edgeMap[srcId].push(id, -1);
						}
					}
				}
				
				// is NodeFamily
				else{ 
					localArr.push('f'); 	// family tag
					// inputCount, inputYPos, repoCount...
					localArr.push(0,0, 0,0, 0,0);
					
					// iterate ports and children
					children = data.$children;
					nodeY = node.pos.y;
					pIndex = -1;
					height /= 2;
					
					for(c in children){
						child = children[c];
						
						// increment node count and get relative y position of the topmost ports
						switch(child.data.$type){
							case "inputPort" : 
								if(localArr[5]++ == 0){ 
									localArr[6] = Math.round(child.pos.y - nodeY + height);
								}
								break;
						
							case "repositoryPort" : 
								if(localArr[7]++ == 0){ 
									localArr[8] = Math.round(child.pos.y - nodeY + height);
								}
								break;
								
							case "outputPort" : 
								if(localArr[9]++ == 0){ 
									localArr[10] = Math.round(child.pos.y - nodeY + height);
								}
								break;
								
							default : break;
						}
						
						if(pIndex != -1){
							// create entry in edges
							if(!(edge = edgeMap[child.id])){
								edgeMap[child.id] = [id, pIndex];
							}else{
								edge[0] = id;
								edge[1] = pIndex;
							}
							
							// save edge info in edges-object
							adja = child.adjacencies;
							
							for(a in adja){
								a = adja[a].data.$direction;
								srcId = a[0];
								
								// fill in info for all edges that have this port
								// as a target
								if(a[1] == child.id){
									
									if(!edgeMap[srcId]){
										edgeMap[srcId] = ["", -1]
									}
									// add target info to edge
									edgeMap[srcId].push(id, pIndex);
								}
							}
						}
						pIndex++;
					}
				}
				// convert node data to string and push it to Array
				nodes.push(localArr.join(" "));
			}
		});
		
		// merge arrays to strings
		var edges = new Array();
		var t, source;
		for(c in edgeMap){
			c = edgeMap[c];
			
			if(c.length > 2){
				source = c[0] + " " + c[1];
				t = 2;
				while(t < c.length){
					target = " " + c[t++] + " " + c[t++];
					edges.push(source + target);
				}
			}
		}
		
		// fuse nodes array to a single string
		nodes = nodes.join(";");
		edges = edges.join(";");
		
		callListener("autoLayout", [nodes, edges]);
		return [nodes, edges];
	}
	
	/**
	 * Extracts certain node and edge information and fuses it to two
	 * Strings whose elements look like this:
	 * NodeString: isCustomNode, width, height, parentNodeIndex
	 * EdgeString: srcNodeIndex, srcPortIndex, tarNodeIndex0, tarNodeIndex2, ...
	 * The resulting strings will be arguments of a listener event and can be used
	 * by the KIELER autolayouting algorithm.
	 *	
	 * Parameters:
	 *	  nodeArray - array of custom nodes that need to be layouted
	 */
	this.partialCustomAutoLayout = function(nodeArray){
		console.log(nodeArray);
		
		var isInArray = new Object();
		for(var n in nodeArray){
			n = nodeArray[n];
			isInArray[n.id] = true;
		}
		console.log(isInArray);
		
		var id, data, children; 
		
		var nodes = [vertexLabelSize];
		var edge, edgeMap = new Array();
		var	srcId, tarId;
		var nodeY, width, height;
		
		// map nodeIDs to indices
		for(var n in nodeArray){
			n = nodeArray[n];
			id = n.id;
			data = n.data;
			width = data.$width;
			height = data.$height;
			
			// push node data to Array
			var nodeString = id + " -1 " + width + " " + height + " c";
			nodes.push(nodeString);
			
			// create entry in edges
			if(!(edge = edgeMap[id])){
				edgeMap[id] = [id, -1];
			}else{
				edge[0] = id;
				edge[1] = -1;
			}
			
			// save edge info in edges-object
			adja = n.adjacencies;
			for(a in adja){
				a = adja[a].data.$direction;
				srcId = a[0];
				tarId = a[1];
				// fill in info for all edges that have this node
				// as a target only if they are in the nodeArray
				if(isInArray[tarId] && tarId == id){
					
					if(!edgeMap[srcId]){
						edgeMap[srcId] = ["", -1]
					}
					// add target info to edge
					edgeMap[srcId].push(id, -1);
				}
			}
		}
		
		// merge arrays to strings
		var edges = new Array();
		var t, source;
		for(var c in edgeMap){
			c = edgeMap[c];
			
			if(c.length > 2){
				source = c[0] + " " + c[1];
				t = 2;
				while(t < c.length){
					target = " " + c[t++] + " " + c[t++];
					edges.push(source + target);
				}
			}
		}
		
		// fuse nodes array to a single string
		nodes = nodes.join(";");
		edges = edges.join(";");
		
		callListener("partialCustomAutoLayout", [nodes, edges]);
		return [nodes, edges];
	}
	
	
	
	//////////////
	//	GRID	//
	//////////////
	
	/**
	 * Changes the visibility of the grid.
	 *
	 * Parameters:
	 *	visibility - must be true to display the graph
	 */
	this.setGridVisible = function(visibility){
		isAnimated = animation.enabled && !animation.busyCanvas;
		var setter = 'current';
		
		if(isAnimated){
			setter = 'end';
		}
		if(visibility){
			grid.setData('alpha', 1, setter);
		}else{
			grid.setData('alpha', 0, setter);
		}
		if(isAnimated){
			fd.animate({  
				modes: ['node-property:alpha'],
				transition: $jit.Trans.Cubic.easeInOut,
				duration: animation.duration / 2,
				onComplete: function(){fd.plot();}
			});
		}else if(!animation.busyCanvas){
			fd.plot();
		}
	}
	
	/**
	 * Toggles the visibility of the grid.
	 *
	 */
	this.toggleGrid = function(){
		if(grid.data.$alpha == 0){
			setGridVisible(true);
		}
		else{
			setGridVisible(false);
		}
	}
	
	/**
	 * Changes the color of the grid.
	 *	
	 * Parameters:
	 *	newColor - the new color of the grid
	 */
	this.setGridColor = function(newColor){
		grid.data.$color = newColor;
		fd.plot();
	}
	
	/**
	 * Changes the size of the grid.
	 *
	 * Parameters:
	 *	newSize - the new size of the grid
	 */
	this.setGridSize = function(newSize){
		/*
		if(!validArg('setGridSize()', newSize, 'number')){
			return;
		}
		*/
		grid.data.$dim = newSize;
		fd.plot();
		
	}
	
	/**
	 * Changes whether Nodes should snap to grid.
	 *	  
	 * Parameters:
	 *  snap - must be true if Nodes should snap
	 */
	this.setGridSnap = function(snap){
		grid.data.$snap = snap;
	}
	
	
	
	//////////////
	//	NODES	//
	//////////////
	
	/**
	 * Iterates through all nodes and ports, calling a specified function on each one.
	 * 
	 * Parameters:
	 *  nodeFunction - a single function, that takes the iterated node as an argument.
	 */
	this.iterateAllNodes = function(nodeFunction){
		
		// filter out dummy nodes
		fd.graph.eachNode(function(node){
			if(node && node.id[0] != "#"){
				nodeFunction(node);
			}
		});
	}
	
	 /**
  * Returns a node from the graph.
  * 
  * Parameters:
  *  nodeID - the id of the desired node
  * 
  * Return:
  *  the node object or null if the node does not exist
  */
  this.getNode = function(nodeID){
	return fd.graph.getNode(nodeID);
  }
  
  /**
   * Returns all nodes that have a specific data value.
   * 
   * Parameters:
   *  dataKey - the key of which the value must match
   *  dataValue - the requested value of the key
   * 
   * Return:
   *  an array containing all nodes that fit the criteria
   */
  this.getNodesByDate = function(dataKey, dataValue){
	  
	  var notInData = (dataKey == "id" || dataKey == "name");
	  var nodeArray = new Array();
	  
	  iterateAllNodes(function(node){
		// key is not nested in node.data
		if(notInData && node[dataKey] == dataValue){
			nodeArray.push(node);
		}
		// key is nested in node.data
		else if(!notInData && node.data[dataKey] == dataValue){
			nodeArray.push(node);
		}
	  });
	  
	return nodeArray;
  }
	
	/**
	 * Adds a Node with ports to the graph. These kind of nodes are referred to as NodeFamilies in our comments.
	 * 
	 * Parameters:
	 *  xPosition - the horizontal position of the added node. 0 is the center of the canvas' starting position
	 *  xPosition - the vertical position of the added node. 0 is the center of the canvas' starting position
	 *  nodeFamily - an object, describing some properties of the node: (id, name, tooltip, data).
	 *					  data can be used to store or overwrite additional data in the node
	 *  repositoryPorts - an array of properties (id, name, tooltip) for the input ports
	 *  inputPorts - an array of properties (id, name, tooltip) for the input ports 
	 *  outputPorts - an array of properties (id, name, tooltip) for the output ports
	 *  nodeType - a string-identifier for a certain group of nodefamilies that can share an icon
	 *  forced - if true, no event listener will be called and the node will be created no matter what
	 */
	this.addNode = function(xPosition, yPosition, nodeFamily, repositoryPorts, inputPorts, outputPorts, nodeType, forced){
		// determine array size
		var countRepo = 0,
			countInput = 0,
			countOutput = 0,
			iconSpace = 0;
			
		if(repositoryPorts){
			countRepo = (typeof repositoryPorts == "number") ? repositoryPorts : repositoryPorts.length;
			if( !countRepo){
				countRepo = 0;
			}
		}
		if(inputPorts){
			countInput = (typeof inputPorts == "number") ? inputPorts : inputPorts.length;
			if(!countInput){
				countInput = 0;
			}
		}
		if(outputPorts){
			countOutput = (typeof outputPorts == "number") ? outputPorts : outputPorts.length;
			if(!countOutput){
				countOutput = 0;
			}
		}
		
		var pos = new $jit.Complex(0,0);
		var maxPorts = Math.max(countInput, countRepo + countOutput + 0.5),
			size = vertexLabelSize * 2;
			
		// insert some space between repo ports and output ports, only if both are present
		if(maxPorts > countInput && (countRepo == 0 || countOutput == 0)){
			maxPorts -= 0.5;
		}
		
		// check if this nodeType has a defined icon
		if(nodeIcons[nodeType]){
			iconSpace = 4;
		}
		
		// set up font
		var font = nodeFamily.font;
		if(!font){ 
			font = nodeTextFamily["nodeFamily"];
		}
		font = "px " + font;
		
		// calculate node family dimensions
		var width = 0;	
		var height = size + Math.max( 2 * size, vertexLabelSize + maxPorts * size);
		
		// calculate width of longest text line
		if(nodeFamily.name){
			var textArray = nodeFamily.name.split("\n");
			for(var t in textArray){
				width = Math.max(width, graph.getTextWidth(textArray[t], (size * 0.83) + font));
			}
			
			height = Math.max(height, (textArray.length + 2) * vertexLabelSize * 2);
		}
		width += (4 + iconSpace) * vertexLabelSize;

		// add big node box
		// change node color, depending on its type
		var nodeColor = nodeFillColor["nodeFamily"],
			textColor = nodeTextColor["nodeFamily"],
			strokeColor = nodeStrokeColor["nodeFamily"];
		
		var newNode = {
			  "data": {
				"$dim": size,					
				"$type": "nodeFamily",
				"$icon": nodeIcons[nodeType],
				"$width": width,
				"$height": height,
				"$color": strokeColor,
				"$textColor": textColor,
				"$fillColor": nodeColor,
				"$nodeType": nodeType,
				"$children" : new Array(),
				"$font": font,
				"$movable" : true,
				"$tooltip": nodeFamily.tooltip
			  }, 
			  "id": "nodeFamily" + nodeIDCounter, // make unique ID
			  "name": textArray
		};
		nodeIDCounter++;
		
		// import existing data
		if(nodeFamily.data){
			for(var d in nodeFamily.data){
				newNode.data[d] = nodeFamily.data[d];
			}
		}
		
		// add closeButton
		var closeButton ={
			  "data": {
				"$dim": size,					
				"$type": "crossBox",
				"$color": strokeColor,
				"$fillColor": nodeColor,
				"$visible": readOnly.deleteCreate,
				"$movable": false
				}, 
			  "id": newNode.id + ".closeButton", 
			  "name": "x"
		};
		
		// set up some more data
		var g = fd.graph;
		g.addNode(newNode);
		g.addNode(closeButton);
		newNode = g.getNode(newNode.id);
		closeButton = g.getNode(closeButton.id);
		closeButton.data.$family = newNode;
		
		// set node positions
		pos.x = xPosition;
		pos.y = yPosition;
		newNode.setPos(pos, 'current');
		newNode.setPos(pos, 'end');
		
		pos.x = xPosition + (width - vertexLabelSize) / 2;
		pos.y = yPosition - height / 2 + size;
		closeButton.setPos(pos, 'current');
		closeButton.setPos(pos, 'end');
		newNode.data.$children.push(closeButton);
		
		// Loop Info: 
		// This loop adds all port types to the temporary port array.
		// For fast position computation, the relative position
		// of the first port to the nodeFamily-box is computed for each port type
		// it is stored in relativeX, relativeY
		
		for(var portType = 0; portType < 3; portType++){
			var x, y, relativeX, relativeY,
				loopPorts, loopType;
			switch(portType){
				case 0: // input ports
					relativeX = -width/2;
					relativeY = (0.75 - countInput/2 )*size;
					loopPorts = inputPorts;
					loopType = "inputPort";
					break;
			
				case 1: // repository ports
					var repoSpace = 0.5;
					if(countOutput == 0){
						repoSpace = 0;
					}
					relativeX = width/2;
					relativeY = (0.75 - (countOutput+countRepo+repoSpace)/2 )*size;
					loopPorts = repositoryPorts;
					loopType = "repositoryPort";
					break;
					
				case 2: // output ports
					var repoSpace = 0.5;
					if(countRepo == 0){
						repoSpace = 0;
					}
					relativeX = width/2;
					relativeY = (0.75 - (countOutput-countRepo-repoSpace)/2 )*size;
					loopPorts = outputPorts;
					loopType = "outputPort";
					break;
			}
			
			// continue only if ports exist
			if(loopPorts){
				x = xPosition + relativeX;
				y = yPosition + relativeY;
				var isPortArray = (typeof loopPorts != "number");
				var portCount = (isPortArray) ? loopPorts.length : loopPorts;
				
				for(var p = 0; p < portCount; p++){
					
						var newPort = {
							"id":  newNode.id + "." + loopType + p,
							"adjacencies": new Array(), 
							"data": {
								"$dim": size,					
								"$type": loopType,
								"$color": nodeStrokeColor[loopType],
								"$fillColor": nodeColor,
								"$movable" : false}
						};
						
						if(isPortArray){
							var port = loopPorts[p];
							newPort.data.$tooltip = port.tooltip;
							newPort.data.$symbol = port.symbol;
							newPort.name = port.name;
						}
						g.addNode(newPort);
						newPort = g.getNode(newPort.id);
						pos.x = x;
						pos.y = y;
						newPort.setPos(pos, 'current');
						newPort.setPos(pos, 'end');
						newNode.data.$children.push(newPort);
						
						y += size;
					}
				}
		}
		// call listener
		if(!forced){
			if(callListener("onCreateNode", [newNode]) == false){
				removeNode(newNode, true, true);
				fd.plot();
				return false;
			}
		}
		fd.plot();
		return newNode;
	}
	
	/**
	 * Adds a user defined node without ports to the graph.
	 * 
	 * Parameters:
	 *  x - the horizontal position of the node
	 *  y - the vertical position of the node
	 *	 label - the displayed label of the node if it is supported
	 *  type - the type of the registered custom node
	 *	 data - optional data entries that may override default values
	 *  forced - if true, does not throw a listener event
	 */
	this.addCustomNode = function(x, y, label, type, data, forced){
		
		// consider linebreaks
		if(label && typeof label == "string"){
			label = label.split("\n");
		}
		// define node
		var newNode = {
		  "adjacencies": new Array(), 
		  "data": {
			"$dim": vertexLabelSize,		
			"$type": type,
			"$custom": true
		  }, 
		  "id": type + nodeIDCounter, 
		  "name": label
		};
		nodeIDCounter++;
		
		// get globally defined colors and font
		var val;
		if(val = nodeTextFamily[type]){
			newNode.data.$font = val;
		}
		if(val = nodeStrokeColor[type]){
			newNode.data.$color = val;
		}
		if(val = nodeFillColor[type]){
			newNode.data.$fillColor = val;
		}
		// (over-)write data if it was specified in node.data
		if(data){
			for(var field in data){
				newNode.data[field] = data[field];
			}
		}
		
		// apply changes immediately
		fd.graph.addNode(newNode);
		newNode = fd.graph.getNode(newNode.id);
		newNode.pos.setc(x, y);
		fd.plot();
		
		// call listener
		if(!forced){
			if(callListener("onCreateNode", [newNode]) == false){
				removeNode(newNode, true, true);
				fd.plot();
				return false;
			}
		}
		
		return newNode;
	}
	
	/**
	 * Clones a Node along with its ports, if they exist. All properties are
	 * copied, but no eges are attached to the cloned node.
	 * 
	 * Parameters:
	 *  xPosition - the horizontal position of the node
	 *  yPosition - the vertical position of the node
	 *	node - the node which is copied
	 *	newID - the id of the copy
	 *	forced - if true, no event will be called upon node creation
	 */
	this.cloneNode = function(xPosition, yPosition, newID, node, forced){
		if(node.data.$custom){
			cloneCustomNode(xPosition, yPosition, node, newID, forced);
		}
		else{
			cloneOrdinaryNode(xPosition, yPosition, node, newID, forced);
		}
		
	}
	
	/**
	 * Removes a Node and all its attached ports and edges from the graph.
	 * 
	 * Parameters:
	 *	node - the node which is to be removed
	 *	forced - if true, the node will be removed unconditionally and no
	 *	 		 event listeners will be called
	 */
	this.removeNode =  function(node, removeChildren, forced){
		
		// call listener
		if(!forced && !callListener("onRemoveNode", [node])){
			return;
		}
		
		// unmark node
		if (markedNode && markedNode.node.id == node.id){
			markedNode = null;
		}
		var g = fd.graph;
		
		// remove all children from node
		var children = node.data.$children;
		if(removeChildren && children){
			var child;
			for(var c in children){
				g.removeNode(children[c].id);
			}
			
		}
		// remove node from parent if it exists
		iterateAllNodes(function(parent){
			var children = parent.data.$children;
			var done = false;
			for(var c in children){
				if(done){
					return;
				}
				if(children[c] == node){
					delete children[c];
					children.splice(c, 1);
					done = true;
					return;
				}
			}
		});
		// remove node itself
		g.removeNode(node.id);
		fd.plot();
		
	}
	
	/**
	 *	Adds a child to a CustomNode. Children move whenever their parents are moved.
	 * A child can only be added once.
	 *	 
	 *	Parameters
	 *	 childNode - the child node which is to be attached to the parent
	 *	 parentNode - the parent
	 *	 forced - if true, no "onAddChild"-Event will be called
	 */
	this.addChild = function(childNode, parentNode, forced){
		if(!forced && callListener("onAddChild", [childNode, parentNode]) == false){
			return;
		}
		if(!parentNode.data.$custom){
			callListener("onError", ["Error in function addChild(): Cannot add children to non-custom nodes!"]);
			return;
		}
		// create child array if necessary
		if(!parentNode.data.$children){
			var childArray = new Array();
			childArray.push(childNode);
			parentNode.data.$children = childArray;
		}
		else{
			// prevent multiple entries of the same child in array
			var childArray = parentNode.data.$children;
			var childEntry;
			for(var c = 0, l = childArray.length; c < l; c++){
				childEntry = childArray[c];
				if(childEntry == childNode){
					return;
				}
			}
			childArray.push(childNode);
		}
	}
	
	/**
	 *	Removes a child from a CustomNode. 
	 *	 
	 *	Parameters
	 *	 childNode - the child node which is to be removed from the parent
	 *	 parentNode - the parent
	 *	 forced - if true, no "onRemoveChild"-Event will be called
	 */
	this.removeChild = function(childNode, parentNode, forced){
		if(!forced && callListener("onRemoveChild", [childNode, parentNode]) == false){
			return;
		}
		
		// return if no children can exist on the node in the first place
		var childArray = parentNode.data.$children;
		if(!parentNode.data.$custom || !childArray){
			return;
		}
		
		var childEntry;
		for(var c = 0, l = childArray.length; c < l; c++){
			childEntry = childArray[c];
			if(childEntry == childNode){
				childArray.splice(c,1);
				return;
			}
		}
	}
	
	/**
	 * Shows or hides the close button of a nodeFamily.
	 * 
	 * Parameters:
	 * 	state - if true, shows the delete button
	 */
	this.setCloseButtonVisible = function(node, state){
		if(!node || node.data.$custom){
			return;
		}
		
		var c;
		var children = node.data.$children;
		
		for(c in children){
			c = children[c];
			if(c.data.$type == "crossBox"){
				c.data.$visible = state;
				fd.plot();
				return;
			}
		}
		
	}
	
	/**
	 * Changes the displayed text of a node.
	 * 
	 * Parameters:
	 * 	node - the node of which the name is changed
	 *  name - the new displayed text
	 */
	this.setNodeName = function(node, name){
		node.name = name;
	}
	 
	/**
	 * Changes one or more properties of a node. Converts displayable strings to 
	 * arrays and recalculates node size.
	 * 
	 * Parameters:
	 *  object - id of a node that needs changing
	 *  properties - a data object that may contain any field from node.data as well 
	 *  as "name" and "id". Data fields must start with a $.
	 */
	this.setNodeData = function(nodeID, properties){
		// if we change the nodeType without specifying an icon,
		// take icon from nodeIcons array
		var nodeType = properties.$nodeType;
		if(nodeType && !properties.$icon){
			properties.$icon = nodeIcons[nodeType];
		}
		
		var node = fd.graph.getNode(nodeID);
		setObjectData(node, properties);
	}
	
	/**
	 * Removes properties of a node.
	 * 
	 * Parameters:
	 *  id - id of a node that needs changing
	 *  propertyNames - an array that contains the names of the properties
	 *  				that are to be deleted
	 */
	this.removeNodeData = function(id, propertyNames){
		
		var node = fd.graph.getNode(id);
		if(!node){
			return;
		}
				var data = node.data;
		for(var p in propertyNames){
			p = propertyNames[p];
			
			if(p == "name"){
				delete node.name;
			}
			else if ( p != "id"){
				delete data[p];
			}
		}
		
		// recalculate node dimensions in case we changed the font or name
		if(!data.$custom){
			updateNodeDimensions(node);
		}
	}
	
	 /**
	  *	Moves a node and its children to the specified position.
	  *
	  * Parameters:
	  *	 node - the node to be moved
	  *	 xPos - the horizontal position of the moved node. 0 is the center of the canvas' starting position
	  *	 yPos - the vertical position of the moved node. 0 is the center of the canvas' starting position
	  *	 smoothMove - used for smooth animation in autoLayout
	  *	 noPlot - if true, does not plot the graph anew
	  */
	 this.moveNode = function(node, x, y, smoothMove, noPlot){
		// snap to grid
		if(!smoothMove && node.data.$movable){
			var snapPos = snapToGrid(node, x, y);
			x = snapPos.x;
			y = snapPos.y;
		}
	 
		// parent position
		var pos = node.pos.getc(true);
		var deltaX = x - pos.x,
			deltaY = y - pos.y;
		
		// move parent
		if(smoothMove){
			node.setPos(new $jit.Complex(x, y), "end");
		}else{
			node.pos.setc(x, y);
		}
		 
		// move children in relation to their parent
		var children = node.data.$children,
			child;
		if(children){
			 for(var c in children){
				 
				 child = children[c];
				 pos = child.pos.getc(true);
				 moveNode(child, pos.x + deltaX, pos.y + deltaY, smoothMove, true);
			 }
		}
		
		if(!noPlot){
			fd.plot();
		}
	}
	
	/**
	 *	Moves a node to the top layer.
	 *
	 *	Parameters:
	 *	 node - the node that is moved to the top
	 *	 moveChildren - if true, the nodes children will be moved to top as well
	 */
	this.setNodeToTop = function(node, moveChildren){
		var nodes = fd.graph.nodes;
		delete nodes[node.id];
		nodes[node.id] = node;
		
		if(moveChildren){
			var children = node.data.$children;
			for(var c in children){
				setNodeToTop(children[c], true);
			}
		}
	}
	
	/**
	 * Highlights exactly one node, and un-highlights the previously highlighted node.
	 *
	 * Parameters:
	 *	 node - the node or edge object which will be highlighted.
	 *		    Can be null if no new node shall be highlighted.
	 *	 strokeColor - the color of the node stroke or a port's fillcolor.
	 *				   Can be null to leave the color unchanged.
	 *	 fillColor - the fill color of a nodefamily. Can be null to leave the color unchanged.
	 */
	this.markNode = function(node, strokeColor, fillColor){
  
	  	var isAnimated = animation.enabled && !animation.busyCanvas;
	  	var setter = 'current';
	  	if(isAnimated){
	  		setter = 'end';
	  	}
	  	
	  	if(!strokeColor){
	  		strokeColor = markStrokeColor;
	  	}
	  	if(!fillColor){
	  		fillColor = markFillColor;
	  	}
	  	
		// clean up the old marking
		if(markedNode){
			
			if(markedNode.node.nodeFrom){
				// ignore edges
			}
			else{
				var type = markedNode.node.data.$type;
				markedNode.node.setData('color', nodeStrokeColor[type], setter);
				markedNode.node.setData('textColor', nodeTextColor[type], setter);
				
				// change FillColor only if it exists
				if(nodeFillColor[type]){
					markedNode.node.setData('fillColor', nodeFillColor[type], setter);
				}
			}
			markedNode = null;
		}
		
		// do we highlight something new?
		if(node){
		
			if(node.nodeFrom != undefined){
				// ignore edges
			}else{
				if(strokeColor != null){
					node.setData('color', strokeColor, setter);
					node.setData('textColor', strokeColor, setter);
				}
				
				// change FillColor only if it exists
				if(fillColor != null  && nodeFillColor[node.data.$type]){
					node.setData('fillColor', fillColor, setter);
				}
			}
			markedNode = { 'strokeColor' : strokeColor, 'fillColor' : fillColor, 'node' : node};
		}
		
		// animate
		if(isAnimated){
			fd.animate({  
				modes: ['edge-property:color',
						'node-property:color'],
				transition: $jit.Trans.Quint.easeOut,  
				duration: animation.duration / 2,
				onComplete: function(){fd.plot();}
			}); 
		}else if(!animation.busyCanvas){
			fd.plot();
		}
	}
  
	/**
	 * Returns the currently marked node.
	 * 
	 * Return:
	 *  the node, marked by the markNode() function.
	 */
	this.getMarkedNode = function(){
	  return (markedNode) ? markedNode.node : false;
	}
	
	
	
	//////////////
	//	EDGES	//
	//////////////
	
	/**
	 *	Parameters:
	 *	 sourceID - id of the source node
	 *	 targetID - id of the target node
	 *
	 *	Return:
	 *	 The adjacence object connecting the specified nodes.
	 */
	this.getEdge = function(sourceID, targetID){
		return fd.graph.getAdjacence(sourceID, targetID);
	}
	
	/**
	* Iterates all edges connected to the specified node and its ports, 
	* calling a function on each adjacency.
	* 
	* Parameters:
	*  node - the node of which the adjacencies are iterated
	*  edgeFun - the function which is called for each adjacency
	*/
	this.iterateEdges = function(node, edgeFun){
		if(node.data.$custom){
		  node.eachAdjacency(edgeFun);
		  return;
		}
		
		var children = node.data.$children;
		for(var c in children){
		  c = children[c];
		  c.eachAdjacency(edgeFun);
		}
	}
	
	/**
	 * Checks to see if a dangling edge is connected to the mouse pointer.
	 * 
	 * Return:
	 *  true if a mouse edge exists
	 */
	this.isEdgeDragged = function(){
	  return navi.isDraggingEdge;
	}
	
	/**
	 * Enables / disables forced orthogonal edges.
	 *
	 * Parameters:
	 *  state - if true, enables orthogonal edges
	 */
	this.setOrthogonalEdges = function(state){
		orthogonalEdges = state;
	}

	/**
	 * Creates a dangling edge between a node and the mouse pointer.
	 * If such an edge exists already, create an edge between both nodes
	 * that were passed to this function.
	 * 
	 * Parameters:
	 *   nodeID - the node.id of the node which is to be connected to the mouse pointer
	 *   isIncoming - if true, the edge will point towards the node
	 *   label - an optional edge label
	 *   data - a data object which may overwrite the standard edge data
	 */
	this.connectEdge = function(nodeID, isIncoming, label, data){
		// second node clicked?
		if(selectedNode){
		
			var label = selectedNode.label;
			var newEdge = false;
			
			// do we create an edge or simply discard it
			if(nodeID){
			
				if(!data && data != false){
					for(var a in fd.graph.edges[mouseNode.id]){
						data = fd.graph.edges[mouseNode.id][a].data;
						break;
					}
				}
			
				/*
				// extract bendPoints from mouse edge
				var tempBendPoints;
				for(tempBendPoints in fd.graph.edges[mouseNode.id]){
					tempBendPoints = fd.graph.edges[mouseNode.id][tempBendPoints].data.$bendPoints;
					break;
				}
				// if no data is specified, transfer the bendPoints
				if(tempBendPoints && !data){
					data = {"$bendPoints" : tempBendPoints};
				}
				*/
				
				// determine direction of edge
				if(selectedNode.isIncoming){
					newEdge = addEdge(nodeID, selectedNode.id, label, data);
				}
				else{
					newEdge = addEdge(selectedNode.id, nodeID, label, data);
				}
			}
			
			// remove mouse edge
			if(selectedNode.isIncoming){
				removeEdge(mouseNode.id, selectedNode.id, true);
			}else{
				removeEdge(selectedNode.id, mouseNode.id, true);
			}
			
			navi.isDraggingEdge = false;
			selectedNode = null;
			
			return newEdge;
		}
		// first node clicked
		else if(nodeID){
			if(callListener("onCreateDanglingEdge", [getNode(nodeID), isIncoming, data]) == false){
				return false;
			}
			
			selectedNode = {"id": nodeID, "isIncoming" : isIncoming, "label" : label};
			navi.isDraggingEdge = true;
			// determine direction of mouse edge
			if(isIncoming){
				var edge = addEdge(mouseNode.id, nodeID, label, data);
				// calculate position for orthogonal edges
				navi.dragEdgeSourceX = mouseNode.pos.x + edge.data.$offsetFrom.x;
				navi.dragEdgeSourceY = mouseNode.pos.y + edge.data.$offsetFrom.y;
				return edge;
			}else{
				var edge = addEdge(nodeID, mouseNode.id, label, data);
				var node = getNode(nodeID);
				// calculate position for orthogonal edges
				navi.dragEdgeSourceX = node.pos.x + edge.data.$offsetFrom.x;
				navi.dragEdgeSourceY = node.pos.y + edge.data.$offsetFrom.y;
				return edge;
			}
		}else{
			selectedNode = null;
			navi.isDraggingEdge = false;
			return false;
		}
	}
  
	/**
	*  Adds a directed edge between two nodes to the graph.
	*  
	*  Parameters:
	*   sourceID - the id of the node from which the edge starts
	*   targetID - the id of the node where the edge ends
	*   edgeLabel - the label of the edge. Can be null if no label is required
	*   edgeData - a data object which may overwrite the standard edge data
	*   forced - if true, creates the edge unconditionally and does not trigger 
	*		 	a listener event
	*  Return:
	*   true if the edge was successfully added
	*/
	this.addEdge = function(sourceID, targetID, edgeLabel, edgeData, forced){
		if(edgeLabel && typeof edgeLabel == "string"){
			edgeLabel = edgeLabel.split('\n');
		}
		
		// look up the nodes which are to be connected
		var g = fd.graph;
		var sourcePort = g.getNode(sourceID),
			targetPort = g.getNode(targetID);
		
		// check if ports were found
		if(!sourcePort){
			callListener("onError", ["Error in function addEdge(): The source node '" + sourceID + "' does not exist."]);
			return false;
		}
		if(!targetPort){
			callListener("onError", ["Error in function addEdge(): The target node '" + targetID + "' does not exist."]);
			return false;
		}
		
		var data = (edgeData) ? edgeData : new Object();
		
		// fill in standard data if it is not defined
		if(!data.$type){
			data.$type = "flowArrow";
		}
		if(!data.$color){
			data.$color = edgeColor;
		}
		data.$direction = [sourceID, targetID ];
		
		// calculate offsets
		data.$offsetFrom = customEdgeMap[sourcePort.data.$type].getOffset(sourcePort, targetPort, true, data);
		data.$offsetTo = customEdgeMap[targetPort.data.$type].getOffset(sourcePort, targetPort, false, data)
		
		// add label to data
		if(edgeLabel){
			data.$label = edgeLabel;
        }
		
		// if it is a dangling edge, no listener needs to be called
		if(sourceID == mouseNode.id || targetID == mouseNode.id){
			
			data.$isMouseEdge = true;
			
			var isIncoming = (sourceID == mouseNode.id);
			var connectedPort = (sourceID == mouseNode.id) ? targetPort : sourcePort;
			
			// apply changes
			var newEdge = fd.graph.addAdjacence(sourcePort, targetPort, data);
			fd.plot();
			return newEdge;
		}
		
		delete data.$isMouseEdge;
		
		// call listener if the edge is not connected to the dummy node and check for permission
		if(!forced){
			// look for nodeFamilies if connected nodes are ports
			var sourceFamily, targetFamily;
			if(!(sourcePort.data.$custom && targetPort.data.$custom)){
				var children;
				iterateAllNodes(function(node){
					
					// look for nodeFamilies
					if(node.data.$type == "nodeFamily"){
						children = node.data.$children;
						
						// look through the ports
						for(var c in children){
							if(children[c].id == sourceID){
								sourceFamily = node;
							}
							else if(children[c].id == targetID){
								targetFamily = node;
								data.$targetFamily = node.id
							}
						}
					}
				});
			}
			if(callListener("onCreateEdge", [sourceFamily, targetFamily, sourcePort, targetPort, data]) == false){
				return false;
			}
		}
		// apply changes
		var newEdge = fd.graph.addAdjacence(sourcePort, targetPort, data);
		fd.plot();
		return newEdge;
	}
	
	/**
	* Removes the edge between two nodes.
	*  
	* Parameters: 
	*  sourceID - the id of the source Node
	*  targetID - the id of the target Node
	*  forced - if true, removes the edge unconditionally and
	*			  does not trigger a listener event
	*/
	this.removeEdge = function(sourceID, targetID, forced){  
		// look up the source node and the nodeFamilies of both nodes
		var g = fd.graph,
			source = g.getNode(sourceID), 
			target = g.getNode(targetID);

		var adja = g.getAdjacence(sourceID, targetID);

		// nothing to delete?
		if(!source || !adja){
			return;
		}
			
		if(!forced && sourceID != mouseNode.id && targetID != mouseNode.id){
			// look for nodeFamilies if connected nodes are ports
			var targetFamily = g.getNode(adja.data.$targetFamily);
			var sourceFamily;
			if(!(source.data.$custom && target.data.$custom)){
				var children;
				iterateAllNodes(function(node){
					
					// look for nodeFamilies
					if(node.data.$type == "nodeFamily"){
						children = node.data.$children;
						
						// look through the ports
						for(var c in children){
							if(children[c].id == sourceID){
								sourceFamily = node;
								break;
							}
						}
					}
				});
			}
			// call listener and ask for permission to delete
			if(!callListener("onRemoveEdge", [sourceFamily, targetFamily, source, target])){
				return;
			}
		}

		// apply changes
		fd.graph.removeAdjacence(sourceID, targetID);
		fd.plot();
	}
  
	/**
	 *	Changes one or more properties of an edge. Converts label strings to arrays.
	 *
	 *	Parameters:	
	 *	 sourceID - the ID of the node where the edge starts
	 *	 targetID - the ID of the node where the edge ends
	 *	 properties - a data object that may contain any field from
	 *				  data as well as "id". Data fields must start with a $.
	 */
	this.setEdgeData = function(sourceID, targetID, properties){
		
		var edge = fd.graph.getAdjacence(sourceID, targetID);
		setObjectData(edge, properties);
	}
	
	/**
	* Sets or removes BendPoints of an Edge. Also updates orthogonal edge position
	* if the edge is a dangling edge.
	* 
	* Parameters:
	*  sourceID - the ID of the connected outputPort
	*  targetID - the ID of the connected inputPort
	*  bendPoints - an array of {"x":..., "y":...} objects
	*/
	this.setBendPoints = function(sourceID, targetID, bendPoints){
		// change data
		fd.graph.getAdjacence(sourceID, targetID).data.$bendPoints = bendPoints;
		
		if(bendPoints.length > 0){
			if(sourceID == mouseNode.id){
				var lastBP = bendPoints[0];
				navi.dragEdgeSourceX = lastBP.x;
				navi.dragEdgeSourceY = lastBP.y;
			}else if(targetID == mouseNode.id){
				var lastBP = bendPoints[bendPoints.length - 1];
				navi.dragEdgeSourceX = lastBP.x;
				navi.dragEdgeSourceY = lastBP.y;
			}
		}
		
		// plot visual changes
		fd.plot();
	}
  
  
  
	//////////////////////
	//	STYLES - ICONS	//
	//////////////////////
	
	/**
	 * Reads a CSS style class and recolours the graph to fit the style.
	 * 
	 * Parameters:
	 *  cssClassName - a string representing the name of a css class
	 *  textIsBorderColor - if true, the node text gets the same color as the border
	 */
	this.setNodeStyleCSS = function(cssClassName, textIsBorderColor){
		
		var css = getCSSObject(cssClassName);
		if(!css){ return};
		
		var	bgColor = css.backgroundColor,
		 	borderColor = css.borderColor,
		 	font = css.fontFamily,
			textColor = css.color;
		
		// clean up colors for javascript use
		bgColor = (bgColor == "") ? null : cleanColorString(bgColor);
		borderColor =  (borderColor == "") ? null : cleanColorString(borderColor);
		textColor =  (textColor == "") ? null : cleanColorString(textColor);
		if(textIsBorderColor){
			textColor = borderColor;
		}
		
		// set node colors
		setNodeStyle(false, bgColor, borderColor, null, null, textColor, font);
		
		// set edge colors
		setNodeStyle("", null, null, borderColor);
	}
	
	/**
	 * Reads a css style class' attribute and recolours a specific part of the graph.
	 * 
	 * Parameters:
	 *  cssClassName - a string representing the name of a css class
	 *  cssAttribute - the css attribute of the class, e.g. 'color', 'border-color', etc
	 *  style - a string, representig what feature should be changed:
	 * 		  	'font' - the font of nodes
	 * 			'textColor' - the text color of nodes
	 * 			'strokeColor' - the border color of nodes and port colors
	 * 			'fillColor' - fillcolor of nodes
	 * 			'edgeColor' - color of edges
	 * 			'focusColor' - the mouse over highlight color
	 * 			'markFillColor' - the standard fill color for markNode(...)
	 *  		'markStrokeColor' - the standard stroke color for markNode(...)
	 */
	this.setExplicitNodeStyleCSS = function(cssClassName, cssAttribute, style){
		var css = getCSSObject(cssClassName);
		if(!css){ return};
		
		val = css[cssAttribute];
		if(style != "font"){
			val = cleanColorString(val);
		}
		switch(style){
			case 'font' :
				setNodeStyle(null, null, null, null, null, null, val);
				break;
			case 'textColor' :
				setNodeStyle(null, null, null, null, null, val);
				break;
			case 'strokeColor' :
				setNodeStyle(null, null, val);
				break;
			case 'fillColor' :
				setNodeStyle(null, val);
				break;
			case 'edgeColor' :
				setNodeStyle(null, null, null, val);
				break;
			case 'focusColor' :
				setNodeStyle(null, null, null, null, val);
				break;
			case 'markFillColor' :
				markFillColor = val;
				break;
			case 'markStrokeColor' :
				markStrokeColor = val;
				break;
		}
	}
	
	/**
	 *  Changes the standard colors, used for markNode().
	 *  
	 *  Parameters:
	 *    fillColor - the fill color of marked Nodes
	 *    strokeColor - the stroke color of marked Nodes
	 */
	this.setMarkedNodeStyle = function(fillColor, strokeColor){
		markFillColor = fillColor;
		markStrokeColor = strokeColor;
	}
	
	/**
	 * Changes the colors of the entire graph.
	 *  
	 * Parameters: 
	 *  nodeType - the name of the nodeType, of which fill- and stroke color are changed, 
	 *  		   or null if all nodeTypes are to be changed
	 *  fillColor - the fill color of nodes. If null, restore Node Color from global Array.
	 *  strokeColor - the stroke color of nodes. If null, restore Stroke Color from global Array.
	 *  eColor - the color of edges. If null, restore Edge Color from global Array.
	 *  focusColor - the of highlighted objects. If null, restore Edge Color from global Array.
	 *  textColor - the textColor of NodeFamilies and CustomNodes
	 *  font	- a font-family string
	 */
	this.setNodeStyle = function(nodeType, fillColor, strokeColor, eColor, focusColor, textColor, font){
		
		// if no nodeType is specified, change all nodeTypes
		if(!nodeType){
			for(var nodeType in nodeStrokeColor){
				setNodeStyle(nodeType, fillColor, strokeColor, eColor, focusColor, textColor, font);
			}
			return;
		}
		
		if(fillColor){
			nodeFillColor[nodeType] = fillColor;
		}
		if(strokeColor){
			nodeStrokeColor[nodeType] = strokeColor;
		}
		if(eColor){
			edgeColor = eColor;
		}
		if(focusColor){
			edgeColorFocus = focusColor;
			nodeColorFocus = focusColor;
		}
		if(textColor){
			nodeTextColor[nodeType] = textColor;
		}
		if(font){
			nodeTextFamily[nodeType] = font;
		}
		font = {"$font" : nodeTextFamily[nodeType]};
		
		var data;
		// change colors
		iterateAllNodes(function(node){
			data = node.data;
			
			if(data.$type == nodeType){
				// change node font
				if(node.data.$font){
					setNodeData(node.id, font);
				}
				// change node color
				if(!(markedNode && node == markedNode.node)){
					if(data.$fillColor){
						data.$fillColor = nodeFillColor[nodeType];
					}
					if(data.$color){
						data.$color = nodeStrokeColor[nodeType];
					}
					if(data.$textColor){
						data.$textColor = nodeTextColor[nodeType];
					}
				}
			}
			// change all edge colors
			if(eColor){
				node.eachAdjacency(function(adja){
					adja.data.$color = edgeColor;
				});
			}
		});
		fd.plot();
	}
	
	/**
	 * Removes all node icons.
	 */
	this.clearNodeIcons = function(){
		for(var i in nodeIcons){
			delete nodeIcons[i];
		}
	}
	
	/**
	 * Assigns an iconPath to a type of nodes, so that all nodes of that type
	 * will display the image located at the specified path.
	 * 
	 * Parameters: 
	 *  nodeType - the group name of the nodes that shall share the icon
	 *  path - the path to the image. If null, an existing icon will be deleted
	 *  resizeImmediately - if true, this function will not wait
	 *			 	 		until the image is loaded and instead resize all
	 *			  			affected nodes, regardless of if the image is loaded
	 *			  			successfully or not
	 */
	this.setNodeIcon = function(nodeType, path, resizeImmediately){
		if(path == null){
			delete nodeIcons[nodeType];
			
			// remove icons from displayed graph and shrink nodes
			var data;
			
			iterateAllNodes(function(node){
				// get data
				data = node.data;
				
				// update node width and port positions
				if(data.$nodeType == nodeType){
					delete data.$icon;
					updateNodeDimensions(node);
				}
			});
			
			return;
		}
		
		var prevImage = nodeIcons[nodeType];
		
		var icon;
		icon = new Image();
		
		// load error
		icon.onerror = function(){
			callListener("onError", ["Error in function setNodeIcon(): Could not load '"+ path +"'."]);
		};
		
		// load success
		icon.onload = function(){
			// this may happen on IE, when linking to images via web
			if(!icon.complete){
				callListener("onError", ["Error in function setNodeIcon(): Timeout while loading '"+ path +"'. Please try again!"]);
				return;
			}
			nodeIcons[nodeType] = icon;
			resizeNodes(icon, !resizeImmediately);
			fd.plot();
		};
		
		
		var resizeNodes = function(newIcon, resize){
			// if nodes were already created, we need to
			// adjust their width and iconPath
			var data;
			iterateAllNodes(function(node){
				// get data
				data = node.data;
				// update node width and port positions
				if(data.$nodeType == nodeType){
					data.$icon = newIcon;
					if(resize && !prevImage){
						updateNodeDimensions(node);
					}
				}
			});
		};
		
		if(resizeImmediately){
			nodeIcons[nodeType] = path;
			resizeNodes("emptyImage", true);
		}
		// attempt to load the image
		icon.src = path;
	}
	
	/**
	 * Parameters:
	 *  nodeType - the nodeType of the icon
	 *  
	 * Return:
	 *  the address of the icon that is linked to the nodeType
	 */
	this.getIconURL = function(nodeType){
		var ni = nodeIcons[nodeType];
		if(ni){
			// if image has not been loaded yet, the nodeIcon contains
			// its path
			if(typeof ni == "string"){
				return nodeIcons[nodeType];
			}else{
				return nodeIcons[nodeType].src;
			}
		}
		return false;
	}
	
	/**
	 * Return:
	 *  a String array of all nodeTypes that have a linked icon
	 */
	this.getIconTypes = function(){
		var types = new Array();
		for(var type in nodeIcons){
			types.push(type);
		}
		return types;
	}
	
	/**
	 * Reads a JSON String which was generated by saveGraph() and returns
	 * an Object containing two Arrays of nodeTypes and their respective URL
	 * 
	 * Parameters:
	 *  jsonString - a JSON String which was generated by saveGraph()
	 * 
	 * Return:
	 *  an Object containing two Arrays of nodeTypes and their respective URL
	 */
	this.getNodeIconsFromJSON = function(jsonString){
		var types  = new Array(), 
			urls = new Array();
		
		// load icons
		var iconData = JSON.parse(jsonString).icons;
		
		for(var type in iconData){
			types.push(type);
			urls.push(iconData[type]);
		}
		
		return { "types" : types, "urls" : urls};
	}
	
	/**
	 * Return:
	 *  an Object containing two Arrays containing nodeTypes and their respective URL
	 */
	this.getNodeIcons = function(){
		var types = new Array(), 
			urls = new Array();
		
		for(var type in nodeIcons){
			types.push(type);
			urls.push(getIconURL(type));
		}
		return { "types" : types, "urls" : urls};
	}
	
	
	////////////////////
	//	ERROR CHECKING	//
	////////////////////
	
	/**
	 * Checks a variable for type correctness.
	 * Sends an onError-event if the argument is not valid.
	 *	
	 * Parameters:
	 *  callFunction - the function where the argument needs to be checked
	 *	arg - the argument which is checked
	 *	type - the type of which the argument must be
	 *	
	 * Return:
	 *  true if the type of arg equals the specified type
	 */
	this.validArg = function(callFunction, arg, type){
		var valid = (typeof arg == type);
		if(!valid){
			callListener("onError", ["Error in function "+ callFunction 
										+ ": Invalid argument type for argument '"
										+ arg + "'. Must be of type " + type + "."]);
		}
		return valid;
	}
  
  	/**
	 * Checks if an object is a valid color string.
	 * Sends an onError-event if the string is not valid.
	 *
	 * Parameters:
	 *	callFunction - the name of the function of wich the argument needs to be checked
	 *  color - the color argument which is checked
	 *	
	 * Return:
	 *  true if the string is a well formed color string 
	 */
	this.validArgColor = function(callFunction, color){
		var valid = false;
		// is it a string at all ?
		if(typeof color == "string"){
			// does the string match "#RRGGBB" ?
			var regEx = /^#[a-f\d]{6}$/i;
			valid = color.match(regEx);
		}
		if(!valid){
			callListener("onError", ["Error in function "+ callFunction 
										+ ": Invalid color '"+ color 
										+ "'. Must be structured like '#RRGGBB'."]);
		}
		return valid;
	}
	
	
	/**
	 * Checks if an object is a valid node.
	 * Sends an onError-event if the Object does not have an id.
	 *	
	 * Parameters:  
	 *  callFunction - the function where the argument needs to be checked
	 *	node - the object argument which is checked
	 *
	 * Return:
	 *  true if the object has an id
	 */
	this.validArgNode = function(callFunction, node){
		var valid = false;
		var id;
		
		// is it an object at all ?
		
		if(node && typeof node == "object"){
			id = node.id;
			
			// does the object have an id
			valid = (typeof id == "string");
		}
		
		if(!valid){
			callListener("onError", ["Error in function "+ callFunction 
										+ ": Invalid Node '"+ node 
										+ "'. Must have a field 'id'."]);
			return valid;
		}
		
		// this regex prevents ids that dont start with a letter and contain spaces
		// This prevents graph hierarchy and autolayout errors
		var regexName = /(^[A-z][^\s]*)/gi;
		var regex = regexName.exec(id)
		
		valid = regex && (regex.input == regex[0]);
		if(!valid){
			callListener("onError", ["Error in function "+ callFunction 
										+ ": Invalid NodeID '"+ id 
										+ "'. IDs must start with a letter and may not contain space characters."]);
			return valid;
		}
		
		return valid;
	}
	
	/**
	 * Checks if a specified node id string is correct and non-existent in the graph.
	 * Sends an onError-event if the argument id is not valid.
	 * 
	 * Parameters:
	 *  callFunction - the function where the argument needs to be checked
	 *  id - the id which is checked
	 * 
	 * Return:
	 *  false if the id is not a string or exists already
	 */
	this.validArgID = function(callFunction, id){
		// check if id is a string first
		if (!validArg(callFunction, id, "string")){
			return false;
		}
		// check if node id is occupied
		var nodeExists = fd.graph.getNode(id);
		if(nodeExists){
			callListener("onError", ["Error in function "+ callFunction 
										+ ": Duplicate Node ID '"
										+ id + "'."]);
		}
		return !nodeExists;
	}
	
	
	
	///////////////////////
	//	PRIVATE FUNCTIONS	//
	///////////////////////
	
	/**
	 *	Converts edges to an XML-Tag string.
	 *
	 * Return:
	 *	 a XML-string containing edge information
	 */
	function edgesToXML(){
		var xmlArray = new Array();
		
		var ignoreList = new Array("$direction", "$color", "$label", "$offsetFrom", "$offsetTo", "$type");
		var validData = function(key){
			for(var a in ignoreList){
				if(ignoreList[a] == key){
					return false;
				}
			}
			return true;
		}
		var attrCustom = {
			'Label' : false,
			'Source' : false,
			'Target' : false,
			'Type' : false
		}
		
		var edgeToXML = function(adja){
			var data = adja.data;
			// prevent duplicate edge detection
			if(data.$direction[0] == adja.nodeFrom.id){
				var attrCount = 3;
				
				// read edge label
				if(data.$label){
					attrCustom.Label = data.$label.join("\\n");
					data.$label = encodeXMLString(data.$label);
					attrCount++;
				}else{
					attrCustom.Label = false;
				}
				// read other edge attributes
				attrCustom.Source = data.$direction[0];
				attrCustom.Target = data.$direction[1];
				attrCustom.Type = data.$type;
				
				xmlArray.push(makeXMLTag(2, "Edge", attrCustom, attrCount, true));
			}
		}
		xmlArray.push('\t<EdgeList>');
		iterateAllNodes(function(node){
			// continue for unfitting nodes
			if(!node.data.$custom && node.data.$type != "nodeFamily"){
				return;
			}
			iterateEdges(node, edgeToXML);
		});
		
		xmlArray.push('\t</EdgeList>');
		return xmlArray.join("\n");
	}
	
	/**
	 *	Converts node styles to an XML-Tag string.
	 *
	 * Return:
	 *	 a XML-string containing color and icon information
	 */
	function layoutToXML(){
		var xmlArray = new Array();
		
		// define global colors
		{
			var topArray = new Array('\t<LayoutData EdgeColor="', edgeColor, '" HoverStrokeColor="', nodeColorFocus);
			if(markFillColor){
				topArray.push('" MarkFillColor="', markFillColor);
			}
			if(markStrokeColor){
				topArray.push('" MarkStrokeColor="', markStrokeColor);
			}
			topArray.push('">');
			xmlArray.push(topArray.join(""));
		}
		
		// define node icons
		var iconArray = new Array('\t\t<Icon IconID="', false, '" URL="', false, '"/>');
		for(var iconID in nodeIcons){
			iconArray[1] = iconID;
			iconArray[3] = getIconURL(iconID);
			xmlArray.push(iconArray.join(""));
		}
		
		// define node colors
		// attempt to save the whole layout in one entry
		testSame: {
			var attributes = {
				'FillColor' : false,
				'StrokeColor' : false,
				'TextColor' : false,
				'Font' : false,
				'Type' : false
			};
			var base = false;
			for(var n in nodeStrokeColor){
				if(!base){
					base = nodeStrokeColor[n];
				}else if(nodeStrokeColor[n] != base){
					break testSame;
				}
			}
			attributes.StrokeColor = base;
			
			base = false;
			for(var n in nodeFillColor){
				if(!base){
					base = nodeFillColor[n];
				}else if(nodeFillColor[n] != base){
					break testSame;
				}
			}
			attributes.FillColor = base;
			
			base = false;
			for(var n in nodeTextColor){
				if(!base){
					base = nodeTextColor[n];
				}else if(nodeTextColor[n] != base){
					break testSame;
				}
			}
			attributes.TextColor = base;
			
			base = false;
			for(var n in nodeTextFamily){
				if(!base){
					base = nodeTextFamily[n];
				}else if(nodeTextFamily[n] != base){
					break testSame;
				}
			}
			attributes.Font = base;
			attributes.Type = "#all";
			xmlArray.push(makeXMLTag(2, "NodeStyle", attributes, 5, true));
			xmlArray.push('\t</LayoutData>');
			return xmlArray.join("\n");
		}
		
		var tagList = new Object();
		var attrCountList = new Object();
		
		for(var n in nodeStrokeColor){
			tagList[n] = {
				'FillColor' : false,
				'StrokeColor' : nodeStrokeColor[n],
				'TextColor' : false,
				'Font' : false,
				'Type' : n};
			attrCountList[n] = 2;
		}
		for(var n in nodeFillColor){
			if(!tagList[n]){
				tagList[n] = {
				'FillColor' : nodeFillColor[n],
				'StrokeColor' : false,
				'TextColor' : false,
				'Font' : false,
				'Type' : n};
				attrCountList[n] = 2;
			}else{
				tagList[n].FillColor = nodeFillColor[n];
				attrCountList[n]++;
			}
		}
		for(var n in nodeTextColor){
			if(!tagList[n]){
				tagList[n] = {
				'FillColor' : false,
				'StrokeColor' : false,
				'TextColor' : nodeTextColor[n],
				'Font' : false,
				'Type' : n};
				attrCountList[n] = 2;
			}else{
				tagList[n].TextColor = nodeTextColor[n];
				attrCountList[n]++;
			}
		}
		for(var n in nodeTextFamily){
			if(!tagList[n]){
				tagList[n] = {
				'FillColor' : false,
				'StrokeColor' : false,
				'TextColor' : false,
				'Font' : nodeTextFamily[n],
				'Type' : n};
				attrCountList[n] = 2;
			}else{
				tagList[n].Font = nodeTextFamily[n];
				attrCountList[n]++;
			}
		}
		for(var n in tagList){
			xmlArray.push(makeXMLTag(2, "NodeStyle", tagList[n], attrCountList[n], true));
		}
		xmlArray.push('\t</LayoutData>');
		return xmlArray.join("\n");
	}
	
	/**
	 *	Converts all nodes to xml tags.
	 *
	 *	Return:
	 *	 a XML string containing all nodes
	 */
	function nodesToXML(){
		var xmlArray = new Array();
		xmlArray.push('\t<NodeList>');
		
		var tabCount = 2;
		var attributes = {
			'NodeID' : false,
			'Label' : false,
			'IconID' : false,
			'Tooltip' : false,
			'Type' : false
		};
		
		var nodeCheckList = new Object();
		var nodeToXML = function(node){
			if(nodeCheckList[node.id]){
				return; // continue
			}
			nodeCheckList[node.id] = true;
		
			data = node.data;
			type = data.$type;
			custom = data.$custom;
			
			// read custom nodes and nodeFamilies
			if(custom){
				attrCount = 2;
				attributes.Type = type;
				attributes.IconID = false;
			}
			else if(type == "nodeFamily"){
				attrCount = 1;
				attributes.Type = false;
				
				// get iconID
				if(val = data.$nodeType){
					attributes.IconID = val;
					attrCount++;
				}else{
					attributes.IconID = false;
				}
			}else{
				return;
			}
			// get id, name, and tooltip
			attributes.NodeID = node.id;
			if(val = node.name){
				
				attributes.Label = (typeof val == "string") ? val : val.join("\\n");
				attributes.Label = encodeXMLString(attributes.Label);
				attrCount++;
			}else{
				attributes.Label = false;
			}
			if(val = data.$tooltip){
				attributes.Tooltip = val;
				attrCount++;
			}else{
				attributes.Tooltip = false;
			}
			
			// get data and ports
			var dataString = dataToXML(tabCount + 1, data, custom);
			
			var children, portString;
			if(custom){
				portString = false;
				tagName = "CustomNode";
				children = node.data.$children;
				children = (children && children.length > 0) ? children : false;
			}else{
				children = false;
				portString = portsToXML(tabCount + 1, data.$children)
				tagName = "PortNode";
			}
			var bigTag = (dataString || portString || children);
			
			// open node tag
			xmlArray.push(makeXMLTag(tabCount, tagName, attributes, attrCount, !bigTag));
			if(dataString){
				xmlArray.push(dataString);
			}
			// parse ports
			if(portString){
				xmlArray.push(portString);
			}
			// parse children
			else if(children){
				xmlArray.push(makeXMLTag(tabCount + 1, "NodeList", false, 0, false));
				tabCount += 2;
				for(var child in children){
					nodeToXML(children[child]);
				}
				tabCount -= 2;
				xmlArray.push(makeXMLClosingTag(tabCount + 1, "NodeList"));
			}
			
			// close node tag
			if(bigTag){
				xmlArray.push(makeXMLClosingTag(tabCount, tagName));
			}
		}; 
		
		var data, type, attrCount, val, custom, tagName;
		iterateAllNodes(nodeToXML);
		xmlArray.push('\t</NodeList>');
		return xmlArray.join("\n");
	}
	
	/**
	 *	Parses children of a node and writes the ports as xml tags.
	 *
	 *	Parameters:
	 *	 tabCount - the number of tab symbols before the tag begins
	 *	 children - the children of the node
	 *
	 *	Return:
	 *	 a XML string that contains all ports of the node, or false if no ports exist
	 */
	function portsToXML(tabCount, children){
		// if only one child exists, it's the close-button
		if(children.length < 2){
			return false;
		}
		var tabs = makeTabs(tabCount);
		var portArray = new Array(tabs, '<Port PortID="', false, '" PortType="', false, '"/>');
		var xmlArray = new Array();
		
		var child, type;
		for(var c = 0, l = children.length; c < l; c++){
			child = children[c];
			type = child.data.$type
			portArray[2] = child.id;
			switch(type){
				case 'inputPort': 
					portArray[4] = 'input';
					xmlArray.push(portArray.join(""));
					break;
				case 'outputPort': 
					portArray[4] = 'output';
					xmlArray.push(portArray.join(""));
					break;
				case 'repositoryPort': 
					portArray[4] = 'repository';
					xmlArray.push(portArray.join(""));
					break;
			}
		}
		return (xmlArray.length == 0) ? false : xmlArray.join("\n");
	}
	
	/**
	 *	Parses data of a node and writes it as xml tags.
	 *
	 *	Parameters:
	 *	 tabCount - the number of tab symbols before the tag begins
	 *	 data - the data object which is to be parsed
	 *	 custom - if true, data of a custom node is being parsed
	 *
	 *	Return:
	 *	 a XML string that contains all data of the node, or false if no valid
	 *	 data is saved
	 */
	function dataToXML(tabCount, data, custom){
		// only save necessary, non-redundant data, ignore all other entries
		var ignoreList = new Array("$alpha", "$color", "$fillColor", "$textColor", "$icon", "$custom", "$tooltip", "$font", "$type", "$children");
		if(!custom){
			ignoreList.push("$height", "$width", "$movable", "$nodeType", "$dim");
		}
		var validData = function(key){
			for(var a in ignoreList){
				if(ignoreList[a] == key){
					return false;
				}
			}
			return true;
		}
		
		// test if no data is saved
		var notEnoughData = true;
		for(var d in data){
				var val = data[d];
				var type = (typeof val);
				if(val != undefined && validData(d) && (type == "object" || type != "function")){
					notEnoughData = false;
					break;
				}
		}
		if(notEnoughData){
			return false;
		}
		var xmlArray = new Array();
		
		var tabs = makeTabs(tabCount++);
		var innerTabs = tabs + "\t";

		// make data opening tag
		xmlArray.push(tabs + '<DataObject Key="data" IsArray="false">');
		
		// prepare tab array
		var dataArray = new Array(innerTabs, '<Data', false, ' Key="', false, '" Value="', false, '"/>');
		var dataObjectArray = new Array(innerTabs, '<DataObject Key="', false, '" IsArray="', false, '">');
		var dataObjectClose = innerTabs + '</DataObject>';
		
		var parseData = function(pData, depth){
			var val, type;
			for(var d in pData){
				val = pData[d];
				if(val != undefined && validData(d)){
					type = (typeof val);
					type = String.fromCharCode(type.charCodeAt(0) - 32) + type.substr(1);
					
					if(type == "Object"){
						// wrap nodes as references
						if(val.id && val.data && val.data.$type){
							dataArray[2] = "NodeRef";
							dataArray[4] = d;
							dataArray[6] = val.id;
							xmlArray.push(makeTabs(depth) + dataArray.join(""));
						}
						else{
							dataObjectArray[2] = d;
							dataObjectArray[4] = (Object.prototype.toString.call(val) === "[object Array]");
							var addTabs = makeTabs(depth);
							xmlArray.push(addTabs + dataObjectArray.join(""));
							
							// parse inner data ("We need to go deeper!")
							parseData(val, depth + 1);
							xmlArray.push(addTabs + dataObjectClose);
						}
					}
					else if(type != "Function"){
						if(type == "String"){
							val = encodeXMLString(val);
						}
						dataArray[2] = type
						dataArray[4] = d;
						dataArray[6] = val;
						xmlArray.push(makeTabs(depth) + dataArray.join(""));
					}
				}
			}
		};
		parseData(data, 0);
		xmlArray.push(tabs + '</DataObject>');
		return xmlArray.join("\n");
	}
	
	/**
	 *	Encodes a string, escaping certain characters as XML numeric sequences,
	 * allowing the string to appear in a XML-document.
	 *
	 *	Parameters:
	 *	 s - the string that needs to be encoded
	 *
	 *	Return:
	 *  a string as it appears in a XML-document
	 */
	function encodeXMLString(s) {
		return s.replace(/&/g, "&#38;").replace(/</g, "&#60;").replace(/>/g, "&#62;").replace(/"/g, "&#34;").replace(/'/g, "&#39;");
	}
	
	/**
	 * Concatenates and returns tabulator symbols.
	 *
	 *	Parameters:
	 *	 tabCount - the number of tab symbols
	 *
	 * Return:
	 *  a string that contains tab symbols
	 */
	function makeTabs(tabCount){
		if(tabCount == 0){
			return "";
		}
		if(tabCount == 1){
			return "\t";
		}
		
		var tabArray = new Array(tabCount);
		while(tabCount > 0){
			tabCount--;
			tabArray[tabCount] = '\t';
		}
		return tabArray.join("");
	}
	
	/**
	 * Writes a XML-closing tag with attributes and preceding tabulator chars.
	 *
	 *	Parameters:
	 *	 tabCount - the number of tab symbols
	 *	 tagName - the name of the tag
	 * Return:
	 *  a XML-closing tag
	 */
	function makeXMLClosingTag(tabCount, tagName){
		var xmlTagArray = new Array(4);
		xmlTagArray[0] = makeTabs(tabCount);
		xmlTagArray[1] = "</";
		xmlTagArray[2] = tagName;
		xmlTagArray[3] = ">";
		return xmlTagArray.join("");
	}
	
	/**
	 * Writes a XML-tag with attributes and preceding tabulator chars.
	 *
	 * Return:
	 *  a XML-opening tag
	 */
	function makeXMLTag(tabCount, tagName, attributes, attrCount, isSingle){
		var xmlTagArray = new Array(4 + attrCount * 5);
		
		xmlTagArray[0] = makeTabs(tabCount);
		xmlTagArray[1] = "<";
		xmlTagArray[2] = tagName;
		
		var p = 3;
		if(attrCount != 0){
			var attr;
			for(var a in attributes){
				attr = attributes[a];
				if(attr){				
					xmlTagArray[p++] = ' ';
					xmlTagArray[p++] = a;
					xmlTagArray[p++] = '="';
					xmlTagArray[p++] = attr;
					xmlTagArray[p++] = '"';
				}
			}
		}
		xmlTagArray[p] = (isSingle) ? '/>' : '>';
		return xmlTagArray.join("");
	}
	
	/**
	 *	Parses a XML-Node that contains colour and icon information.
	 *
	 *	Parameters:
	 *	 xmlLayout - a layout XML-Node that is to be parsed
	 */
	function parseXMLLayout(xmlLayout){
	
		// set marked node style
		{
			var mFillColor = xmlLayout.getAttribute("MarkFillColor");
			if(mFillColor){
				markFillColor = mFillColor;
			}
			var mStrokeColor = xmlLayout.getAttribute("MarkStrokeColor");
			if(mStrokeColor){
				markStrokeColor = mStrokeColor;
			}
			// set edge and hover colors
			var eColor = xmlLayout.getAttribute("EdgeColor");
			var hColor = xmlLayout.getAttribute("HoverStrokeColor");
			if(eColor || hColor){
				setNodeStyle(false, false, false, eColor, hColor);
			}
			
		}
		// set other colours
		var children = xmlLayout.children;
		var child;
		for(var i = 0, l = children.length; i < l; i++){
			child = children[i];
			// import icon
			if(child.localName == "Icon"){
				var iconID = child.getAttribute("IconID");
				var path = child.getAttribute("URL");
				setNodeIcon(iconID, path, true);
			}
			// set node style
			else{
				var type = child.getAttribute("Type");
				var fillColor = child.getAttribute("FillColor");
				var strokeColor = child.getAttribute("StrokeColor");
				var textColor = child.getAttribute("TextColor");
				var font = child.getAttribute("Font");
				type = (type == "#all") ? false : type;
				setNodeStyle(type, fillColor, strokeColor, false, false, textColor, font);
			}
		}
	}
	
	/**
	 *	Parses XML-Nodes that contains edge information.
	 *
	 *	Parameters:
	 *	 oldIDTable - a map between the old XML-Node-IDs and the created node IDs
	 *	 xmlIcons - an array of XML-Nodes that are to be parsed
	 *	 forced - if true, no "onCreateEdge" events are fired
	 */
	function parseXMLEdges(oldIDTable, xmlEdges, forced){
		var edge, label, oldSourceID, oldTargetID, data, sourcePort, targetPort;
		for(var i = 0, l = xmlEdges.length; i < l; i++){
			edge = xmlEdges[i];
			label = edge.getAttribute("Label");
			oldSourceID = edge.getAttribute("Source");
			oldTargetID = edge.getAttribute("Target");
			
			// handle linebreaks of label
			if(label){
				label = label.replace(/\\n/g, "\n");
			}
			// retrieve edge data
			{
				var xmlData = edge.getElementsByTagName("DataObject");
				if(xmlData.length > 0){
					data = parseXMLDataObject(xmlData[0]);
				}else{
					data = new Object();
				}
				data.$type = edge.getAttribute("Type")
			}
			
			// find the old IDs of the connected nodes
			var sourceID = oldIDTable[oldSourceID];
			var targetID = oldIDTable[oldTargetID];
			
			addEdge(sourceID, targetID, label, data, forced);
		}
	}
		
	/**
	 *	Parses a XML-Node that contains a data object, used by nodes and edges.
	 *
	 *	Parameters:
	 *	 xmlDate - the input XML-Node that is to be parsed
	 *	
	 *	Return:
	 *	 a data object that represents the XML-structure of the input
	 */
	function parseXMLDataObject(xmlData){
		var isArray = xmlData.getAttribute("IsArray") == "true";
		var dChildren = xmlData.children;
		var data = (isArray) ? new Array(dChildren.length) : new Object();
		for(var d = 0, dl = dChildren.length; d < dl; d++){
		
			var dChild = dChildren[d];
			var dType = dChild.localName.substr(4);
			var key = (isArray) ? parseInt(dChild.getAttribute("Key")) : dChild.getAttribute("Key");
			var value = dChild.getAttribute("Value");
			
			switch(dType){
				case "String" : data[key] = value; break;
				case "Number" : data[key] = parseFloat(value); break;
				case "Boolean" : data[key] = (value == "true"); break;
				case "Object" : data[key] = parseXMLDataObject(dChild); break;
				case "NodeRef" : data[key] = "#NODEREF:" + value; break;
			}
		}
		return data;
	}
		
	/**
	 *	Parses XML-Nodes, creating graph nodes in the process.
	 *
	 *	Parameters:
	 *	 nodeArray - an array of XML-Nodes that are to be created
	 *	 parent - the optional parent node of the nodes that are added
	 *	 forced - if true, no "onCreateNode" events are fired
	 *
	 *	Return:
	 *	 a map between the old XML-Node-IDs and the created node IDs
	 */
	function parseXMLNodes(nodeArray, parent, forced){
		var oldIDTable = new Object();
		
		for(var i = 0, l = nodeArray.length; i < l; i++){
			var xmlNode = nodeArray[i];
			var custom = (xmlNode.localName == "CustomNode");
			var nodeDef = new Object();
			
			// retrieve id and label
			nodeDef.name = xmlNode.getAttribute("Label");
			nodeDef.tooltip = xmlNode.getAttribute("Tooltip");
			if(nodeDef.tooltip == undefined){
				delete nodeDef.tooltip;
			}
			// handle linebreaks
			if(nodeDef.name){
				nodeDef.name = nodeDef.name.replace(/\\n/g, "\n");
			}
			
			// retrieve other data and children
			var data = new Object();
			var children = xmlNode.children;
			var childNodes = false;
			for(var c = 0, cl = children.length; c < cl; c++){
				var dataNode = children[c];
				if(dataNode.localName == "DataObject"){
					data = parseXMLDataObject(dataNode);
				}
				else if(dataNode.localName == "NodeList"){
					childNodes = dataNode.children;
				}
			}
			
			// prepare node specific stuff
			var type;
			if(custom){
				// allow node movement if it is not explicitly forbidden
				if(data.$movable == undefined){
					data.$movable = true;
				}
				type = xmlNode.getAttribute("Type");
			}
			else{
				type = xmlNode.getAttribute("IconID");
			}
			nodeDef.data = data;
			
			var newNode;
			if(custom){
				newNode = addCustomNode(0, 0, nodeDef.name, type, data, forced);
				
				// parse child nodes
				if(childNodes){
					var innerTable = parseXMLNodes(childNodes, newNode, forced);
					
					// merge oldIDTable of children
					for(var t in innerTable){
						oldIDTable[t] = innerTable[t];
					}
					delete innerTable;
				}
			}else{
				// parse ports
				var inputPorts = 0;
				var outputPorts = 0;
				var repositoryPorts = 0;
				
				children = xmlNode.getElementsByTagName("Port");
				for(var c = 0, cl = children.length; c < cl; c++){
					
					var child = children[c];
					
					var portType = child.getAttribute("PortType");
					switch(portType){
						case "input" : 
							oldIDTable[child.getAttribute("PortID")] = "nodeFamily" + nodeIDCounter + ".inputPort" + inputPorts;
							inputPorts++; 
							break;
						case "output" :  
							oldIDTable[child.getAttribute("PortID")] = "nodeFamily" + nodeIDCounter + ".outputPort" + outputPorts;
							outputPorts++; 
							break;
						case "repository" :  
							oldIDTable[child.getAttribute("PortID")] = "nodeFamily" + nodeIDCounter + ".repositoryPort" + repositoryPorts;
							repositoryPorts++; 
							break;
					}
					
				}
				
				// make node
				newNode = addNode(0, 0, nodeDef, repositoryPorts, inputPorts, outputPorts, type, forced);
			}
			
			// make entry in old ID table
			oldIDTable[xmlNode.getAttribute("NodeID")] = newNode.id;
			
			// add child to parent
			if(parent){
				addChild(newNode, parent);
			}
		}
		return oldIDTable;
	}
	
	/**
	 * 	Overwrites the document's onKeyDown function,
	 *  catching the keyCode and sending it to the 
	 *  event listener.
	 *  
	 *  Parameters:
	 *   event - the key down event
	 */
	document.onkeydown = function (event) {
		  if (!event){
			  event = window.event;
		  }
		  var keyCode;
		  
		  if (event.which) {
			  keyCode = event.which;
		  } else if (event.keyCode) {
			  keyCode = event.keyCode;
		  } 
		  callListener("onKeyDown", [keyCode]);
		  return true;
	}
	
	/**
	 * 	Overwrites the document's onKeyUp function,
	 *  catching the keyCode and sending it to the 
	 *  event listener.
	 *  
	 *  Parameters:
	 *   event - the key down event
	 */
	document.onkeyup = function (event) {
		  if (!event){
			  event = window.event;
		  }
		  var keyCode;
		  
		  if (event.which) {
			  keyCode = event.which;
		  } else if (event.keyCode) {
			  keyCode = event.keyCode;
		  } 
		  callListener("onKeyUp", [keyCode]);
		  return true;
	}
	
	/**
	 * Fetches the CSS rules of an input String.
	 * 
	 * Parameters:
	 *  className - name of the CSS rule
	 *  
	 * Return:
	 *  TODO
	 */
	function getCSSObject(className) {
	    var sheets = document.styleSheets;
		var x, classes;
		// browse .css files
	    for(var i = sheets.length -1; i >= 0; i--){
	    	classes = sheets[i].rules || sheets[i].cssRules;
	    	
	     	// browse css rules
	    	for(x = 0; x < classes.length; x++) {
	            if(classes[x].selectorText == className) {
	                    return classes[x].style;
	            }
	        }
	    }
	    return null;
	}

	/**
	 * This method receives a color from a css rule
	 * and rewrites it in a way that can be used by javascript.
	 * 
	 * Parameters:
	 *  cssColor - the input color string
	 * 
	 * Return:
	 *  a "#rrggbb" color string
	 */
	function cleanColorString(cssColor){
		if(!cssColor || cssColor.indexOf("rgb") == -1){
			// the color is either #rrggbb or a name
			return cssColor;
		}
		// the color is a rgb(...) type of string:
		var regex = /rgb\s*\((\w+),\s*(\w+),\s*(\w+)\s*\)[\s\d\D]*/gi;
		var result = regex.exec(cssColor);
		
		// if regex does not match, the colour is likely in an unusual format
		// return gray as to not break the editor
		if(!result){
			return "#888888";
		}
		// bring Red to Hex value
		var r = parseInt(result[1]).toString(16);
		r = (r.length == 2) ? r : "0" + r;
		
		// bring Green to Hex value
		var g = parseInt(result[2]).toString(16);
		g = (g.length == 2) ? g : "0" + g;
		
		// bring Blue to Hex value
		var b = parseInt(result[3]).toString(16);
		b = (b.length == 2) ? b : "0" + b;

		return "#"+r+g+b;
	}
	
	/**
	 * Changes one or more properties of a node or edge. Converts displayable strings 
	 * to arrays in order to allow linebreaks, and recalculates node size if the font
	 * has changed.
	 * 
	 * Parameters:
	 *  object - a node or an edge that needs changing
	 *  properties - a data object that may contain any field from
	 *			  node.data as well as "name" and "id".
	 */
	function setObjectData(object, properties){
		var isEdge = !!object.nodeFrom;
		var value;
		
		// iterate through properties object
		for(var field in properties){
			value = properties[field];
			
			// convert string to array if it is a displayable name
			if((field == "name" || field == "$label") && typeof value == "string"){
				value = value.split("\n");
				object[field] = value;
			}
			
			// if id is changed, we need to change some entries
			else if(!isEdge && field == "id"){
				renameNodeID(object, value);
			}
			// make changes to data
			else{
				// treat a new font name by appending the font size as well
				if(field == "$font"){
					value = "px " + ((value) ? value : "Lucida Console");
				}
				else if(value && field == "$unselectable" && hover == object){
					setHighlight(false);
				}
				else if(isEdge && field == '$type'){
					// apply new value to data
					object.Edge[field] = value;
				}
				
				// apply new value to data
				object.data[field] = value;
			}
		}
		// recalculate node dimensions in case we changed the font or name
		if(!isEdge && !object.data.$custom){
			updateNodeDimensions(object);
		}
		
		// apply visual changes
		fd.plot();
	}
	
	/**
	 * Renames the id field of a node.
	 *
	 * Parameters:
	 *	node - the node to be renamed
	 *	newID - the new id
	 */
	function renameNodeID(node, newID){
		var g = fd.graph;
		var oldID = node.id;
		
		// make sure the id is not the same, otherwise the node will be deleted
		if(newID != oldID){
			node.id = newID;
			// update graph.nodes
			g.nodes[newID] = node;
			delete g.nodes[oldID];
			
			// update graph.edges
			{
				g.edges[newID] = g.edges[oldID];
				delete g.edges[oldID];
				
				var edges = g.edges[newID];
				var dir;
				for(var e in edges){
					dir = edges[e].data.$direction;
					if(dir[0] == oldID){
						dir[0] = newID;
					}else{
						dir[1] = newID;
					}
					g.edges[e][newID] = g.edges[e][oldID];
					delete g.edges[e][oldID];
				}
			}
			
			if(node.data.$custom){
				return;
			}
			// rename ports
			var idLength = oldID.length;
			var children = node.data.$children;
			var ipCount = 0;
			var opCount = 0;
			var rpCount = 0;
			var newChildId;
			
			for(var child in children){
				child = children[child];
				var childId = child.id;
				var type = child.data.$type;
				switch(type){
					case "inputPort":
						newChildId = newID + ".inputPort" + ipCount;
						ipCount++;
						break;
					case "outputPort":
						newChildId = newID + ".outputPort" + opCount;
						opCount++;
						break;
					case "repositoryPort":
						newChildId = newID + ".repositoryPort" + rpCount;
						rpCount++;
						break;
					case "crossBox":
						newChildId = newID + ".closeButton";
						break;
				
				}
				child.id = newChildId;
				
				// update edges connected to the port
				var adjas = child.adjacencies;
				for(var a in adjas){
					var adja = adjas[a];
					var dir = adja.data.$direction;
					if(dir[0] == childId){
						dir[0] = newChildId;
					}else{
						dir[1] = newChildId;
						adja.data.$targetFamily = newID;
					}
					
					// update graph node
					var adjaEntry = (adja.nodeFrom.id == newChildId) ? adja.nodeTo : adja.nodeFrom;
					adjaEntry.adjacencies[newChildId] = adja;
					delete adjaEntry.adjacencies[childId];
				}
				
				// update graph node
				g.nodes[newChildId] = child;
				delete g.nodes[childId];
				g.edges[newChildId] = g.edges[childId];
				delete g.edges[childId];
			}
		}
	}
	
	/**
	 * Calculates and changes the width and height of a nodeFamily, 
	 * depending on its font, and repositions the ports.
	 *	  
	 * Parameters:
	 *  node - the nodeFamily of the graph
	 */
	function updateNodeDimensions(node){
		
		var data = node.data;
		
		if(data.$type != 'nodeFamily'){
			return;
		}
		
		// reserve icon space
		var iconSpace = 0;
		if(data.$icon){iconSpace = 4;}
		// calculate width of name
		var width = 0;
		
		var font = data.$font;
		if(font){
			font = (data.$dim * 0.83) + font;
		}else{
			font = (data.$dim * 0.83) + "px Lucida Console";
		}
		
		// calculate width of longest text line
		var textArray = node.name;
		for(var t in textArray){
			width = Math.max(width, graph.getTextWidth(textArray[t], font));
		}
		
		// calculate nodeFamily dimensions
		width += (4 + iconSpace) * vertexLabelSize;
		
		// check if the width changed at all
		if(data.$width == width){
			return;
		}
		
		// calculate how much bigger/smaller the width got
		var delta = (width - node.data.$width) / 2;
		
		// update width
		node.data.$width = width;
		width /= 2;
		
		// update port positions
		var portCounter = {
			'inputPort' : 0,
			'outputPort' : 0,
			'repositoryPort' : 0};
			
		var children = data.$children,
			parentX = node.pos.getc(true).x,
			childPos;
		
		for(var c in children){
			c = children[c];
			portCounter[c.data.$type]++;
			
			childPos = c.pos.getc(true);
			
			if( parentX - childPos.x > 0){
				c.pos.setc(childPos.x - delta, childPos.y);
			}
			else{
				c.pos.setc(childPos.x + delta, childPos.y);
			}
		}
		
		// update height
		var maxPorts = Math.max(portCounter.inputPort,
								portCounter.outputPort,
								portCounter.repositoryPort);
		var size = vertexLabelSize * 2;
		
		var height = size + Math.max( 2 * size, vertexLabelSize + maxPorts * size);
		
		if(textArray){
			height = Math.max(height, (textArray.length + 2) * size);
		}
		
		// update crossBox position
		delta = (height - node.data.$height) / 2;
		if(!data.$custom){
			c = children[0];
			childPos = c.pos.getc(true);
			c.pos.setc(childPos.x, childPos.y - delta);
		}
		
		node.data.$height = height;
	}
	
	/**
	* Updates all edge start- and end position offsets of a node.
	*
	* Parameters:
	*   node - the node of which the edges need to be updated
	*/
	function updateEdgeOffset(node){
		function updateOffset(object){
			var data, source, target;
			object.eachAdjacency(function(adja){
				
				data = adja.data;
				
				// is the first node the source node?
				if(adja.nodeFrom.id == data.$direction[0]){
					source = adja.nodeFrom;
					target = adja.nodeTo;
				}
				else{
					source = adja.nodeTo;
					target = adja.nodeFrom;
				}
				data.$offsetFrom =
					customEdgeMap[source.data.$type].getOffset(source, target, true, data);
				data.$offsetTo =
					customEdgeMap[target.data.$type].getOffset(source, target, false, data);
			});
  		}
		
		updateOffset(node);
		
		var children = node.data.$children;
		for(var c in children){
			updateOffset(children[c]);
		}
	}
	
	/**
	 *	Computes the position of a node that needs to snap to the grid
	 *
	 *	Parameters:
	 *	 node - the node that is snapped to the grid
	 *	 x - the unsnapped x-position
	 *	 y - the unsnapped y-position
	 *
	 *	Return:
	 *	 a x,y-object that defines the snapped position
	 */
	function snapToGrid(node, x, y){
		var data = node.data;
		
		if(grid.data.$snap){
			var gridDim = grid.data.$dim,
				left = (x - data.$width/2),
				top = (y - data.$height/2),
				right = (x + data.$width/2),
				bottom = (y + data.$height/2),
				
				offLeft = Math.abs(left % gridDim),
				offTop = Math.abs(top % gridDim),
				offRight = Math.abs(right % gridDim),
				offBottom = Math.abs(bottom % gridDim),
				threshold = gridDim/6;
		
			if(top > 0){
				offTop = gridDim - offTop;
			}
			if(bottom > 0){
				offBottom = gridDim - offBottom;
			}
			if(left > 0){
				offLeft = gridDim - offLeft;
			}
			if(right > 0){
				offRight = gridDim - offRight;
			}
			
			// snap to closest gridline
			if(offLeft < threshold) {
				x += offLeft;
			}
			if(offRight < threshold) {
				x += offRight;
			}
			else if(offLeft > gridDim - threshold){
				x -= gridDim-offLeft;
			}
			else if(offRight > gridDim - threshold){
				x -= gridDim-offRight;
			}
			
			if(offTop < threshold) {
				y += offTop;
			}
			
			else if(offTop > gridDim - threshold){
				y -= gridDim-offTop;
			}
			
			else if(offBottom < threshold) {
				y += offBottom;
			}
			
			else if(offBottom > gridDim - threshold){
				y -= gridDim-offBottom;
			}
		}
		
		return {'x' : x, 'y' : y};
	}
	
	/**
	 * Calls all listeners that are registered under the eventName.
	 *	
	 * Parameters:
	 *  eventName - see the addListener() function
	 *	arguments - an array of arguments. e.g. for mouseEvents it is [node, eventInfo, e]
	 *
	 * Return:
	 *	true if an editor action, such as creating an edge, is permitted
	 */
	function callListener(eventName, arguments){
		var lArr = listener[eventName];
		
		if(!lArr){
			return true;
		}
		var permitted = true;
		var testval;
		
		// iterate through all listener this event concerns
		for(var e in lArr){
			testval = lArr[e].apply(lArr[e], arguments);
			
			if(testval == false){
				permitted = false;
			}
		}
		return permitted;
	}
	
	/**
	* Highlights exactly one node or edge, and de-highlights the previously highlighted
	* node or edge.
	* 
	* Parameters:
	*  node - the node or edge object which will be highlighted
	*  noAnim - does not animate the highlight. Used by loadPositions()
	*/
	function setHighlight(node, noAnim){
	  
	  if(animation.busyCanvas){
		  return;
	  }
	  
	  var setter = 'current';
	  var isAnimated = animation.enabled;
	  if(isAnimated){
		  setter = 'end';
	  }
	  
		// animation parameters
		var trans = $jit.Trans.Quint.easeOut, 
			dur = animation.duration / 4;
		
		// clean up the old highlight
		if(hover){
			
			if(hover.nodeFrom){
				hover.setData('lineWidth', 1, setter);
				hover.setData('color', edgeColor, setter);
			}
			else{
				var type = hover.data.$type;
				if(markedNode != null && hover.id == markedNode.node.id){
					hover.setData('color', markedNode.strokeColor, setter);
					hover.setData('textColor', markedNode.strokeColor, setter);
				}
				else{
					hover.setData('color', nodeStrokeColor[type], setter);
					hover.setData('textColor', nodeTextColor[type], setter);
				}
				// decrease size of ports and cross button
				if(type == 'inputPort' || type == 'outputPort' || type == 'repositoryPort'){
					hover.setData('dim', vertexLabelSize*2, setter);
				}
				
			}
			hover = null;
		}
		
		// do we highlight something new?
		if(node){
			if(node.nodeFrom != undefined){
				node.setData('color', edgeColorFocus, setter);
				node.setData('lineWidth', 2, setter);
			}else{
				node.setData('color', nodeColorFocus, setter);
				node.setData('textColor', nodeColorFocus, setter);
				
				// increase size of ports and cross button
				var type = node.data.$type;
				if(type == 'inputPort' || type == 'outputPort' || type == 'repositoryPort'){
					node.setData('dim', vertexLabelSize*3, setter);
				}
			}
			hover = node;
		}else{
			// slower fade away transition
			trans = $jit.Trans.Quint.easeIn;
			dur *= 2;
		}
		
		if(noAnim){
			return;
		}
		
		// Animations overwrite each other. If we let this animation
		// execute while we delete an edge, the onComplete() function
		// of the edge animation will not be called
		if(isAnimated){
			fd.animate({  
					modes: ['edge-property:lineWidth:color',
							'node-property:color:dim'],
					transition: trans,  
					duration: dur,
					onComplete: function(){fd.plot();}
			});
		}else{
			fd.plot();
		}
		
	}
	
	/**
	 * Clones a non-custom node along with its ports if they exist. 
	 * All properties are copied, but no eges are attached to the cloned node.
	 * 
	 * Parameters:
	 *  xPosition - the horizontal position of the node
	 *  yPosition - the vertical position of the node
	 *	node - the node which is copied
	 *	newID - the id of the copy
	 *	forced - if true, no event will be called upon node creation
	 */
	function cloneOrdinaryNode(xPosition, yPosition, node, newID, forced){
		var repositoryPorts = new Array();
		var inputPorts = new Array();
		var outputPorts = new Array();
		
		var children = node.data.$children;
		var d, type;
		var portIDStart = node.id.length + 1;
		
		for(d in children){
			d = children[d];
			var port = {'name' : d.name};
			
			type = d.data.$type;
			switch(type){
				case 'repositoryPort':
					repositoryPorts.push(port);
					break;
				case 'inputPort':
					inputPorts.push(port);
					break;
				case 'outputPort':
					outputPorts.push(port);
					break;
			}
		}
		var nodeProps = {'name' : node.name};
		 
		var clone = 
			addNode(xPosition, yPosition, nodeProps, repositoryPorts, inputPorts, outputPorts, node.data.$type, forced);
		
		// copy node data
		var newData = new Object();
		var data = clone.data;
		for(d in data){
			if(d != "$children"){
				newData[d] = copyDataValue(data[d]);
			}
		}
		setNodeData(clone.id, newData);
		
		// copy port data
		var i = 0;
		var newChildren = clone.data.$children;
		for(d in children){
			
			type = children[d].data.$type;
			if(type != 'repositoryPort' && type != 'inputPort' && type != 'outputPort'){
				continue;
			}
			newData = copyDataValue(children[d].data);
			setNodeData(newChildren[i].id, newData);
			i++;
		}
	}
	
	/**
	 * Clones a custom node without its children. All properties are copied, 
	 * but no eges are attached to the cloned node.
	 * 
	 * Parameters:
	 *  xPosition - the horizontal position of the node
	 *  yPosition - the vertical position of the node
	 *	 node - the node which is copied
	 *	 newID - the id of the copy
	 *	 forced - if true, no event will be called upon node creation
	 */
	function cloneCustomNode(xPosition, yPosition, node, newID, forced){
		var newData = new Object();
		var data = node.data;
		var d;
		for(d in data){
			if(d != "$children"){
				newData[d] = copyDataValue(data[d]);
			}
		}
		addCustomNode(xPosition, yPosition, node.name, "", newData, forced);
	}
	
	/**
	 * Copies a value. If the value is an object or array, it is copied accordingly 
	 * instead of returning a pointer to the object.
	 * 
	 * Parameters:
	 * 	val - the value that needs to be copied
	 * 
	 * Return:
	 * 	a copy of the value
	 */
	function copyDataValue(val){
		// copy array
		if(val instanceof Array){
			var copiedVal = new Array(val.length);
			for(var i = 0, l = val.length; i < l; i++){
				copiedVal[i] = copyDataValue(val[i]);
			}
			return copiedVal;
		}
		
		// copy object
		else if(typeof(val) == 'object'){
			var copiedVal = new Object();
			var v;
			for(v in val){
				copiedVal[v] = copyDataValue(val[v]);
			}
			return copiedVal;
		}
		
		// copy everything else
		else{
			return val;
		}
	}
	
	
	///////////////////////
	// GRAPH CONSTRUCTOR //
	///////////////////////
	
	/**
	*	Initializes the Graph, enabling canvas navigation and tooltips.
	*	Also the graph is initialized with a dummy mouse node and
	*	what happens on various mouse events is determined.
	*/
	function initGraph(containerName){
	if(containerName && typeof(containerName) == 'string'){
		visContainer = containerName;
	}
		
	if(!document.getElementById(visContainer)){
		console.error("Could not load Graph into div '"+ visContainer + "'! No such container exists!");
		return false;
	}
	  
	// init json Array  
	var json = new Array();

	// define mouseNode
	json.push(
		{
		  "adjacencies": new Array(), 
		  "data": {
			"$type": "none"
		  }, 
		  "id": "#DUMMY_MOUSE_NODE"
		},
		
		// define gridNode
		{ 
		'data': {
			'$dim' : vertexLabelSize*4,
			'$type'	 : 'grid',
			'$color'  : '#AAAAFF',
			'$width' : document.getElementById(visContainer).clientWidth,
			'$height': document.getElementById(visContainer).clientHeight,
			'$alpha' : 0,
			'$snap'   : false
		},
		'id':	'#DUMMY_GRID_NODE'
		}
	);


	  
	  fd = new $jit.GraphFlow({
		width : document.getElementById(visContainer).clientWidth,
		height : document.getElementById(visContainer).clientHeight,
	  
		//id of the visualization container
		injectInto: visContainer,
		//Enable zooming and panning
		//with scrolling and DnD
		Navigation: {
		  enable: true,
		  //Enable panning events only if we're dragging the empty
		  //canvas (and not a node).
		  panning: 'avoid nodes',
		  zooming: 40 //zoom speed. higher is more sensible
		},
		// Change node and edge styles such as
		// color and width.
		// These properties are also set per node
		// with dollar prefixed data-properties in the
		// JSON structure.
		Node: {
		  overridable: true,
		  CanvasStyles: {  
			  lineWidth: 2  
			}  
		  
		},
		Edge: {
		  overridable: true,
		  color: edgeColor,
		  lineWidth: 1,
		  dim: vertexLabelSize,
		  type: 'flowArrow'
		},
		//Native canvas text styling  
		Label: {  
			type: 'HTML', //Native or HTML  
			size: 10,  
			style: 'bold'  
		}, 
		//Add Tips  
		  Tips: {  
			enable: true,  
			type: 'Native',
			onShow: function(tip, node) {  
			  var message = node.data.$tooltip;
			  
			  // do not show a tooltip if it is undefined
			  if(message == undefined){
				tip.style.visibility = "hidden";
			  }
			  else{
				tip.innerHTML = "<div class=\"tip-text\">" + message + "</div>";  
				tip.style.visibility = "visible";
			  }
			}  
		 },  
		 
		// Add node events
		Events: {
		  enable: true,
		  enableForEdges: true,
		  type: 'Native',
		  
		  /**
		   *	EVENT:	OnRightClick
		   */
		  onRightClick: function(node, eventInfo, e) {
			callListener("onRightClick", [node, eventInfo, e]);
		  },
		  
		  /**
		   *	EVENT:	OnDragCancel
		   */
		  onDragCancel: function(node, eventInfo, e) {
			callListener("onDragCancel", [node, eventInfo, e]);
		  },
		  
		  /**
		   *	EVENT:	OnMouseEnter
		   */
		  onMouseEnter: function(node, eventInfo, e) {
			if(node.id == mouseNode.id){
				return;
			}
			// ignore some highlights if we are dragging an edge
			if(selectedNode && 
				(node.data.$type == "nodeFamily" 
				|| node.data.$type == "crossBox" || node.nodeFrom)){
				return;
			}
			
			setHighlight(node);
			if(readOnly.move && node.data.$type == "nodeFamily"){
				setMouseCursor('move');
			}else if (readOnly.deleteCreate){
				setMouseCursor('pointer');
			}
			callListener("onMouseEnter", [node, eventInfo, e]);
		  },
		  
		  /**
		   *	EVENT:	OnMouseMove
		   */
		  onMouseMove: function(node, eventInfo, e) {
			var pos = eventInfo.getPos();

			// are we dragging an edge with the mouse ?
			if(selectedNode){
				var px = pos.x,
					py = pos.y;
				
				if(orthogonalEdges){
					var dx = navi.dragEdgeSourceX;
					var dy = navi.dragEdgeSourceY;
					if(Math.abs(px - dx) < Math.abs(py - dy)){
						px = dx;
					}else{
						py = dy;
					}
				}
				mouseNode.pos.setc(px, py);
				fd.plot();
			}
			
			callListener("onMouseMove", [node, eventInfo, e]);
		  },
		  
		  /**
		   *	EVENT:	OnMouseLeave
		   */
		  onMouseLeave: function(node, eventInfo, e) {
			setMouseCursor(false);
			setHighlight(false);
			callListener("onMouseLeave", [node, eventInfo, e]);
		  },
		  
		  /**
		   *	EVENT:	OnDragStart
		   *	Actually this Event fires whenever the mouse button is down
		   */
		  onDragStart: function(node, eventInfo, e) {
		  var mousePos = eventInfo.getPos();
		  
		  // dragging the canvas
			if(!node || node.id == mouseNode.id){
				
				navi.dragX = e.layerX;
				navi.dragY = e.layerY;
				callListener("onDragStart", [false, eventInfo, e]);
				return;
			}
			if(!node.data.$movable){
				callListener("onDragStart", [node, eventInfo, e]);
				return;
			}
			
			//dragging a node
			if(!selectedNode){
				// memorize where we drag the node instead on centering
				// it to the mouse pointer
				var	nodePos = node.pos.getc(true);
				
				// the start position of the node
				navi.dragX = nodePos.x;
				navi.dragY = nodePos.y;
				
				// the offset between the center of the node and the actual position
				// where we grabbed the node
				navi.grabOffsetX = nodePos.x - mousePos.x;
				navi.grabOffsetY = nodePos.y - mousePos.y;
				
				// is dragging a node
				navi.isDragging = true;
			}
			callListener("onDragStart", [node, eventInfo, e]);
		  },
		  
		  /**
		   *	EVENT:	OnDragMove
		   */
		  onDragMove: function(node, eventInfo, e) {
			if(!node || !node.data.$movable || selectedNode || !readOnly.move){
					return;
			}
			
			// move node
			var pos = eventInfo.getPos(),
				x = pos.x + navi.grabOffsetX,
				y = pos.y + navi.grabOffsetY;
			//TODO: if this doesnt work, comment in: moveNode(node, x, y);
			
			// detach bendPoints on moving to far
			if(navi.dragX != null){
				var deltaX = Math.abs(x - navi.dragX),
					deltaY = Math.abs(y - navi.dragY);
				if(deltaX > 24 || deltaY > 24){
					iterateEdges(node, 
						function(adja){
							delete adja.data.$bendPoints;
					});
					navi.dragX = null;
					navi.dragY = null;
					callListener("onDragFar", [node, eventInfo, e]);
				}
			}
			
			if(callListener("onDragMove", [node, eventInfo, e]) != false){
				moveNode(node, x, y);
			};
		  },
		  
		  /**
		   *	EVENT:	OnDragEnd
		   */
		  onDragEnd: function(node, eventInfo, e) {
			
			callListener("onDragEnd", [node, eventInfo, e]);
			
			if(node && !node.nodeFrom){
				// recalculate the edge offset of the node and its children
				updateEdgeOffset(node);
			}
			
			fd.plot();
		  },
		  
		  /**
		   *	EVENT:	OnTouchMove
		   */
		  onTouchMove: function(node, eventInfo, e) {
			$jit.util.event.stop(e); //stop default touchmove event
			this.onDragMove(node, eventInfo, e);
		  },
		  
		  /**
		   *	EVENT:	OnClick
		   */
		  onClick: function(node, eventInfo, e){
		  
			  if(e.button == 1){
				  callListener("onMiddleClick", [node, eventInfo, e]);
				  return;
			  }
			  
			  var isNotDragging = true;
			  var wasDraggingEdge = navi.isDraggingEdge && selectedNode;
			  
			  // set mouse position (required for dragging edges)
			  var pos = eventInfo.getPos();
			  mouseNode.pos.setc(pos.x, pos.y);
			  
			  
			  // finished dragging the screen
			  if(!node){
				var deltaX = e.layerX - navi.dragX,
					deltaY = e.layerY - navi.dragY;
				navi.centerX -= deltaX;
				navi.centerY -= deltaY;
				
				isNotDragging = (Math.abs(deltaX) < 5 && Math.abs(deltaY) < 5);
				
				// remove selection and mouse-edge upon clicking anywhere
				if(isNotDragging && selectedNode){
				  connectEdge(null);
				}
				fd.plot();
			  }
			  else if(!node.nodeFrom && readOnly.deleteCreate){
				
				var incomingEdge = false;
				switch(node.data.$type){
					case "crossBox":
						removeNode(node.data.$family, true);
						break;
						
					case "nodeFamily":
						break;
						
					case "inputPort":
						incomingEdge = true;
					case "outputPort":
					case "repositoryPort":
						connectEdge(node.id, incomingEdge);
						break;
				}
				
			  }
			  else if(node.nodeFrom && readOnly.deleteCreate){
				// no selection yet
				if(selectedNode == null){
					var source = node.data.$direction[0],
						edgeData = node.data,
						label = node.data.$label;
					
					// remove selectedEdge
					removeEdge(source, node.data.$direction[1]);
					
					// create dangling edge
					connectEdge(source, false, label, edgeData);
					
					fd.plot();
					
					if(isNotDragging){
						callListener("onClick", [node, eventInfo, e]);
					}
					return;
				}
			  }
				  
			  if(isNotDragging){
				  callListener("onClick", [node, eventInfo, e]);
			  }
			  navi.isDragging = false;
			  
			  if(wasDraggingEdge){
				  navi.isDraggingEdge = false;
			  }
		  },
		  
		  /**
		   * 	EVENT: OnMouseWheel
		   */
		  onMouseWheel: function(e, delta) {
			  // update whether labels should be displayed
			  fd.canvas.showLabels = (fd.canvas.scaleOffsetX > fd.canvas.labelThreshold);
			  fd.plot();
		  }
		},
		
		
		//Number of iterations for the FD algorithm
		iterations: 0,
		//Edge length
		levelDistance: 0
	  });
	  
	  // load JSON data.
	  fd.loadJSON(json, 0);
	  
	  // set pointers to grid and mouse node
	  mouseNode = fd.graph.getNode("#DUMMY_MOUSE_NODE");
	  grid = fd.graph.getNode("#DUMMY_GRID_NODE");
	  
	  // set zoom threshold for displaying text
	  fd.canvas.labelThreshold = 1/vertexLabelSize;
	  fd.canvas.showLabels = true;
	  
	  console.log(fd.canvas);
	  console.log(fd.graph);
	}
	
	// initialize graph
	initGraph(containerName);
	return this;

}