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
	
	// construct GraphFlow object
	var graph = GraphFlow();
	
	// tell the graph what should happen when errors occur
	graph.addListener("onError", function(e){Log.write(e);});
	
	// adds a listener that removes invalid Edges 
	graph.addEdgeConstraints();
	
	// add icons
	
	/* TODO: customize image path */
	var imagePath = 'images\\';
	graph.setNodeIcon('Timer', imagePath + 'Timer.png');
	graph.setNodeIcon('Start', imagePath + 'start.png');
	graph.setNodeIcon('Reader', imagePath + 'download.png');
	graph.setNodeIcon('Exception', imagePath + 'exception.png');
	graph.setNodeIcon('NHTask', imagePath + 'automaticTask.png');
	graph.setNodeIcon('Condition', imagePath + 'Decission.png');
	graph.setNodeIcon('HTask', imagePath + 'humanTask.png');
	graph.setNodeIcon('Writer', imagePath + 'upload.png');
	graph.setNodeIcon('Finish', imagePath + 'Stop.png');
	
	// Changing Graph Colors:
		// we choose a nice green
		graph.setNodeStyle("#CCCCFF", "#444488", "#4444AA", "#000044", "#EEEEFF");
	
	// prohibit node deletion via mouse click, also hides the cross button on nodes
	//graph.setReadOnly(false, true);

	// define nodes
	graph.addNode(100, 0, {'id' : 'node1_1', 'name' : 'Timer'}, null, null, [{'id':'op1'}], 'Timer');
	graph.addNode(200, 0, {'id' : 'node1_2', 'name' : 'Start'}, null, null, [{'id':'op1'}], 'Start');
	graph.addNode(0, 0, {'id' : 'node2_1', 'name' : 'Load From DB'}, null, [{'id':'ip1'}], [{'id':'op1'}, {'id':'op2'}], 'Reader');
	graph.addNode(0, 0, {'id' : 'node2_2', 'name' : 'Exception'}, null, [{'id':'ip1'}], null, 'Exception');
	graph.addNode(0, 0, {'id' : 'node3_1', 'name' : 'Non-Human Task'}, null, [{'id':'ip1'}], [{'id':'op1'}, {'id':'op2'}], 'NHTask');
	graph.addNode(0, 0, {'id' : 'node3_2', 'name' : 'Exception'}, null, [{'id':'ip1'}], null, 'Exception');
	graph.addNode(0, 0, {'id' : 'node4_1', 'name' : 'On Condition'}, null, [{'id':'ip1'},{'id':'ip2'}], [{'id':'op1'}, {'id':'op2'}], 'Condition');
	graph.addNode(0, 0, {'id' : 'node4_2', 'name' : 'Human Task'}, null, [{'id':'ip1'}], [{'id':'op1'}], 'HTask');
	graph.addNode(0, 0, {'id' : 'node5_1', 'name' : 'Upload Data'}, null, [{'id':'ip1'}], [{'id':'op1'}, {'id':'op2'}], 'Writer');
	graph.addNode(0, 0, {'id' : 'node5_2', 'name' : 'Exception'}, null, [{'id':'ip1'}], null, 'Exception');
	graph.addNode(0, 0, {'id' : 'node6_1', 'name' : 'Finish'}, null, [{'id':'ip1'}], null, 'Finish');
	
	// define edges
	graph.addEdge("node1_1.op1", "node2_1.ip1");
	graph.addEdge("node1_2.op1", "node2_1.ip1");
	graph.addEdge("node2_1.op1", "node3_1.ip1");
	graph.addEdge("node2_1.op2", "node2_2.ip1");
	graph.addEdge("node3_1.op1", "node4_1.ip1");
	graph.addEdge("node3_1.op2", "node3_2.ip1");
	graph.addEdge("node4_1.op1", "node5_1.ip1");
	graph.addEdge("node4_1.op2", "node4_2.ip1");
	graph.addEdge("node4_2.op1", "node4_1.ip2");
	graph.addEdge("node5_1.op1", "node6_1.ip1");
	graph.addEdge("node5_1.op2", "node5_2.ip1");
	
	// add bendpoints for the cycle
	//graph.setBendPoints("node4_1.op2", "node4_2.ip1", [{"x":595,"y":-30},{"x":595,"y":105},{"x":290,"y":105},{"x":290,"y":64}]);
	//graph.setBendPoints("node4_2.op1", "node4_1.ip2", [{"x":580,"y":62},{"x":580,"y":5},{"x":275,"y":5},{"x":275,"y":-30}]);
	
	// apply Graph changes
	graph.refresh();
	
	// move Nodes to positions
	
	graph.loadNodePositions("node1_1 -485.99999999999994 -156 node1_2 -486.00000000000006 50.775000000000006 "
							+"node2_1 -240 -54 node2_2 29.999999999999993 156 node3_1 96.03750000000002 -54 "
							+"node3_2 378 156 node4_1 434.23125 -54 node4_2 436.23125 50.77499999999999 "
							+"node5_1 750 -54.00000000000001 node5_2 1014 156 node6_1 1032 -60"); 
	
	
	// adjust zoom level to see the complete graph
	graph.scaleToFit();
	
	
	// Customizing the Grid
	graph.setGridColor("#A0A0AA");
	//graph.setGridVisible(true);
	
	var gridOn = false;
	
	graph.addListener("onRightClick", function(node, info, e){
		gridOn = !gridOn;
		graph.setGridSnap(gridOn);
		graph.setGridVisible(gridOn);
	});
	
	graph.addListener("autoLayout", function(nodePositions, edgeInfo){
		//console.log(nodePositions);
		//console.log(edgeInfo);
		graph.loadPositionsFromLayout("110 227 110 135 396 178 706 264 736 166 1068 178 1086 62 760 56 1418 111 1722 169 1704 77 #232 239 232 190 ; 232 147 232 190 ; ; 560 202 560 276 ; 912 166 912 62 ; ; 1260 62 1260 123 ; 1250 86 1250 114 560 114 560 68 ; 922 68 922 86 ; 1576 111 1576 89 ; 1576 135 1576 181 ; ");
		graph.scaleToFit();
	});
	graph.autoLayout();
	
	
	
}