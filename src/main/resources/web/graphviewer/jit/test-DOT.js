
function paintDOT(){
	var regString = 
		'digraph G {'
		+'rankdir=LR;'
		+'"depNode_0" [label="$",shape="none"]'
		+'subgraph "cluster_container1" {'
		+'	label = "<<execution container>>\\nBjoern-PC";'
		+'	shape = "box";'
		+'	style = "filled";'
		+'	fillcolor = "white";'
		+'	subgraph "cluster_component_1" {'
		+'		label = "<<deployment component>>\\n@1:..SessionAndTraceRegistrationFilter";'
		+'		shape = "box";'
		+'		style = "filled";'
		+'		fillcolor = "white";'
		+'		"depNode_1" [label="doFilter(..)",shape="oval",style="filled",color="#000000",fillcolor="white"]'
		+'	}'
		+'	subgraph "cluster_component_2" {'
		+'		label = "<<deployment component>>\\n@2:..ProductMapper";'
		+'		shape = "box";'
		+'		style = "filled";'
		+'		fillcolor = "white";'
		+'		"depNode_3" [label="getProductListByCategory(..)",shape="oval",style="filled",color="#000000",fillcolor="white"]'
		+'	}'
		+'	subgraph "cluster_component_3" {'
		+'		label = "<<deployment component>>\\n@3:..CatalogService";'
		+'		shape = "box";'
		+'		style = "filled";'
		+'		fillcolor = "white";'
		+'		"depNode_4" [label="getCategory(..)",shape="oval",style="filled",color="#000000",fillcolor="white"]'
		+'		"depNode_2" [label="getProductListByCategory(..)",shape="oval",style="filled",color="#000000",fillcolor="white"]'
		+'	}'
		+'	subgraph "cluster_component_4" {'
		+'		label = "<<deployment component>>\\n@4:..CategoryMapper";'
		+'		shape = "box";'
		+'		style = "filled";'
		+'		fillcolor = "white";'
		+'		"depNode_5" [label="getCategory(..)",shape="oval",style="filled",color="#000000",fillcolor="white"]'
		+'	}'
		+'}'
		+'depNode_0->depNode_1[label=2, style="solid", arrowhead="open", color="#000000"]'
		+'depNode_2->depNode_3[label=1, style="solid", arrowhead="open", color="#000000"]'
		+'depNode_1->depNode_2[label=1, style="solid", arrowhead="open", color="#000000"]'
		+'depNode_1->depNode_4[label=1, style="solid", arrowhead="open", color="#000000"]'
		+'depNode_4->depNode_5[label=1, style="solid", arrowhead="open", color="#000000"]'
		+'}';
	
	// load plugin
	importDOT(graph);
	
	
	graph.addListener("onRightClick", 	function(node, a, b){
		if(node && !node.nodeFrom){
			graph.removeNode(node);
		}
	});
	
	
	
	// build Graph from string
	DOT.extractGraph(regString);
}