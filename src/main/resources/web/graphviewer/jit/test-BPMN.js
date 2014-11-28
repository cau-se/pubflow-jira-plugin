
function paintBPMN(){
	
	// load plugin
	importBPMN(graph);
	// add nodes
	BPMN.addPool(100, "customPool0", "SuperPool", ["lane 0", "lane 1", "woobiwobbiwab"]);
	
	
	// show Coordinates in node
	graph.addListener('onMouseMove', function(node, info, e){
		var n = graph.getNode("customNode3");
		
		if(n){
			graph.setNodeData("customNode3", {"$label" : [(Math.round(info.getPos().x) +", " + Math.round(info.getPos().y))]});
			//graph.setNodeData("customNode3", {"$label" : [node.id]});
		}
	});

	
	var shuffle = 0;
	var nodeCount = 0;
	var gridVis = true;
	graph.addListener('onClick', function(node, info, e){
		
		if(!node && !graph.isEdgeDragged()){
			var pos = info.getPos();
			var testName= "Broetchen\nbacken\nsich\nnicht\nvon\nselbst";
			for(var a=0, l= nodeCount; a < l; a++){
				testName += "\n!";
			}
			
			var shuffle = ++nodeCount % 7;
			
			switch(shuffle){
			case 0: BPMN.addEvent(pos.x, pos.y, "customNode"+(nodeCount), testName);
					break;
			case 1: BPMN.addGateway(pos.x, pos.y, "customNode"+(nodeCount), testName);
					break;
			case 2: BPMN.addActivity(pos.x, pos.y, "customNode"+(nodeCount), testName);
					break;
			case 3: BPMN.addDataObject(pos.x, pos.y, "customNode"+(nodeCount), testName, "input");
					break;
			case 4: BPMN.addDataObject(pos.x, pos.y, "customNode"+(nodeCount), testName, "output");
					break;
			case 5: BPMN.addDataObject(pos.x, pos.y, "customNode"+(nodeCount), testName, "data");
					break;
			case 6: BPMN.addAnnotation(pos.x, pos.y, "customNode"+(nodeCount), testName);
					break;
			}
		}else{
			graph.markNode(node, "#ff0000");
		}
	});
}