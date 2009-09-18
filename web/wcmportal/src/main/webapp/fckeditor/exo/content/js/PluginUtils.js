function PluginUtils() {
}

PluginUtils.prototype.request = function(urlRequestXML) {
	var xmlHttpRequest = false;
	if (window.XMLHttpRequest) {
		xmlHttpRequest = new window.XMLHttpRequest();
		xmlHttpRequest.open("GET",urlRequestXML,false);
		xmlHttpRequest.send("");
		return xmlHttpRequest.responseXML;
		}
	else if (ActiveXObject("Microsoft.XMLDOM")) { // for IE
		xmlHttpRequest = new ActiveXObject("Microsoft.XMLDOM");
		xmlHttpRequest.async=false;
		xmlHttpRequest.load(urlRequestXML);
		return xmlHttpRequest;
	}
	alert("There was a problem retrieving the XML data!");
	return null;
};

PluginUtils.prototype.renderTree = function(objXML) {
	var	xmlTreeNodes = eXoWCM.PluginUtils.request(objXML);
	var nodeList = xmlTreeNodes.getElementsByTagName('Folders');
	var listContainer = document.getElementById('ListContainer');
	if(!listContainer) return;
	var leftWorkSpace = eXo.core.DOMUtil.findFirstDescendantByClass(listContainer, "div", "LeftWorkspace");
	if(!leftWorkSpace) return;
	var treeHTML = '';
	var tmpStrName = '';
	for(var i = 0 ; i < nodeList.length; i++)	 {
		tmpStrName = nodeList[i].getAttribute("name") ;
		treeHTML += '<div class="Node" onclick="eXoWCM.PluginUtils.actionColExp(this);">';
		treeHTML += 	'<div class="ExpandIcon">';		
		treeHTML += 		'<a title="'+tmpStrName+'" class="NodeIcon DefaultPageIcon" href="javascript:void(0);" onclick="getDir(this, event);" name="'+tmpStrName+'" id="'+tmpStrName.replace(" ", "")+'">';
		treeHTML +=			tmpStrName;	
		treeHTML +=			'</a>';
		treeHTML += 	'</div>';			
		treeHTML += '</div>';			
		var tmp = eXoWCM.PluginUtils.renderSubTree(nodeList[i]);
		if(tmp != '') treeHTML += tmp;
	}
	leftWorkSpace.innerHTML = treeHTML;
};

// render children container
PluginUtils.prototype.renderSubTree = function(currentNode) {
	if(!currentNode) return;
	var nodeList = currentNode.getElementsByTagName('Folder');
	var treeHTML = '';
	if(nodeList && nodeList.length > 0) {
		treeHTML += '<div class="ChildrenContainer" style="display:none;">'	;
		for(var i = 0; i < nodeList.length; i++) {
			var tmpStrName = nodeList[i].getAttribute("name");
			treeHTML += '<div class="Node" onclick="eXoWCM.PluginUtils.actionColExp(this);">';
			treeHTML += 	'<div class="ExpandIcon">';
			treeHTML +=			'<a title="'+tmpStrName+'" class="NodeIcon DefaultPageIcon" href="javascript:void(0);" onclick="getDir(this, event);" name="'+tmpStrName+'" id="'+tmpStrName.replace(" ", "")+'"  driverPath="'+nodeList[i].getAttribute("driverPath")+'">';
			treeHTML +=				tmpStrName;	
			treeHTML += 		'</a>';
			treeHTML +=		'</div>';
			treeHTML +=	'</div>';
		}
		treeHTML += '</div>';
	}
	return treeHTML;
};

