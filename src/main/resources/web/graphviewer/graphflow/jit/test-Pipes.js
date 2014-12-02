
function paintPipes(){
	
	// adds a listener that removes invalid Edges
	graph.addEdgeConstraints();
	
	var node1 = {
			"id" : "superNode1",
			"name" : "i am node",
			"tooltip" : "look at me, i'm a node!"
		};

		var node2 = {
			"id" : "superNode2",
			"name" : "i am node\n this is superlong text!\na\nb\nc\nd\ne\nf\ng\nh",
			"tooltip" : "look at me, i'm a node!"
		};

		// the above defined node is created at position x:0, y: -100, where x:0,
		// y:0 is the center of the canvas

		var inputPorts1 = [ {
			"name" : "inputPort",
			"id" : "ip1",
			"tooltip" : "input goes here"
		}, {
			"name" : "inputPort",
			"id" : "ip2",
			"tooltip" : "input goes here"
		}, {
			"name" : "inputPort",
			"id" : "ip3",
			"tooltip" : "input goes here"
		}, {
			"name" : "inputPort",
			"id" : "ip4"
		}, {
			"name" : "inputPort",
			"id" : "ip5",
			"tooltip" : "input goes here"
		}, {
			"name" : "inputPort",
			"id" : "ip6"
		} ];

		// define an array of output ports. The id must be unique only within a
		// node. Tooltips are optional.
		var outputPorts1 = [ {
			"name" : "outputPort",
			"id" : "op1",
			"symbol" : ":)"
		} ];

		var inputPorts2 = [ {
			"name" : "inputPort",
			"id" : "ip1",
			"tooltip" : "input goes here"
		}, {
			"name" : "inputPort",
			"id" : "ip2",
			"tooltip" : "input goes here",
			"symbol" : "fu"
		}, {
			"name" : "inputPort",
			"id" : "ip3",
			"tooltip" : "input goes here"
		}, {
			"name" : "inputPort",
			"id" : "ip4"
		}, {
			"name" : "inputPort",
			"id" : "ip5",
			"tooltip" : "input goes here"
		}, {
			"name" : "inputPort",
			"id" : "ip6"
		} ];

		// define an array of output ports. The id must be unique only within a
		// node. Tooltips are optional.
		var outputPorts3 = [ {
			"name" : "outputPort",
			"id" : "op1"
		} ];
		
		
		// add nodes
		var c = graph.getScreenCenter();
		graph.addNode(c.x, c.y, node1, [{"id":"rp1", "symbol" : "k"}], inputPorts1, outputPorts1, 'Filter');
		
		graph.addNode(c.x, c.y, node2, null, inputPorts2, outputPorts3, 'Filter2');
		
		// add edges
		graph.addEdge("superNode1.op1", "superNode2.ip1", "This label!\ncontains a linebreak!", {"$bendPoints":[{"x":100,"y":0}]}, true);
		
		var nodeCount=0;
		var rClickFunc = 
			function(node, info, e){
				if(!node && !graph.isEdgeDragged()){
					var pos = info.getPos();
					// define some properties of the created node. The id must be unique!
					var node2 = {"id":"Node"+(nodeCount++), 
								  "name":"Node "+ nodeCount, 
								  "tooltip":"I am a tooltip!"};
					
					// add a Filter-Node with a single input-port and no output- or repository-ports
					graph.addNode(pos.x, pos.y, node2, null, [{"name":"inputPort", "id":"ip1"}], [{"name":"inputPort", "id":"op1"}], "Filter");
				}else if(!node.nodeFrom){
					graph.markNode(node, "#FF0000");
				}
		};
		//graph.addListener('onClick', rClickFunc);
		
		
}