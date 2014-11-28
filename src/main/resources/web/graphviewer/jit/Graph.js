
///////////////////////////////
//	Exemplary GraphFlow Data //
///////////////////////////////
var graph;


/**
 * This function is called once per browser refresh to initialize the graph and all its components.
 */
function paintGraph() {
	
	// construct GraphFlow object and its gwtExtension member
	graph = GraphFlow();
	graph.gwtExt = {};
	
	//TODO
	importBPMN(graph);
	
	// adds a listener that removes invalid Edges
	//graph.addEdgeConstraints();
	
	/**
	 *	Print errors to console
	 */
	graph.addListener('onError', function(message){console.log(message);});
	
	/**
	 *	Add newly created nodes to the GWT node list,
	 *	also signals GWT that the Graph has changed
	 */
	graph.addListener("onCreateNode", function(node){
		graph.gwtExt.addTreeNode(node.id, node.name);
		graph.gwtExt.setChanged();
	});
	
	/**
	 *	Remove deleted nodes and their children from the GWT node list,
	 *	also signals GWT that the Graph has changed
	 */
	graph.addListener("onRemoveNode", function(node){
		var children = node.data.$children;
		for(var c in children){
			graph.gwtExt.removeTreeNode(children[c].id);
		}
		graph.gwtExt.removeTreeNode(node.id);
		graph.gwtExt.selectTreeNode("#null");
		graph.gwtExt.setChanged();
	});
	
	/**
	 *	Signals GWT that the Graph has changed when edges are created or removed
	 */
	graph.addListener("onCreateEdge", function(){graph.gwtExt.setChanged();});
	graph.addListener("onRemoveEdge", function(){graph.gwtExt.setChanged();});
	
	
	/**
	 *	RIGHT CLICK
	 *  - Remove ghostNode
	 *	- Add bendPoint if a dangling edge exists
	 *	- Shows properties if a node is clicked
	 */
	graph.addListener("onRightClick", 	function(node, info, e){
		var pos = info.getPos();
		
		if(ghostNode){
			graph.removeNode(ghostNode, true);
			ghostNode = false;
			return;
		}
		
		if(graph.isEdgeDragged()){
			var mouse = graph.getNode("#DUMMY_MOUSE_NODE");
			var adja = mouse.adjacencies;
			
			// add bendPoint on mouse pos
			for(var edge in adja){
				edge = adja[edge];
				var bendPoints = edge.data.$bendPoints;
				var newBP = {'x' : mouse.pos.x, 'y' : mouse.pos.y};
				var dir = edge.data.$direction;
				
				if(!bendPoints){
					bendPoints = [newBP];
				}else if(dir[0] == mouse.id){
					bendPoints.unshift(newBP);
				}else{
					bendPoints.push(newBP);
				}
				graph.setBendPoints(dir[0], dir[1], bendPoints);
				
				break;
			}
		}else{
			graph.gwtExt.hideRightClickMenu();
			
			// select only node families and
			if(node && !node.nodeFrom){
				graph.gwtExt.selectTreeNode(node.id);
			}
			else{
				graph.gwtExt.selectTreeNode("#null");
			}
			if(!node){
				graph.gwtExt.showRightClickMenu(node, e.clientX, e.clientY);
			}
		}
	});
	
	// KEY PRESSES
	
	/**
	 *	GWT interface to enable/disable key presses
	 */
	var keyPressEnabled = false;
	graph.gwtExt.setKeyPressEnabled = function(state){
		keyPressEnabled = state;
	};
	
	/**
	 *	Disable orthogonal edges if shift is released
	 */
	graph.addListener("onKeyUp", 	function(keyCode){
		if(!keyPressEnabled){
			return;
		}
		// "SHIFT": disable orthogonal edges
		if(keyCode == 16){
			graph.setOrthogonalEdges(false);
		}
	});
	
	/**
	 *	Key Presses
	 */
	graph.addListener("onKeyDown", 	function(keyCode){
		
		if(!keyPressEnabled){
			return;
		}
		var mNode = graph.getMarkedNode();
		
		// "SHIFT": force orthogonal edges
		if(keyCode == 16){
			graph.setOrthogonalEdges(true);
		}
		// "DELETE": remove node
		if(keyCode == 46){
			
			if(ghostNode){
				graph.gwtExt.removeNode(ghostNode.id);
				//graph.removeNode(ghostNode, true);
				ghostNode = false;
			}
			else if(mNode){
				//graph.removeNode(mNode, true);
				graph.gwtExt.removeNode(mNode.id);
				return;
			}
		}
		// "ESCAPE": abort edge drag, hide Menu
		if(keyCode == 27){
			if(graph.isEdgeDragged()){
				graph.connectEdge(null);
			}
			else if(ghostNode){
				graph.removeNode(ghostNode, true);
				ghostNode = false;
			}
			graph.gwtExt.hideRightClickMenu();
			return;
		}
		
		// "PLUS": zoom in
		if(keyCode == 107 || keyCode == 187){
			graph.gwtExt.zoomIn();
			
			return;
		}
		// "MINUS": zoom out
		if(keyCode == 109 || keyCode == 189){
			graph.gwtExt.zoomOut();
			return;
		}
		// "G": toggle Grid
		if(keyCode == 71){
			
		}
		// "F": zoom to fit
		if(keyCode == 70){
			graph.scaleToFit();
			return;
		}
			
	});
	
	/**
	 *	Calculates positions for a boundary event, which clings to
	 *	its parent's borders.
	 *
	 *	Parameters:
	 *		x - the x-coordinate where the node is moved
	 *		y - the y-coordinate where the node is moved
	 *		bParent - the subProcess to which the node is attached
	 *
	 *	Returns:
	 *		an object containing the position as x, y, and a boolean flag detached,
	 *		which becomes true if the node is moved too far away from its parent
	 */
	function computeBoundaryPos(x, y, bParent){
		var cutOffDist = 24;
		var bx = bParent.pos.x,
			by = bParent.pos.y;
		var bWidth = bParent.data.$width / 2,
			bHeight = bParent.data.$height / 2;
		var bLeft = bx - bWidth,
			bTop = by - bHeight,
			bRight = bx + bWidth,
			bBottom = by + bHeight;
			
		// detach event if too far away
		if(x < bLeft - cutOffDist || x > bRight + cutOffDist || y < bTop - cutOffDist || y > bBottom + cutOffDist){
			return { 
				'x' : x,
				'y' : y,
				'detached' : true};
		}
		// move event along the border of the boundary parent
		var nodeX, nodeY;
		if(y > by){
			if(x > bx){
				if(Math.abs(x - bRight) < Math.abs(y - bBottom)){
					nodeX = bRight;
					nodeY = (y > bBottom) ? bBottom : y;
				}
				else{
					nodeX = (x > bRight) ? bRight : x;
					nodeY = bBottom;
				}
			}
			else{
				if(Math.abs(x - bLeft) < Math.abs(y - bBottom)){
					nodeX = bLeft;
					nodeY = (y > bBottom) ? bBottom : y;
				}
				else{
					nodeX = (x < bLeft) ? bLeft : x;
					nodeY = bBottom;
				}
			}
		}else{
			if(x > bx){
				if(Math.abs(x - bRight) < Math.abs(y - bTop)){
					nodeX = bRight;
					nodeY = (y < bTop) ? bTop : y;
				}
				else{
					nodeX = (x > bRight) ? bRight : x;
					nodeY = bTop;
				}
			}
			else{
				if(Math.abs(x - bLeft) < Math.abs(y - bTop)){
					nodeX = bLeft;
					nodeY = (y < bTop) ? bTop : y;
				}
				else{
					nodeX = (x < bLeft) ? bLeft : x;
					nodeY = bTop;
				}
			}
		}
		
		return { 
			'x' : nodeX,
			'y' : nodeY,
			'detached' : false};
	};
	
	
	// boundary events
	/**
	 *	Move / Detach boundary events to a border and move
	 */
	graph.addListener("onMouseMove", function(node, info, e){
		if(ghostNode){
			var pos = info.getPos();
			var px = pos.x,
				py = pos.y;
			var bParent = ghostNode.data.$boundaryParent;
			
			// move normal element
			if(!bParent){
				graph.moveNode(ghostNode, px, py);
				return;
			}
			
			// move boundary event
			var bPos = computeBoundaryPos(px, py, bParent);
			if(bPos.detached){
				delete ghostNode.data.$boundaryParent;
			}
			graph.moveNode(ghostNode, bPos.x, bPos.y);
		}
	});
	
	/**
	 *	Attach a ghostNode boundary event to a subProcess if hovered.
	 */
	graph.addListener("onMouseEnter", function(node, info, e){
		if(ghostNode){
			var isBoundaryEvent = ghostNode.data.$boundaryEvent;
			var allowsBoundaryEvents = (node.data.$type == "bpmn_subprocess");
			if(isBoundaryEvent && allowsBoundaryEvents){
				ghostNode.data.$boundaryParent = node;
			}
		}
	});
	
	// ADD GHOST NODE
	var ghostNode;
	var ghostNodeEnabled = false;
	
	/**
	 *	GWT interface to enable/disable ghost nodes
	 */
	graph.gwtExt.setGhostNodesEnabled = function(state){
		if(!state){
			setGhostNode(false);
		}
		ghostNodeEnabled = state;
	};
	
	/**
	 *	Clears old ghostNode, if necessary, and assigns a new one.
	 */
	var setGhostNode = function(node){
		if(!ghostNodeEnabled){
			return;
		}
		var makeGhost = function(n, visible){
			var data = n.data;
			// this prevents invisible elements from reappearing
			if(data.$alpha != 0){
				if(visible){
					data.$alpha = 1.0;
					delete n.data.$unselectable;
				}else{
					data.$alpha = 0.5;
					data.$unselectable = true;
				}
			}
			var children = data.$children;
			for(var c in children){
				makeGhost(children[c], visible);
			}
		};
		if(ghostNode){
			makeGhost(ghostNode, true);
			graph.setNodeData(ghostNode.id, {'$alpha' : 1.0});
			ghostNode = false;
			graph.BPMN.extendedLanes = false;
			graph.BPMN.extendedPools = false;
		}
		if(node){
			makeGhost(node, false);
			ghostNode = node;
			graph.setNodeData(ghostNode.id, {'$unselectable' : true});
			
			if(ghostNode.data.$type == "bpmn_lane"){
				graph.BPMN.extendedPools = true;
			}
			else if(ghostNode.data.$type != "bpmn_pool"){
				graph.BPMN.extendedLanes = true;
			}
		}
	}
	
	/**
	 *	Created nodes become ghostNodes.
	 */
	graph.addListener("onCreateNode", function(node){
		setGhostNode(node);
	});
	
	/**
	 *	Prevents creating edges when a ghost node is clicked
	 */
	graph.addListener("onCreateDanglingEdge", function(connectedNode, isIncoming, data){
		return !ghostNode;
	});
	
	
	/**
	 *	CLICK
	 *	- assigns lanes to clicked pools
	 *	- assigns boundary events to parents
	 *	- removes ghostNodes
	 *	- clears GWT node list selection
	 */
	graph.addListener("onClick", function(node, info, e){
		graph.gwtExt.hideRightClickMenu();
		
		if(ghostNode){
			
			if(ghostNode.data.$boundaryEvent){
				var bParent = ghostNode.data.$boundaryParent;
				if(bParent){
					bParent.data.$children.push(ghostNode);
				}
			}
			if(ghostNode.data.$type != "bpmn_lane"){
				setGhostNode(false);
			}
			return;
		}
		
		if(!node || !node.nodeFrom){
			graph.gwtExt.selectTreeNode("#null");
			return;
		}
		//graph.gwtExt.selectTreeNode(node.id);
	});
	
	/**
	 * 	Moves elements to top layer if dragged
	 */
	graph.addListener("onDragFar", function(node, info, e){
		
		if(node){
			var type = node.data.$type;
			if(type != "bpmn_pool" && type != "bpmn_lane"){
				graph.setNodeToTop(node, true);
			}
		}
	});
	
	// ghost node drag
	var allowEdges = true;
	var isDragging = false;
	var dragStartPos;
	
	/**
	 *	Add ghostNode as child to a clicked subProcess
	 */
	graph.addListener("onDragStart", function(node, info, e){
		
		// add node to parent if a parent was clicked,
		if(ghostNode){
			var gType = ghostNode.data.$type;
			var isPool = (node) ? node.data.$type == "bpmn_pool" : false;
			
			switch(gType){
				case "bpmn_lane":
					
					// only add lanes to pools
					if(isPool){
						var mPosY = info.getPos().y;
						graph.BPMN.addLaneToPool(ghostNode, node, mPosY);
						ghostNode.data.$movable = false;
						setGhostNode(false);
					}else{
						graph.gwtExt.notification("Click on a pool to add the lane!\nRight click to discard the lane.", 2500);
					}
					return;
					
				case "bpmn_pool":
					// do not add pools to anything
					break;
				default:
					// add every other element as a child, but not to pools
					if(node && !isPool && node.data.$children){
						graph.addChild(ghostNode, node);
						setGhostNode(node);
						return;
					}
			}
		}
		dragStartPos = info.getPos();
	});
	
	/**
	 * 	Register dragging if element is dragged further than 3 pixels,
	 *	also aligns a dragged boundary event along its borders
	 */
	graph.addListener("onDragMove", function(node, info, e){
		if(dragStartPos && !ghostNode && !isDragging && node && node.data.$custom && !node.nodeFrom){
			var pos = info.getPos();
			var delta = Math.abs(dragStartPos.x - pos.x);
			if(delta > 3){
				setGhostNode(node);
				isDragging = true;
				return;
			}
			delta = Math.abs(dragStartPos.y - pos.y);
			if(delta > 3){
				setGhostNode(node);
				isDragging = true;
				return;
			}
			return false;
		}
		// position boundary events
		if(ghostNode){
			if(ghostNode.data.$boundaryEvent){
				var pos = info.getPos();
				var bPos = computeBoundaryPos(pos.x, pos.y, ghostNode.data.$boundaryParent);
				if(!bPos.detached){
					graph.moveNode(ghostNode, bPos.x, bPos.y);
				}
			}
			else if(ghostNode.data.$type == "bpmn_lane"){
				// do nothing
				return;
			}
			// switch ghostNode if it is not the one we are dragging
			if(node != ghostNode){
				setGhostNode(node);
			}
		}
		
	});
	
	/**
	 *	If GhostNode was created by dragging a node, discard it if the dragging stops.
	 */
	var dragEndFunction =
		function(node, info, e){
			if(ghostNode && ghostNode.data.$type == "bpmn_lane" && ghostNode.data.$movable){
				return;
			}
			setGhostNode(false);
			isDragging = false;
			dragStartPos = false;
	};
	graph.addListener("onDragEnd", dragEndFunction);
	graph.addListener("onDragCancel", dragEndFunction);
	
	
	var theme = "Clean";
	var cssGraphTheme = ".graphStyle"+ theme;
	graph.setExplicitNodeStyleCSS(cssGraphTheme, "color", "textColor");
	graph.setExplicitNodeStyleCSS(cssGraphTheme, "color", "edgeColor");
	graph.setExplicitNodeStyleCSS(cssGraphTheme, "border", "strokeColor");
	graph.setExplicitNodeStyleCSS(cssGraphTheme, "background", "fillColor");
	graph.setExplicitNodeStyleCSS(cssGraphTheme + "Focus", "border", "focusColor");
	graph.setExplicitNodeStyleCSS(cssGraphTheme + "Focus", "border", "markStrokeColor");
	graph.setExplicitNodeStyleCSS(cssGraphTheme + "Focus", "color", "markFillColor");
	
	/*
	var bpmnTest=
		'<process id="PROCESS_1" isClosed="false" isExecutable="true" processType="None">\n'
	    +' <startEvent id="_3" isInterrupting="true" name="Start Event" parallelMultiple="false">\n'
	    +'  <outgoing>_5</outgoing>\n'
	    +' </startEvent>\n'
	    +' <task completionQuantity="1" id="_4" isForCompensation="false" name="Task" startQuantity="1">\n'
	    +'  <incoming>_5</incoming>\n'
	    +'  <incoming>_8</incoming>\n'
	    +'  <outgoing>_7</outgoing>\n'
	    +' </task>\n'
	    +' <sequenceFlow id="_5" sourceRef="_3" targetRef="_4"/>\n'
	    +' <exclusiveGateway gatewayDirection="Diverging" id="_6" name="Exclusive Gateway">\n'
	    +'  <incoming>_7</incoming>\n'
	    +'  <outgoing>_8</outgoing>\n'
	    +'  <outgoing>_10</outgoing>\n'
	    +'  <outgoing>_14</outgoing>\n'
	    +' </exclusiveGateway>\n'
	    +' <sequenceFlow id="_7" sourceRef="_4" targetRef="_6"/>\n'
	    +' <sequenceFlow id="_8" sourceRef="_6" targetRef="_4"/>\n'
	    +' <task completionQuantity="1" id="_9" isForCompensation="false" name="Task2" startQuantity="1">\n'
	    +'  <incoming>_10</incoming>\n'
	    +'  <outgoing>_19</outgoing>\n'
	    +' </task>\n'
	    +' <sequenceFlow id="_10" sourceRef="_6" targetRef="_9"/>\n'
	    +' <task completionQuantity="1" id="_13" isForCompensation="false" name="Task3" startQuantity="1">\n'
	    +'  <incoming>_14</incoming>\n'
	    +'  <outgoing>_20</outgoing>\n'
	    +' </task>\n'
	    +' <sequenceFlow id="_14" sourceRef="_6" targetRef="_13"/>\n'
	    +' <endEvent id="_18" name="End Event">\n'
	    +'  <incoming>_19</incoming>\n'
	    +'  <incoming>_20</incoming>\n'
	    +' </endEvent>\n'
	    +' <sequenceFlow id="_19" sourceRef="_9" targetRef="_18"/>\n'
	    +' <sequenceFlow id="_20" sourceRef="_13" targetRef="_18"/>\n'
	    +'</process>';
	
	
	
	//graph.setNodeStyleCSS(".graphStyle");//".gwt-StackLayoutPanel .gwt-StackLayoutPanelHeader");
	//graph.setExplicitNodeStyleCSS("", "background", "font");
	graph.setExplicitNodeStyleCSS(".gwt-TabLayoutPanel .gwt-TabLayoutPanelTab-selected", "color", "textColor");
	graph.setExplicitNodeStyleCSS(".gwt-TabLayoutPanel .gwt-TabLayoutPanelTab-selected", "color", "edgeColor");
	// #333333
	// same as fill in standard, chrome; bad in dark
	graph.setExplicitNodeStyleCSS(".gwt-DecoratedTabBar .tabMiddleCenter", "background", "strokeColor");
	// #8E8E8E
	graph.setExplicitNodeStyleCSS(".gwt-StackLayoutPanel .gwt-StackLayoutPanelHeader-hovering", "background", "fillColor");
	// #d3def6
	graph.setExplicitNodeStyleCSS("a, a:visited", "color", "focusColor");
	graph.setExplicitNodeStyleCSS("a, a:visited", "color", "markStrokeColor");
	// #0066cc
	graph.setExplicitNodeStyleCSS(".gwt-TabBar", "background", "markFillColor");
	// #cccccc
	*/
	
	// background-color : .gwt-DecoratedStackPanel .stackItemTopLeft 
	// color :
	// border-color : 
	//graph.setNodeStyle("nodeFamily", null, "#bbbbbb");
	//paintBPMN();
	//paintDOT();
	//paintPipes();
	
	
}
