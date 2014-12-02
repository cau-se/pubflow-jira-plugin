
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
	
	// adds a listener that removes invalid Edges
	graph.addEdgeConstraints();
	
	// handle errors
	graph.addListener('onError', function(message){console.log(message);});
	
	// handle node creation / deletion
	graph.addListener("onCreateNode", 	function(node){
		graph.gwtExt.addTreeNode(node.id, node.name);
		graph.gwtExt.setChanged();
	});
	
	graph.addListener("onRemoveNode", 	function(node){
		graph.gwtExt.removeTreeNode(node.id);
		graph.gwtExt.selectTreeNode("#null");
		graph.gwtExt.setChanged();
	});
	
	// handle edge manipulation
	graph.addListener("onCreateEdge", 	function(sourceNode, targetNode, sourcePort, targetPort){
		graph.gwtExt.setChanged();
	});
	graph.addListener("onRemoveEdge", 	function(sourceNode, targetNode, sourcePort, targetPort){
		graph.gwtExt.setChanged();
	});
	
	// helper function: sets the transparency of a node and its ports to a specified value
	var setNodeAlpha = function(node, alpha){
		var children = node.data.$children;
		for(var c in children){
			c = children[c];
			c.data.$alpha = alpha;
		}
		graph.setNodeData(node.id, {'$alpha' : alpha});
	}
	
	
	// Restore normal click behaviour on right click
	graph.addListener("onRightClick", 	function(node, info, e){
		var pos = info.getPos();
		
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
			
			// select only node families and
			if(node && (node.data.$custom || node.data.$type == "nodeFamily") && !node.nodeFrom){
				graph.gwtExt.selectTreeNode(node.id);
			}
			else{
				graph.gwtExt.selectTreeNode("#null");
			}
			graph.gwtExt.showRightClickMenu(node, e.clientX, e.clientY);
		}
	});
	
	// KEY PRESSES
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
		if(mNode && keyCode == 46){
			var data = mNode.data;
			
			// disallow port or crossbox removal
			if(data.$custom || data.$type == "nodeFamily"){
				graph.removeNode(mNode, true);
				return;
			}
		}
		// "ESCAPE": abort edge drag, hide Menu
		if(keyCode == 27){
			if(graph.isEdgeDragged()){
				graph.connectEdge(null);
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
	
	// ADD GHOST NODE
	var ghostNode;
	var ghostNodeEnabled = false;
	graph.gwtExt.setGhostNodesEnabled = function(state){
		ghostNodeEnabled = state;
		if(!state){
			if(ghostNode){
				setNodeAlpha(ghostNode, 1.0);
				delete ghostNode.data.$unselectable;
			}
			ghostNode = false;
		}
	};
	
	graph.addListener("onCreateNode", function(node){
		if(ghostNodeEnabled){
			if(ghostNode){
				setNodeAlpha(ghostNode, 1.0);
				delete ghostNode.data.$unselectable;
			}
			
			ghostNode = node;
			ghostNode.data.$unselectable = true;
			setNodeAlpha(ghostNode, 0.5);
		}
	});
	graph.addListener("onMouseMove", function(node, info, e){
		if(ghostNode){
			var pos = info.getPos();
			graph.moveNode(ghostNode, pos.x, pos.y);
		}
	});
	
	// select node in list
	graph.addListener("onClick", function(node, info, e){
		graph.gwtExt.hideRightClickMenu();
		if(ghostNode){
			delete ghostNode.data.$unselectable;
			setNodeAlpha(ghostNode, 1.0);
			ghostNode = false;
			return;
		}
		if(!node || node.nodeFrom || !(node.data.$custom || node.data.$type == "nodeFamily")){
			graph.gwtExt.selectTreeNode("#null");
			return;
		}
		graph.setNodeToTop(node, true);
		graph.gwtExt.selectTreeNode(node.id);
	});
	
	// alpha drag
	graph.addListener("onDragStart", function(node, info, e){
		if(node && node != ghostNode && (node.data.$custom || node.data.$type == "nodeFamily")){
			setNodeAlpha(node, 0.5);
		}
	});
	var dragEndFunction =
		function(node, info, e){
			if(node && node != ghostNode && (node.data.$custom || node.data.$type == "nodeFamily")){
				setNodeAlpha(node, 1.0);
		}};
	
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
	
	
}
