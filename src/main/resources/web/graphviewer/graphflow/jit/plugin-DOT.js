

var DOT;



function importDOT(graph){
	
	// define Click Listener for edge creation
	graph.addListener('onClick', function(node, info, e){
		if(node && node.data.$type == 'dot_node'){
			
			connectEdge(node.id, false);
		}
	});
	
	
	// define edge offsets
	
	/**
	 * Calculates edge offset, by connecting the edge on one of the 4 sides of the
	 * node. However the right side is reserved as output, while the left side is
	 * reserved as an input.
	 */
	var compassOffset = function(sourcePort, targetPort, isSource, edgeData){
		var offset = {'x':0, 'y':0};
		
		var sx = sourcePort.pos.x,
			sy = sourcePort.pos.y;
		
		var tx = targetPort.pos.x,
			ty = targetPort.pos.y;
		
		var daltaX, deltaY, height, width;
		
		// connect up, down, or east
		if(isSource){
			deltaX = (tx - sx);
			deltaY = (sy - ty);
			height = sourcePort.data.$height;
			width = sourcePort.data.$width;
		}else{
			deltaX = (tx - sx);
			deltaY = (ty - sy);
			height = targetPort.data.$height
			width = - targetPort.data.$width;
		}
			
		// is the target below the source?
		if(Math.abs(deltaX) < Math.abs(deltaY)){
			if(deltaY < -height){
				offset.y = height / 2;
			}
			else{ // if( deltaY > 2 * height){
				offset.y = -height / 2;
			}
		}
		else{
			if(deltaX >  width){
				offset.x = width / 2;
			}
			else{
				offset.x = -width / 2;
			}
		}
		
		return offset;
	};
	
	registerCustomNode('dot_subGraph', '#DDDDDD', '#000000', compassOffset);
	registerCustomNode('dot_node', '#DDDDDD', '#000000', compassOffset);
	
	// increment edge counter
	graph.addListener('onCreateEdge', function(sourceNode, targetNode, sourcePort, targetPort, data){
		var sourceID = sourcePort.id,
			targetID = targetPort.id,
			sData = sourcePort.data,
			tData = targetPort.data,
			sourceType = sData.$type,
			targetType = tData.$type;
		
		var isDot = (sourceType.substring(0, 4) == "dot_");
		if(isDot){
			
			// calculate offsets
			var widthFrom = sData.$width / 2,
				widthTo = tData.$width / 2;
			
			var offsetFrom = {'x': widthFrom, 'y': 0},
			offsetTo = {'x': -widthTo, 'y': 0};
			
			data.$offsetFrom = offsetFrom;
			data.$offsetTo = offsetTo;
			
			data.$type = "dot_edge";
		}
	});
	
	
	DOT = {};
	DOT.dim = graph.getSizeModifier();
	
	
	/**
	 * Reads the entire string-content of a DOT file, constructing a graph out of the data. 
	 * @param input the string representation of the DOT graph
	 */
	DOT.extractGraph = function(input){
		var result, subResult;
		
		// extract the whole inner graph
		var getGraph = /digraph\s\w+\s*{([\s\S]*?)((?="\w+?"|subgraph)[\s\S]*)}/gi;
		
		result = getGraph.exec(input);
		var graphProperties = {};
		var graphStructure = result[2];
		
		// extract graph properties
		var getGraphValue = /\s*(\w*)\s*=\s*([\s\S]+?)\s*\;/gi;
				
		while(subResult = getGraphValue.exec(result[1])){
			graphProperties[subResult[1]] = subResult[2];
		}
		
		// extract the part that contains edges
		
		var getEdgeString = /(\w+\s*\->[\s\S]*)/gi;
		var getEdge =/\s*(\w+)\s*->(\w+)\[([\s\S]*?)\]/gi;
		var getValue = /\s*(\w*)\s*=\s*"([\s\S]+?)"\s*(?:\;|\,)?/gi;
		
		var edges = [];
		var edgeString = getEdgeString.exec(graphStructure);
		
		// were edges defined at all?
		if(edgeString){
			edgeString = edgeString[1];
		
			// extract single edges from edgeString
			var data;
			while(result = getEdge.exec(edgeString)){
				// get additional edge data
				if(result[3]){
					data = {};
					while(subResult = getValue.exec(result[3])){
						// add a $ before the data entry to allow overwriting
						data["$" + subResult[1]] = subResult[2];
					}
				}
				
				// memorize sourceNode, targetNode, data
				edges.push([result[1], result[2], data]);
			}
		}
		
		// add nodes
		DOT.extractSubGraphs(graphStructure);
		
		// add edges
		var edge;
		for(var e in edges){
			edge = edges[e];
			graph.addEdge(edge[0], edge[1], edge[2].$label);
		}
	}

	/**
	 * Takes a DOT-string, containing only node and subgraph-data as input and
	 * adds all nodes to the graph
	 * @param graphStruct the input DOT string
	 */
	DOT.extractSubGraphs = function(graphStruct){
		var result, subResult;
		// this RegEx extracts
		//   1. a }-bracket, signaling that a subgraph has closed
		// or
		//   2. a subgraph id
		//   3. a string containing subgraph properties
		// or
		//   4. a node id
		//   5. a string containing node properties
		var getSubGraph = /\s*(\})\s*|\s*subgraph\s+"(\w+)"\s*\{\s*([\s\S]*?\;)\s*(?=subgraph|")|\s*"(\w+)"\s*\[([^\]]*)\]/gi;
		
		// this RegEx extracts
		// 1. the property 2. its value
		var getValue = /\s*(\w*)\s*=\s*"([\s\S]+?)"\s*(?:\;|\,)?/gi;
		
		var parents = [];
		var id, properties, element, data, isSubGraph, l;
		
		// use regex on the graph string until no matches are found
		while(result = getSubGraph.exec(graphStruct)){
			
			// got the end of a subgraph
			if(result[1]){
				parents.pop();
			}
			
			// got a  subgraph or node
			else{				
				// get id and properties
				if(isSubGraph = !!result[2]){
					id = result[2];
					properties = result[3];
				}else{
					id = result[4];
					properties = result[5];
				}
				
				// extract data from string
				data = [];
				while(subResult = getValue.exec(properties)){
					data["$" + subResult[1]] = subResult[2];
				}
				
				// define the graph element
				element = {'id' : id, 'data' : data};
				
				// add the node/subGraph
				l = parents.length;
				
				if(l  != 0){
					DOT.addNode(0, 0, element, parents[l-1], isSubGraph);
				}else{
					DOT.addNode(0, 0, element, null, isSubGraph);
				}
				
				// append subGraphID as deepest parent
				if(isSubGraph){parents.push(id); }
			}
		}
	}
	
	// very limited x11 color scheme
	var colorTable ={
		'white' : '#FFFFFF',	
		'gray' 	: '#888888',
		'black' : '#000000',
		'red' 	: '#FF0000',
		'green' : '#00FF00',
		'blue' 	: '#0000FF',
		'cyan' 	: '#00FFFF',
		'magenta':'#FF00FF',
		'yellow': '#FFFF00'
	};
	
	/**
	 * adds a custom node to the graph
	 */
	DOT.addNode = function(x, y, element, parent, isSubGraph){
		
		var data = element.data;
		var width = DOT.dim;
		var d;
		
		// map color name to hex color
		if(d = data.$color){
			if(d[0] != '#'){
				if(colorTable.d){
					data.$color = colorTable[d];
				}else{
					data.$color = colorTable.black;
				}
			}
		}else{
			data.$color = colorTable.black;
		}
		
		// map fillcolor name to hex color
		if(d = data.$fillcolor){
			if(d[0] != '#'){
				if(colorTable.d){
					data.$fillcolor = colorTable[d];
				}else{
					data.$fillcolor = colorTable.white;
				}
			}
		}else{
			data.$fillcolor = colorTable.white;
		}
		
		// determine some dimensions
	   	data.$height = 3 * DOT.dim;
	   	data.$movable = isSubGraph;
	   	data.$children = [];
	   	
	   	// convert linebreaks into label array
	   	d = data.$label;
	   	if(d){
	   		data.$label = d.split("\\n");
	   	}
	   	
	   	// calculate text width
		if(d = data.$label){
			for(var l in d){
				width = Math.max(width, graph.getTextWidth(d[l], (1.66 * DOT.dim) + "px Lucida Console"));
			}
			data.$height += d.length * 2 * DOT.dim;
		}
		data.$width = width + 4 * DOT.dim;
	   	
		if(isSubGraph){
			graph.addCustomNode(x, y, element, 'dot_subGraph');
		}else{
			graph.addCustomNode(x, y, element, 'dot_node');
		}
		
		// append to parent
		if(parent){
			var p = graph.getNode(parent);
			var c = graph.getNode(element.id);
			DOT.appendToParent(p, c);
		}
	};
	
	/**
	 * Adds a Node or another subGraph as child to another element
	 */
	DOT.appendToParent = function(parent, child){
		var cd = child.data,
			pd = parent.data,
			dim = pd.$dim;
			children = pd.$children;
		
		var childWidth = cd.$width + 2 * dim;
		
		// expand container width if the child is too wide
		if(childWidth > pd.$width){
			pd.$width = childWidth;
		}
		
		// expand container height
		pd.$height += cd.$height + 2 * dim ;
		
		// calculate vertical position of child
		var childY;
		var pPos = parent.pos.getc(true);
		
		// do children exist already ?
		if(children.length != 0){
			
			var lastChild = children[children.length - 1];
			childY = lastChild.pos.getc(true).y + lastChild.data.$height / 2 
				+ 2 * dim + cd.$height / 2;
			
		// this is the first child
		}else{
			
			childY = pPos.y - pd.$height / 2 + 3 * dim + cd.$height / 2;
			// add the space of the label
			if(pd.$label){
				childY += (pd.$label.length + 2) * 2 * dim;
			}
		}
		
		child.pos.setc(pPos.x, childY);
		
		parent.pos.y += cd.$height / 2 + dim; 
		children.push(child);
	}
	
}

var ShapeHelper = {
	'drawShape' : function(ctx, shape, x, y, width, height){
		// define the shape
		if(shape == "oval"){
			var x0 = x - width;
			var x1 = x + width;
			var y0 = y - height;
			var y1 = y + height;
			
			ctx.moveTo(x0, y);
			ctx.quadraticCurveTo(x0, y0, x, y0);
			ctx.quadraticCurveTo(x1, y0, x1, y);
			ctx.quadraticCurveTo(x1, y1, x, y1);
			ctx.quadraticCurveTo(x0, y1, x0, y);
		}
		else if(shape == "box"){
			var rx, ry;
			rx = x - width;
			ry = y - height;
			ctx.moveTo(rx, ry);
			rx = x + width;
			ctx.lineTo(rx, ry);
			ry = y + height;
			ctx.lineTo(rx, ry);
			ctx.lineTo(x - width, ry);
			ctx.closePath();
		}
	}	
};

var DOT_Objects = {
	'dot_subGraph' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				dim = data.$dim,
				pos = node.pos.getc(true),
				x = pos.x,
				y = pos.y,
				width = data.$width / 2,
				height = data.$height / 2,
				isFilled = (data.$style == "filled"),
				label = data.$label,
				shape = data.$shape;
			
			var ctx = canvas.getCtx();
			ctx.save();
			ctx.beginPath();
			
			// define shape
			ShapeHelper.drawShape(ctx, shape, x, y, width, height);
			
			// fill shape 
			if(isFilled){
				ctx.fillStyle = data.$fillcolor;
				ctx.fill();
			}
			
			// stroke shape
			ctx.fillStyle = data.$color;
			ctx.stroke();
			
			// draw the label
			if(canvas.showLabels && label){
				var tdim = dim * 2;
				var midX, midY, textWidth;
				ctx.font =  (1.66 * dim) + "px Lucida Console";
				midY = y - height + tdim + dim;
				for(var t = 0, l = label.length; t < l; t++){
					textWidth = ctx.measureText(label[t]).width/2;
					midX = x - textWidth;
					
					ctx.fillText(label[t], midX, midY);
					midY += tdim;
				}
			}
			
			ctx.restore();
			
		},
		'contains' : function(node, pos){
		      var npos = node.pos.getc(true), 
		          width = node.data.$width / 2,
		          height = node.data.$height / 2;
		      return Math.abs(pos.x - npos.x) < width && Math.abs(pos.y - npos.y) < height;
	    }
	},
	'dot_node' : {
		'render' : function(node, canvas) {  
			var data = node.data,
				pos = node.pos.getc(true),
				dim = data.$dim,
				x = pos.x,
				y = pos.y,
				width = data.$width / 2,
				height = data.$height / 2,
				isFilled = (data.$style == "filled"),
				label = data.$label,
				shape = data.$shape;
			
			var ctx = canvas.getCtx();
			ctx.save();
			ctx.beginPath();
			
			// define shape
			ShapeHelper.drawShape(ctx, shape, x, y, width, height);
			
			// fill shape 
			if(isFilled){
				ctx.fillStyle = data.$fillcolor;
				ctx.fill();
			}
			
			// stroke shape
			ctx.fillStyle = data.$color;
			ctx.stroke();
			
			// draw the label
			// draw the label
			if(canvas.showLabels && label){
				var tdim = dim * 2;
				var midX, midY, textWidth;
				ctx.font =  (1.66 * dim) + "px Lucida Console";
				midY = y + dim / 2;
				
				for(var t = 0, l = label.length; t < l; t++){
					textWidth = ctx.measureText(label[t]).width/2;
					midX = x - textWidth;
					
					ctx.fillText(label[t], midX, midY);
					midY += tdim;
				}
			}
			
			ctx.restore();
			
		},
		'contains' : function(node, pos){
		      var npos = node.pos.getc(true), 
		          width = node.data.$width / 2,
		          height = node.data.$height / 2;
		      return Math.abs(pos.x - npos.x) < width && Math.abs(pos.y - npos.y) < height;
	    }
	}
	
};


