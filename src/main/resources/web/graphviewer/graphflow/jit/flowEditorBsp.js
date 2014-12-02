var graph;
var nodeCount=0;

function init(){
	graph = GraphFlow();
	
	graph.addListener("onCreateEdge", 	function(sourceNode, targetNode, sourcePort, targetPort){
											var label = document.f1.inputEdge.value;
											
											graph.addEdge(sourcePort.id, targetPort.id, label, true);
											return false; // prohibit original edge
										});
										
	// adds a listener that removes invalid Edges
	graph.addEdgeConstraints();
	
	// add icons
	graph.setNodeIcon('Filter', 'http://icons.iconarchive.com/icons/everaldo/crystal-clear/96/Action-run-icon.png');
	graph.setNodeIcon('Reader', 'http://icons.iconarchive.com/icons/everaldo/crystal-clear/96/Filesystem-folder-yellow-icon.png');
	graph.setNodeIcon('Repository', 'http://icons.iconarchive.com/icons/everaldo/crystal-clear/96/App-ark-2-icon.png');
	
}

/** Adds a node to the graph with parameters, specified in input fields */
function createNode(){
	var nName = document.f1.inputName.value,
		nClass = document.f1.inputClass.value,
		nType = document.f1.inputType.value;
		
	if(nName == ""){
		alert("Please enter a name!");
		return;
	}
	if(nClass == ""){
		nClass = null;
	}
	

	var node2 = {"id":"Node"+nodeCount, 
				  "name": nName, 
				  "nodeClass": nClass,
				  "tooltip":"This is Node number "+nodeCount};
	nodeCount++;
	graph.addNode(0, 0, node2, null, [{"name":"inputPort", "id":"ip1"}], [{"name":"outputPort", "id":"op1"}], nType);
	graph.refresh();
}