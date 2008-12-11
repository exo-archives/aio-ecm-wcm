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
		if (currentNodes[index].children != null && currentNodes[index].children != '') {
			getChild(currentNodeUris, currentNodes[index].children, ++index); 
		}
	}
	
	if (currentNodeUris.length > 1)
		getChild(currentNodeUris, currentNodes[0].children, 1);
		
	return currentNodes;
}

function getBreadcrumbArr(navigations, previousURI, wcmContentTitle) {
	var breadcrumbNodes = new Array();
	var breadcrumbForNavigations = new Array();
	var previousNodeUris = previousURI.split("/");
	var JsonObj = {};
	
	for (var i in navigations) {
		for (var j in navigations[i].nodes) {
			if(navigations[i].nodes[j].name == previousNodeUris[0]) {
				JsonObj = {
					resolvedLabel: navigations[i].nodes[j].resolvedLabel,
					uri: navigations[i].nodes[j].uri
				}
				breadcrumbForNavigations[0] = navigations[i].nodes[j];
				breadcrumbNodes[0] = JsonObj;
				break;
			}
		}
	}
	
	function getChild(previousNodeUris, children, index) {
		for (var i in children) {
			if(previousNodeUris[index] == children[i].name) {
				JsonObj = {
					resolvedLabel: children[i].resolvedLabel,
					uri: children[i].uri
				}
				breadcrumbNodes[index] = JsonObj;
				breadcrumbForNavigations[index] = children[i];
				break;
			}
		}
		if (breadcrumbForNavigations[index].children != null) {
			getChild(previousNodeUris, breadcrumbForNavigations[index].children, ++index); 
		}
	}
	
	if (previousNodeUris.length > 1)
		getChild(previousNodeUris, breadcrumbForNavigations[0].children, 1);
	
	JsonObj = 	{
					resolvedLabel: wcmContentTitle,
					uri: "#"
				}
	breadcrumbNodes[breadcrumbNodes.length] = JsonObj;
		
	return breadcrumbNodes;
}

function getBreadcrumbs() {
	var navigations = eXo.env.portal.navigations;
	var selectedNodeUri = eXo.env.portal.selectedNodeUri;
	var wcmContentTitle = eXo.env.portal.wcmContentTitle;
	var previousURI = eXo.env.portal.previousURI;
	var breadcumbs = new Array();
		
	if (wcmContentTitle != 'null') {
		breadcumbs = getBreadcrumbArr(navigations, previousURI, wcmContentTitle);
	} else {
		breadcumbs = getCurrentNodes(navigations, selectedNodeUri);
	}
	
	return breadcumbs;
}