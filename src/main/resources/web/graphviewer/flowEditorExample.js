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

///////////////////////////////
//	Examplary GraphFlow Data //
///////////////////////////////

function init(){

	/**
		Constructor
	 */
		// if no name is specified, the graph is drawn into a container with the id 'infovis'
		var divContainerName = "myGraphCanvas";
		var graph = GraphFlow(divContainerName);

	/**
		Read-Only-Mode
			Save the graph from manipulation via mouse drags and clicks.
	 */
	{
		// if false, then
		//	1. the upper-right delete-button on nodes becomes invisible and unclickable
		//  2. edges cannot be replaced via mouse clicks
		//  3. no edges are created if ports are clicked
		var freeMouseManipulation = false;
		
		// if false, nodes cannot be moved via mouse drag
		var freeNodeDragging = false;
		
		// setting both parameters to false completely restricts the user from manipulating
		// nodes and edges
		graph.setReadOnly(freeMouseManipulation, freeNodeDragging);	
	}
	
	/**
		Graph Animations
			If enabled, animates node/edge highlighting, loadPositions(), loadGraph(), markNode(), scaleToFit(),
			and setGridVisible().
	 */
	{
		// animation duration modifier in milliseconds
		// setGridVisible(), markNode() are animated in half that duration
		// node / edge highlighting is animated in a quarter of that duration
		var animDuration = 1000;
		
		// if false, no animations are shown, instead everything changes instantly,
		// which improves performance
		var animEnabled = true;
		 
		graph.setAnimation(animEnabled, animDuration)	
	}
		
	/**
		Setting the Mouse Cursor
			The mouse cursor changes automatically if the mouse enters
			a node or edge, and when it leaves them.
	*/
	{
		// this parameter can be left out to restore the default pointer. Viable names are:
		//   crosshair, e-resize, help, move, n-resize, ne-resize, nw-resize, pointer, progress, 
		//   s-resize, se-resize, sw-resize, text, w-resize, wait, default
		var cursorName = "crosshair";
		
		// this won't help much, because the curser will be overriden once it enters a node or edge
		graph.setMouseCursor(newCursor):
		
		// let's override the default pointer behavior by registering some event listeners
		var mouseOverNodeCursor = "crosshair";
		var mouseOverEdgeCursor = "help";
		var mouseOverBackgroundCursor = "progress";
		
		// this function will be called every time the mouse leaves a node or edge
		graph.addListener("onMouseLeave", 
			function(object){
				graph.setMouseCursor(mouseOverBackgroundCursor):
			}
		);
		
		// this function will be called every time the mouse enters a node or edge
		graph.addListener("onMouseEnter", 
			function(object){
				// this is a way to check if the object is an edge
				// only edges posses the property 'nodeFrom'
				var isEdge = object.nodeFrom;
				
				if(isEdge){
					graph.setMouseCursor(mouseOverEdgeCursor):
				}
				// is Node
				else{
					graph.setMouseCursor(mouseOverNodeCursor):
				}
			}
		);
	}
	
	/**	
		Registering Event Listeners
			Event listeners are functions that are called automatically,
			every time a certain event happens. One must pass the name 
			of the event and a function as parameters.
			
			(see "Setting the Mouse Cursor" for two examples)
	*/
	{
		// this function pops up an alert box that tells what was clicked
		var mouseEventFunction =
			function(object, eventInfo, e){
				
				// object validates to 'false' if nothing was clicked
				if(!object){
					alert("You clicked nothing.");
					return;
				}
				
				var isEdge = object.nodeFrom;
				if(isEdge){
					alert("You clicked on an edge between '" + object.nodeFrom.id + "' and '" + object.nodeTo.id + "'.");
					
					// note that object.nodeFrom and object.nodeTo are not necessarily the source and target node in the same
					// direction as the edge arrow. To get the source and target node, you can do the following instead:
					var dirArray = object.data.$direction;
					var sourceNode = graph.getNode(dirArray[0]);
					var targetNode = graph.getNode(dirArray[1]);
					alert("The edge originates in '" + sourceNode.id + "' and ends in'" + targetNode.id + "'.");
					
				}else{
					alert("You clicked on a node with type '" + object.data.$type + "'.");
				}
			};
		graph.addRightClickListener(mouseEventFunction);
		
		// Graph Events: onError, onCreateEdge, onCreateNode, onRemoveEdge,	onRemoveNode, autoLayout
		graph.addRemoveNodeListener( 
			function(node){
				alert("Bye, bye " + node.id + "! You will be missed!");
			}
		);
		
		// by default, the onCreateEdge event will even be called if you click a port and get a dangling edge
		// the if-condition inside the function makes sure that a dangling edge is already present and
		// thus will only be called if a complete edge is created
		graph.addCreateEdgeListener( 
			function(sourceNode, targetNode, sourcePort, targetPort){
				if(graph.isEdgeDragged()){
					alert("A new edge is born! " + sourceNode.id +" and " + targetNode.id + " are now in a relationship!");
				}
			}
		);
		
		graph.addKeyDownListener( function(keyCode){alert("Key with code '"+ keyCode +"' was pressed!");});
		graph.addKeyUpListener( function(keyCode){alert("Key with code '"+ keyCode +"' was released!");});
		
		// this function creates a predefined "onCreateEdge" listener that restricts edge placement
		graph.addEdgeConstraints();
	}
	
	/**
		Graph Dimensions and Text
			A short description on how graph elements are scaled.
	*/
	{
		// this function returns the size modifier of the graph
		// it is used to scale text, nodes and ports
		var dim = graph.getSizeModifier();
		
		// typical text height
		var textHeight = 0.83 * dim;
			
		// this function can measure the width of a text, which can be useful if
		// you define a custom node with text and need to align the text or adapt the node size
		var text = "This text will be measured!";
		
		// a complete font is preceded by its size in pixels
		var fontName = "Lucida Console";
		var measuredFont = textHeight + "px " + fontName;
		
		var textWidth = graph.getTextWidth(text, measuredFont);
	}
	
	/**
		Screen Bounds
			The canvas is virtually infinite, it can be useful to have access to the current
			position of the view.
	*/
	{
		// this function should be called whenever the container of the GraphFlow changes in size
		// it resizes the canvas to fit the new container size
		graph.updateCanvasSize();
		
		// this function returns the bounds of all nodes and edges of the graph
		// it can be used to measure the size the graph uses up on the canvas
		var gBounds = getGraphBounds();
		var graphWidth = gBounds.right - gBounds.left,
			graphHeight = gBounds.bottom - gBounds.top;
		var graphCenterX = gBounds.x,
			graphCenterY = gBounds.y;
		
		alert("The graph size is " + graphWidth + " x " + graphHeight + ". It is centered at " + graphCenterX + ", " + graphCenterY + ".");
		
		// this function returns the coordinates of the screen center in canvas coordinates
		// this can be used to create nodes in the center of the screen
		var sc = graph.getScreenCenter();
		var xCenter = sc.x;
		var yCenter = sc.y;
		
		// this function returns the coordinates of the borders of the screen
		var bounds = getScreenBounds();
		alert("You are currently viewing a " + (bounds.right - bounds.left) + " x " + (bounds.bottom - bounds.top) + " area of the canvas!");
	}
	
	/**
		Zooming
	*/
	{
		// zoom out out if factor is between 0 and 1
		var zoomOutFactor = 0.75;
		graph.zoom(zoomOutFactor);
		
		// zoom in if it is greater than 1
		var zoomInFactor = 1.25;
		graph.zoom(zoomInFactor);
		
		// this function moves the canvas and zooms in to show all nodes and edges
		graph.scaleToFit();
	}
	
	/**
		Grid Options
			A grid can be shown, customized and used for aligning nodes.
	*/
	{
		// enable/disable visibility
		var visState = true;
		graph.setGridVisible(visState);

		// change graph colors
		var gridColor = "#FF0088";
		gridColor = "red";
		graph.setGridColor(gridColor);

		// resize grid
		var newSize = graph.getSizeModifier();
		graph.setGridSize(newSize);

		// enable/disable nodes to snap to grid lines
		var snapState = true;
		graph.setGridSnap(snapState);
	}
	
	/**
		Node Creation and Deletion
	*/
	{
		// position of the new node
		var nodeX = 0, 
			nodeY = 0;
		
		// a node may have a displayed text and a tooltip
		var nodeDef = { 
			'name' 	  : "This text is inside the nodes' body.\n It is purely optional!",
			'font'	  : "Comic Sans", // such font! wow! much amaze!
			'tooltip' : "Optional tooltip!" 
		};
		
		// all nodes that have the same iconID, display the same icon (see Icons and Colors examples)
		var iconID = "iconID_1";
		
		// if true, the "onCreateNode" event will not be called on creation
		var triggerEvent = false;
		
		// a definition for each port is optional. Alternatively, you can provide an integer that defines the number of created ports.
		var portDef = {
			'symbol'  : "P",
			'tooltip' : "Optional port tooltip!" 
		};
		
		// ports are divided into three categories:
		//	 input ports are positioned on the left border of a node
		//   output ports are positioned on the right border of a node
		//   repository ports are positioned above output ports
		// an array of port definitions, an integer, or null is required for each type of port
		var inputPorts = [portDef];
		
		var outputPorts = new Array();
		for(int i = 2, l = 5; i < l; i++){
		
			var opDef = {'id' : "portID_" + i};
			outputPorts.push(opDef);
		}
		var repositoryPorts = 2;
		
		// add the node to the graph
		var myNode = graph.addNode(nodeX, nodeY, nodeDef, repositoryPorts, inputPorts, outputPorts, iconID, triggerEvent);
		
		// define a simple right click event that creates a node
		var nodeCounter = 2;
		graph.addListener("onRightClick",
			function(object, eventInfo, e){
				var clickPos = eventInfo.getPos();
				var nd = {'name' : 'node ' + nodeCounter);
				
				var inputPorts = 4;
				var outputPorts = 2;
				graph.addNode(clickPos.x, clickPos.y, nd, null, inputPorts, outputPorts);
				
				nodeCounter++;
		});
		
		// you can remove nodes like this
		var deletePorts = true;
		graph.removeNode(myNode, deletePorts, triggerEvent);
		
		// clear the entire graph with all nodes and edges
		
		// if false, no onRemoveNode or onRemoveEdge events are called
		var triggerEvents = false;
		graph.clear(triggerEvents);
	}
	
	/**
		Node Data
			Existing nodes can be changed as seen fit. You can also assign
			additional variables to nodes.
			This example uses the node defined in the previous section
	*/
	{
		// nodes have all sorts of properties, in order to view them, open your browsers console.
		// For firefox and chrome, use the FIREBUG extension. Now, create a node like in the preceding
		// example and type the following command
		var nodeID = "uniqueID_1";
		var myNode = graph.getNode(nodeID);
		console.log(myNode);
		
		// a representation of the node should show up in your browsers console. The 'data' property of
		// the node shows what can be changed, e.g. the font and alpha
		// you can add your own entries as well
		var properties = {
			'$font' : "Arial Black",
			'$alpha' : 0.5,
			'$myVal' : "awesome",
			'$myVal2' : 42
		};
		
		// this function should be used to change anything about a node, because
		// intern representation of fonts and names is different from what you define
		graph.setNodeData(nodeID, properties);
		
		// you can however access data directly
		var myVal = myNode.data.$myVal;
		
		// data entries can also be removed 
		graph.removeNodeData(nodeID, "$myVal2");

		// node positions should be changed with the following function
		var x = 0, y = 0;
		graph.moveNode(myNode, x, y);
	}
	
	/**
		Selecting Nodes
	*/
	{
		// a single node is best selected by its unique ID
		var nodeID = myNode.id;
		var myNode = graph.getNode(nodeID);
		// if it is buried behind other nodes, it can be moved to the top layer
		graph.setNodeToTop(myNode);
		
		// you can iterate through all nodes and call a function for each node
		graph.iterateAllNodes(
			function(node){
				alert(node.id);
			});
		
		// you can also filter nodes by matching data and get an array of nodes in return
		// the following function returns an array of all nodes whose data.$myVal property has the value "awesome"
		var awesomeNodes = graph.getNodesByDate("$myVal", "awesome");
		
		// aside from the usual highlighting, one can mark a single node
		var markStrokeColor = "white",
			 markFillColor = "black";
		graph.markNode(myNode, markStrokeColor, markFillColor);
		
		// to use the default mark colors, simply call
		graph.markNode(myNode);
	
		// the marked node can be called
		graph.getMarkedNode();
		
		// to unmark a node, call the markNode function on an argument that evaluates to false, like null, undefined, false
		// or just leave the field empty
		graph.markNode(false);
		graph.markNode(null);
		graph.markNode();
	}
	
	/**
		Edges
			Edges are created automatically by clicking on ports. However, they can
			also be created and modified programatically.
	*/
	{
		// this function has the same behavior as clicking on a port:
		// it creates a dangling edge and connects it, if called again
		var edgeLabel = "This is\nedge text";
		var edgeData = {'$color' : "red"};
		// if true, the edge arrow points towards the port
		var isIncoming = false;
		// only the portID is mandatory for this function
		var portID = "uniqueID_1.portID_1";
		graph.connectEdge(portID, isIncoming, edgeLabel, edgeData);
		
		// in order to create a complete edge, this function is used:
		var portTargetID = "uniqueID_2.portID_1";
		var triggerEvent = false;
		graph.addEdge(portID, portTargetID, edgeLabel, edgeData, triggerEvent);
		
		// this function removes an existing edge
		graph.removeEdge(portID, portTargetID, triggerEvent);
		
		// check if a dangling edge exists with this function (see "Registering Event Listeners")
		var isEdgeDangling = graph.isEdgeDragged();
		
		// edges contain a data object that functions the same as that of a node. However, to select
		// an edge, the source and target port ids must be known
		var properties = {
			"$color" : "#FF0088"
		}
		graph.setEdgeData(portID, portTargetID, properties);

		// to manually set bend points, an array of x-y-objects needs to be defined first
		var bendPoints = new Array();
		bendPoints.push({'x' : 20, 'y' : 20});
		bendPoints.push({'x' : 20, 'y' : 10}); 
		bendPoints.push({'x' : 10, 'y' : 10});
		graph.setBendPoints(portID, portTargetID, bendPoints);
		
		// to call a function on each edge, connected to a node, this function can be used:
		var nodeID = "uniqueID_1";
		var myNode = graph.getNode(nodeID);
		var edgeFun = function(adjacency){
			var nodeFrom = adjacency.nodeFrom;
			var nodeTo = adjacency.nodeTo;
			// adjacency.nodeFrom does not have to be the source node! the $direction data is an
			// array with two fields that contain the id of the source and target node respectively
			if(nodeFrom.id != adjacency.data.$direction[0]){
				nodeFrom = nodeTo;
				nodeTo = adjacency.nodeFrom;
			}
			alert(nodeFrom.id + " -> " + nodeTo.id);
		};
		graph.iterateEdges(myNode, edgeFun);
	}
	
	/**
		Loading / Saving
	*/
	{
		// the json tree structure of an entire graph can be converted to a string
		var saveString = graph.saveGraph();
		
		// this string can then be loaded. This procedure creates nodes and edges and thus can
		// trigger onCreateNode and onCreateEdge events. This feature can be prevented with a boolean flag
		var triggerEvents = false;
		graph.loadGraph(saveString, triggerEvents);	
	}
	
	/**
		Layouting
	*/
	{
		// if the positions of the graph elements shall be saved, the following function suffices
		var positionString = graph.savePositions();
		
		// positions can be loaded like this. This can serve as a save point to come back to if the layout
		// is changed in a bad way
		graph.loadPositions(positionString);
		
		// this function by itself does not layout the graph. instead it prepares two strings with necessary layout
		// information, that serve as parameters for the KIELER autolayouting algorithm, provided by the GraphLayouter.jar
		// additionally, after completion, this function will trigger an autoLayout event, that carries both strings of the
		// returned object
		var layoutArr = graph.autoLayout();
		var nodeLayoutString = layoutArr[0];
		var edgeLayoutString = layoutArr[1];
		// the KIELER autolayouting algorithm returns a positionString that can be used by loadPositions()
		
		// this function is work in progress. Its purpose is to only layout a part of the graph
		var nodeArray = new Array();
		nodeArray.push(graph.getNode("uniqueID_1"));
		nodeArray.push(graph.getNode("uniqueID_2"));
		graph.partialCustomAutoLayout(nodeArray);
	}
	

	/**
		Colors / Styles
	*/
	{
		// iconIDs are not only used to share icons between nodes, but styles as well
		var iconID = "iconID_1";
		
		// this extreme example shall serve to show what each parameter means, if it is not clear from the names
		var fillColor = 	"#000000",
			strokeColor = 	"#AA0000",
			edgeColor = 	"#FF9900",
			hoverColor = 	"#00AA00",
			textColor = 	"#FFFFFF",
			font =			"Comic Sans";
		// change the colors and font of each node with the specified iconID
		graph.setNodeStyle(iconID, fillColor, strokeColor, edgeColor, hoverColor, textColor, font);
		// to change the style of all nodes, simply set the iconID to false
		graph.setNodeStyle(false, fillColor, strokeColor, edgeColor, hoverColor, textColor, font);
		
		// this function changes the default colors of the markNode() function (see Selecting Nodes example)
		graph.setMarkedNodeStyle(fillColor, strokeColor);
		
		// you can change the entire graph style according to a CSS rule
		
		// if false, the color attribute is used
		var useBorderColorForText = true; 
		// the class must exist in an included css file
		var cssClassName = "myCssClass";
		graph.setNodeStyleCSS(cssClassName, useBorderColorForText);

		// to only use specific css attributes for specific colors, use this function
		var cssAttribute = "background";
		// this parameter determines what part of the graph should be changed. Possible values are:
		// font, textColor, strokeColor, fillColor, edgeColor, focusColor, markFillColor, markStrokeColor
		var style = "focusColor";
		graph.setExplicitNodeStyleCSS(cssClassName, cssAttribute, style);
	}
	
	/**
		Icons
	*/
	{
		// all nodes that have the same iconID, display the same icon and share a color scheme
		var iconID = "iconID_1";
		// the icon path must be a valid url
		var path = "http://images1.wikia.nocookie.net/__cb20130506023846/unanything/images/5/56/Rick_Astley.jpg";
		// if true, nodes are resized to fit the icon even before the icon was successfully loaded
		var resizeImmediately = true;
		
		graph.setNodeIcon(iconID, path, resizeImmediately);	

		// this function returns the path that is assigned to an iconID
		var retPath = graph.getIconURL(iconID);

		// this function returns an array of all existing iconIDs
		var iconIDs = graph.getIconTypes();
		
		// this function reads a string, generated by saveGraph(see Loading / Saving example) and returns an object
		// that contains all nodeIDs and their respective paths as two arrays
		var saveString = graph.saveGraph();
		var iconIDTable = graph.getNodeIconsFromJSON(saveString);
		
		var iconIDArray = iconIDTable.types;
		var iconPathArray = iconIDTable.urls;
		
		for(var i = 0, l = iconIDArray.length; i < l; i++){
			alert("IconID: " + iconIDArray[i] + ", URL: " + iconPathArray[i]);
		}
	}
	
	/**
		Custom Nodes
			Aside from the standard nodes with ports, it is possible to define custom shapes and
			custom behaviours for nodes and edges. This is an advanced feature that requires
			knowledge of HTML-Canvas operations and of the JIT-Toolkit. I suggest to take a good
			look at the plugin-BPMN.js.
	*/
	{
		// the custom node requires a unique nodeType. The draw and mouse collision functions are
		// defined for that type
		var nodeType = "myCustomNodeType";
	
		// define the nodeType
		var myCustomNodeBluePrint = {
			'myCustomNodeType' : {
			// this function determines how the node is drawn
				'render' : function(node, canvas) {  
					var data = node.data,
						pos = node.pos.getc(true),
						x = pos.x,
						y = pos.y,
						radius =  data.$dim,
						lineWidth = radius / 10;  
					
					var ctx = canvas.getCtx();
					ctx.save();
					
					// fill circle
					ctx.beginPath();
					ctx.fillStyle = data.$fillColor;
					ctx.arc(x, y, radius, 0, CircleHelper.fullCirc, true);
					ctx.fill(); 
					
					//draw stroke(s)
					ctx.beginPath();
					ctx.fillStyle = data.$color;
					ctx.lineWidth = lineWidth;
									
					// display node name
					var label = node.name;
					if(canvas.showLabels && label){
						SymbolHelper.drawLabel(label, radius, ctx, data.$color, data.$font, x, y + 1.5 * radius);
					}
					ctx.restore();
				},
		  
				// this function determines when the node shall be counted as hovered by the mouse
				'contains' : function(node, pos){
					if(node.data.$unselectable){
					  return false;
					}
					var npos = node.pos.getc(true), 
					  radius = node.data.$dim;
					// return true if mouse is within certain distance of the node
					return Math.abs(pos.x - npos.x) < radius && Math.abs(pos.y - npos.y) < radius;
				}
			}
		};

		// define fill and stroke colors, used by the canvas context
		var fillColor = "white",
			strokeColor = "black";
			
		// by default, edges originate and point towards the center of a node, by defining an
		// offset function, it is possible to move these anchor points relative to the node center
		
		// this simple offset function aligns the edge to the right middle of the node if it originates
		// there, and on the left if the node is the target
		var edgeOffsetFun = function(sourcePort, targetPort, isSource, edgeData){
			if(isSource){
				return {'x' : sourcePort.data.$width/2, 'y' : 0};
			} 
			else{
				return {'x' : -sourcePort.data.$width/2, 'y' : 0};
			}
		};
		
		// once the necessary data for the custom node is defined, register it once
		$jit.GraphFlow.Plot.NodeTypes.implement(myCustomNodeBluePrint);
		graph.registerCustomNode(nodeType, fillColor, strokeColor, edgeOffsetFun);
		
		
		// once the nodeType is defined, custom nodes can easily be created:
		
		// if true, triggers an onCreateNode event
		var triggerEvent = false;
		// position of the custom node
		var x = 0;
		var y = 0;
		var name = "This text is by default invisible. You must define what to do with it!";
		
		// data is an object that every node can have
		var data = {
			'&tooltip' : "Custom tooltips work!" 
		};
		var myCNode = graph.addCustomNode(x, y, name, nodeType, data, triggerEvent);
		
	}

	/**
		Testing for Type Correctness
			These functions were once used for checking parameters.
			However the graph framework became too complex and these functions were left out
			to boost performance. All functions trigger an onError event if some criteria is not met.
	*/
	{
		// this parameter serves to locate the error by signalling which function caused it
		var functionName = "testFunc()";
		
		// check if a variable is of a certain type
		var someNumber = 42;
		var type = "string";
		var isString = graph.validArg(functionName, someNumber, type);
		alert("Is '" + someNumber +"' of type '" + type + "'? " + isString);
		
		// to check if a variable is a #RRGGBB color string, this function can be used
		var someColor = "#FF00DD";
		var isColor = graph.validArgColor(functionName, someColor);
		alert("Is '" + someColor +"' a well-formed color string?" + isColor);
		
		// to check if a variable is a node object, use this function
		var someNode = graph.getNode("uniqueID_1");
		var isNode = graph.validArgNode(functionName, someNode);
		alert("Is '" + someNode +"' a node?" + isNode);
		
		// to check if a node id is well-formed and available, use this function
		var someNodeID = "uniqueID_1";
		var isIDFree = graph.validArgID(callFunction, id);
		alert("Is '" + someNodeID +"' a valid and available ID?" + isIDFree);
		
		// to register an error event, use this function (see Registering Event Listeners Example)
		graph.addListener('onError', function(message){alert(message);});
	}
}