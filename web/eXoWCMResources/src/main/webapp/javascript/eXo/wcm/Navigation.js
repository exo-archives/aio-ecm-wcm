function getCurrentNodes(navigations, selectedNodeUri) {
	var currentNodes = new Array();
	var currentNodeUris = new Array();
	currentNodeUris = selectedNodeUri.split("/");
	
	for (var i in navigations) {
		for (var j in navigations[i].nodes) {
			if(navigations[i].nodes[j].name == currentNodeUris[0]) {
				currentNodes[0] = navigations[i].nodes[j];
				break;
			}
		}
	}
	
	function getChild(currentNodeUris, children, index) {
		for (var i in children) {
			if(currentNodeUris[index] == children[i].name) {
				currentNodes[index] = children[i];
				break;
			}
		}
		if (!currentNodes[index].children) {
			getChild(currentNodeUris, currentNodes[index].children, ++index); 
		}
	}
	
	if (currentNodeUris.length > 1)
		getChild(currentNodeUris, currentNodes[0].children, 1);
		
	return currentNodes;
}