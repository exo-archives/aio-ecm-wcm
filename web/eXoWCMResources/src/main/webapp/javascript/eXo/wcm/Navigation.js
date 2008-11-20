function getNavigations(renderFunction) {
	var http;
	var navigations;
	
	var serviceUrl = getHostName() + '/portal/rest/wcmNavigation/getPortalNavigations?portalName=' + eXo.env.portal.portalName + '&language=en';
	
	if (window.ActiveXObject)
		http = new ActiveXObject('Microsoft.XMLHTTP');
	else
		http = new XMLHttpRequest();
	http.open('GET', serviceUrl, true);
	http.onreadystatechange = function() {
		if (http.readyState == 4) {
			navigations = http.responseText;
			navigations = navigations.substring(1, navigations.length - 1);
			navigations = navigations.replace('"navigations":', 'var navigations = ');
			eval(navigations);
			renderFunction(navigations);
		}
	};
	http.send(null);
}

function getCurrentNodes(navigations) {
	var currentNodes = new Array();
	var prefixPortalBaseURL = eXo.env.portal.context + '/' + eXo.env.portal.accessMode + '/' + eXo.env.portal.portalName + '/';
	if (prefixPortalBaseURL == (eXo.env.server.portalBaseURL+"/")) {
		currentNodes[0] = navigations[0].nodes[0];
		return currentNodes;
	}
	var currentUri = eXo.env.server.portalBaseURL.substring(prefixPortalBaseURL.length,eXo.env.server.portalBaseURL.length);
	var currentNodeUris = currentUri.split("/");
	
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
		if (currentNodes[index].children != null) {
			getChild(currentNodeUris, currentNodes[index].children, ++index); 
		}
	}
	
	if (currentNodeUris.length > 1)
		getChild(currentNodeUris, currentNodes[0].children, 1);
		
	return currentNodes;
}