PluginUtils.prototype.renderSubTrees = function(currentNode, event, connector) {
	var event = event || window.event;
	event.cancelBubble = true;
	if(!currentNode) return;
	var nodeList = currentNode.getElementsByTagName('Folder');
	var treeHTML = '';
	var fileList = '';
	if(nodeList && nodeList.length > 0) {
		fileList = currentNode.getElementsByTagName('File');
		treeHTML += '<div class="ChildrenContainer" style="display:none;">'	;
		for(var i = 0; i < nodeList.length; i++) {
			var tmpStrName = nodeList[i].getAttribute("name");
			treeHTML += '<div class="Node" onclick="eXoWCM.PluginUtils.actionColExp(this);">';
			treeHTML += 	'<div class="ExpandIcon">';
			treeHTML +=			'<a title="'+ tmpStrName +'" class="NodeIcon DefaultPageIcon" href="javascript:void(0);" onclick="getDir(this, event);" name="'+tmpStrName+'" id="'+tmpStrName+'">';
			treeHTML +=				tmpStrName;	
			treeHTML += 		'</a>';
			treeHTML +=		'</div>';
			treeHTML +=	'</div>';
		}
		treeHTML += '</div>';
	} else {
		var xmlTreeNodes = PluginUtils.prototype.request(connector);
		var currentNodeList = xmlTreeNodes.getElementsByTagName('Folder');
		fileList = xmlTreeNodes.getElementsByTagName('File');
		if(currentNodeList && currentNodeList.length > 0) {
			for(var i = 0; i < currentNodeList.length; i++) {
				var	tmpStrName	= currentNodeList[i].getAttribute("name");
				treeHTML += '<div class="Node" onclick="eXoWCM.PluginUtils.actionColExp(this);">';
				treeHTML += 	'<div class="ExpandIcon">';
				treeHTML +=			'<a title="'+tmpStrName+'" class="NodeIcon DefaultPageIcon" href="javascript:void(0);" onclick="getDir(this, event);" name="'+tmpStrName+'" id="'+tmpStrName.replace(" ", "")+'">';
				treeHTML +=				tmpStrName;	
				treeHTML += 		'</a>';
				treeHTML +=		'</div>';
				treeHTML +=	'</div>';
			}
			var parentNode = eXo.core.DOMUtil.findAncestorByClass(currentNode, "Node");
			var nodeIcon = eXo.core.DOMUtil.findAncestorByClass(currentNode, "ExpandIcon");
			var nextElementNode = eXo.core.DOMUtil.findNextElementByTagName(parentNode, "div");
			var tmpNode = document.createElement("div");
			tmpNode.className = "ChildrenContainer" ;
			tmpNode.style.display = "block";
			tmpNode.innerHTML = treeHTML;
			if(nextElementNode && nextElementNode.className == "Node") {
				nodeIcon.className = 'CollapseIcon';				
				nextElementNode.parentNode.insertBefore(tmpNode, nextElementNode) ;
			} else if(nextElementNode && nextElementNode.className == "ChildrenContainer"){
				eXoWCM.PluginUtils.actionColExp(parentNode);
				var parentNodeContainer = eXo.core.DOMUtil.findAncestorByClass(currentNode, "ChildrenContainer");
				if(parentNodeContainer) {
					if(cldrContainer)	{
						parentNodeContainer.removeChild(nextElementNode);
						getDir(currentNode, event);
					}
				}
				if(nodeIcon) nodeIcon.className = 'CollapseIcon';
			} else {
				var cldrContainer = eXo.core.DOMUtil.findAncestorByClass(currentNode, "ChildrenContainer");
				nodeIcon.className = 'CollapseIcon';
				cldrContainer.appendChild(tmpNode);
			}
		}
	}
	eXoWCM.PluginUtils.listFiles(fileList);
	
};

PluginUtils.prototype.actionColExp = function(objNode) {
	if(!objNode) return;
	var nextElt = eXo.core.DOMUtil.findNextElementByTagName(objNode, "div");
	if(!nextElt) return;
	var icoElt = eXo.core.DOMUtil.getChildrenByTagName(objNode, "div")[0];
	if(nextElt && nextElt.style.display != 'block') {
		nextElt.style.display = 'block';
		icoElt.className = 'CollapseIcon';
	} else {
		nextElt.style.display = 'none';
		icoElt.className = 'ExpandIcon';
	}
};

PluginUtils.prototype.listFiles = function(list) {
	if(!list && list.length < 0) return;
	var filesList = list;
	var listItem = '';
	var rightWS = document.getElementById('RightWorkspace');
	var clazz = 'OddItem';
	for(var i = 0; i < filesList.length; i++) {
		if(clazz == 'EventItem') {
			clazz = 'OddItem';
		} else if(clazz == 'OddItem') {
			clazz = 'EventItem';
		}
		listItem += '<tr class="'+clazz+'">';
		listItem += 	'<td class="Item '+ eXoWCM.PluginUtils.getClazzIcon(filesList[i].getAttribute("nodeType"))+'">'+ filesList[i].getAttribute("name") +'</td>';
		listItem +=		'<td class="Item">'+ filesList[i].getAttribute("dateCreated") +'</td>';
		listItem +=		'<td class="Item">'+ filesList[i].getAttribute("size")+'&nbsp;kb' +'</td>';
		listItem +=	'</tr>'
	}
	rightWS.innerHTML =  listItem;
};

PluginUtils.prototype.getClazzIcon = function(nodeType) {
	if(!nodeType) return;
	var strClassIcon = '';
	strClassIcon = nodeType.replace("/", "_").replace(":", "_") + "16x16Icon";
	return strClassIcon;
};

PluginUtils.prototype.renderBreadcrumbs = function(currentNode) {
	return;
	if(!currentNode) return;
	var sHTML = '';
	var breadscrumbsContainer = document.getElementById("BreadcumbsContainer");
	while(currentNode.className != "LeftWorkspace") {
		breadscrumbsContainer.innerHTML = '';
		var curName = currentNode.getAttribute('name');
		if(curName) {
			var strHTML = '<a class="Nomal">'+curName+'</a>';
			if(!currentNode) {
				strHTML 	 += '<div class="RightArrowIcon"><span></span></div>';
			} 
		} 
		var tmpNode = document.createElement("div");
		tmpNode.innerHTML = strHTML;
		breadscrumbsContainer.appendChild(tmpNode);
		currentNode = currentNode.parentNode;
	} 
};

PluginUtils.prototype.referNode = function(objNode, event) {
	return;
	getDir(objNode, event);
};

if(!window.eXoWCM) eXoWCM = new Object();
eXoWCM.PluginUtils = new PluginUtils();