$jit.GraphFlow.Plot.NodeTypes.implement(DOT_Objects);

{
	var DOT_Edge = {
		'render': function(adj, canvas) {
			var data = adj.data,
				dim = adj.Edge.dim,
		        inverse = data.$direction[0] != adj.nodeFrom.id,
				posFrom = adj.nodeFrom.pos.getc(true),
				posTo = adj.nodeTo.pos.getc(true),
				bend = data.$bendPoints;
				offF = data.$offsetFrom,
				offT = data.$offsetTo;
	
			var from = {"x": posFrom.x, "y" : posFrom.y};
			var to = {"x": posTo.x, "y" : posTo.y};
			
			// swap points if the edge direction is "wrong"
			if(inverse){
				var temp = to;
				to = from;
				from = temp;
			}
			
			// add offset
			from.x += offF.x;
			from.y += offF.y;
			to.x += offT.x;
			to.y += offT.y;
			
			var ctx = canvas.getCtx();
			// draw potential bend points
			var	toBend;
			if(bend){
				ctx.beginPath();
				for(var b = 0, l = bend.length; b < l; b++){
					toBend = bend[b];
					ctx.moveTo(from.x, from.y);
					ctx.lineTo(toBend.x, toBend.y);
					from = bend[b];
				}
				ctx.stroke();
		}
		
		// draw final line
		$jit.GraphFlow.EdgeHelper.drawEdge(canvas, from, to, 0, dim, dim, dim);
	
		// add the label
		var label = adj.data.$label;
		if(label && canvas.showLabels){
			ctx.font = (1.23 * dim)+"px Arial";
				
			var	midX = (from.x + to.x - ctx.measureText(label).width) / 2,
				midY = (from.y + to.y - dim) / 2;
		}
		},
		
		'contains': $jit.GraphFlow.EdgeHelper.contains
			/*
			function(adj, pos) {		
	        var data = adj.data,
	        	dim = adj.Edge.dim,
				inverse = data.$direction[0] != adj.nodeFrom.id,
				posFrom = adj.nodeFrom.pos.getc(true),
				posTo = adj.nodeTo.pos.getc(true);
				
			var from = {"x": posFrom.x, "y" : posFrom.y};
			var to = {"x": posTo.x, "y" : posTo.y};
			
			// swap if the arrow points in inverse order
			if(inverse){
				var temp = to;
				to = from;
				from = temp;
			}
			
			// add offsets to node positions
			{
				var offsetFrom = data.$offsetFrom,
					offsetTo = data.$offsetTo;
				
				from.x += offsetFrom.x;
				from.y += offsetFrom.y;
				
				to.x += offsetTo.x;
				to.y += offsetTo.y;
			}
			
			// check if mouse is between bendpoints
			var bend = data.$bendPoints,
				toBend,
				contains = false;
			if(bend){
				for(var b = 0, l = bend.length; b < l; b++){
					toBend = bend[b];
					contains |= EdgeHelper.contains(from, toBend, pos, dim);
					from = bend[b];
				}
			}
			
			return contains || EdgeHelper.contains(from, to, pos, dim);
	      }*/
	};
	
	DOT_Objects.dot_edge = DOT_Edge;
	$jit.GraphFlow.Plot.EdgeTypes.implement({'dot_edge' : DOT_Edge});
}
