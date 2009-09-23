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
	var treeHTML = '';
	for(var i = 0 ; i < nodeList.length; i++)	 {
		var strName = nodeList[i].getAttribute("name") ;
		var id = eXoWCM.PluginUtils.generateIdDriver(nodeList[i]);
		treeHTML += '<div class="Node" onclick="eXoWCM.PluginUtils.actionColExp(this);">';
		treeHTML += 	'<div class="ExpandIcon">';		
		treeHTML += 		'<a title="'+strName+'" class="NodeIcon DefaultPageIcon" href="javascript:void(0);" onclick="eXoWCM.PluginUtils.renderBreadcrumbs(this);" name="'+strName+'" id="'+id+'">';
		treeHTML +=			strName;	
		treeHTML +=			'</a>';
		treeHTML += 	'</div>';			
		treeHTML += '</div>';			
		var tmp = eXoWCM.PluginUtils.renderSubTree(nodeList[i]);
		if(tmp != '') treeHTML += tmp;
	}
	var leftWorkSpace = document.getElementById('LeftWorkspace');	
	if(leftWorkSpace) leftWorkSpace.innerHTML = treeHTML;
};

// render children container
PluginUtils.prototype.renderSubTree = function(currentNode) {
	if(!currentNode) return;
	var nodeList = currentNode.getElementsByTagName('Folder');
	var treeHTML = '';
	if(nodeList && nodeList.length > 0) {
		treeHTML += '<div class="ChildrenContainer" style="display:none;">'	;
		for(var i = 0; i < nodeList.length; i++) {
			var id = eXoWCM.PluginUtils.generateIdDriver(nodeList[i]);
			var strName = nodeList[i].getAttribute("name");
			var driverPath = nodeList[i].getAttribute("driverPath");
			treeHTML += '<div class="Node" onclick="eXoWCM.PluginUtils.actionColExp(this);">';
			treeHTML += 	'<div class="ExpandIcon">';
			treeHTML +=			'<a title="'+strName+'" class="NodeIcon DefaultPageIcon" href="javascript:void(0);" onclick="getDir(this, event);" name="'+strName+'" id="'+id+'"  driverPath="'+driverPath+'">';
			treeHTML +=				strName;	
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
			var id = eXoWCM.PluginUtils.generateIdNodes(nodeList[i], currentNode.id);
			var strName = nodeList[i].getAttribute("name");
			treeHTML += '<div class="Node" onclick="eXoWCM.PluginUtils.actionColExp(this);">';
			treeHTML += 	'<div class="ExpandIcon">';
			treeHTML +=			'<a title="'+ strName +'" class="NodeIcon DefaultPageIcon" href="javascript:void(0);" onclick="getDir(this, event);" name="'+strName+'" id="'+id+'">';
			treeHTML +=				strName;	
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
				var id = eXoWCM.PluginUtils.generateIdNodes(currentNodeList[i], currentNode.id);
				var	strName	= currentNodeList[i].getAttribute("name");
				treeHTML += '<div class="Node" onclick="eXoWCM.PluginUtils.actionColExp(this);">';
				treeHTML += 	'<div class="ExpandIcon">';
				treeHTML +=			'<a title="'+strName+'" class="NodeIcon DefaultPageIcon" href="javascript:void(0);" onclick="getDir(this, event);" name="'+strName+'" id="'+id+'">';
				treeHTML +=				strName;	
				treeHTML += 		'</a>';
				treeHTML +=		'</div>';
				treeHTML +=	'</div>';
			}
			var parentNode = eXo.core.DOMUtil.findAncestorByClass(currentNode, "Node");
			var nodeIcon = eXo.core.DOMUtil.findAncestorByTagName(currentNode, "div");
			var nextElementNode = eXo.core.DOMUtil.findNextElementByTagName(parentNode, "div");
			var tmpNode = document.createElement("div");
			tmpNode.className = "ChildrenContainer" ;
			tmpNode.innerHTML = treeHTML;
			if(nextElementNode && nextElementNode.className == "Node") {
				nextElementNode.parentNode.insertBefore(tmpNode, nextElementNode) ;
				nodeIcon.className = 'CollapseIcon';				
				tmpNode.style.display = "block";
			} else if(nextElementNode && nextElementNode.className == "ChildrenContainer"){
				eXoWCM.PluginUtils.actionColExp(parentNode);
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
	var iconElt = eXo.core.DOMUtil.getChildrenByTagName(objNode, "div")[0];
	if(nextElt && nextElt.style.display != 'block') {
		nextElt.style.display = 'block';
		iconElt.className = 'CollapseIcon';
	} else {
		nextElt.style.display = 'none';
		iconElt.className = 'ExpandIcon';
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
		var clazzItem = eXoWCM.PluginUtils.getClazzIcon(filesList[i].getAttribute("nodeType"));
		var url 			= filesList[i].getAttribute("url");
		var nodeType	= filesList[i].getAttribute("nodeType");
		var node = filesList[i].getAttribute("name");
		listItem += 	'<td class="Item '+clazzItem+'" url="'+url+'" nodeType="'+nodeType+'" onclick="eXoWCM.PluginUtils.insertContent(this);">'+name+'</td>';
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
	if(!currentNode) return;
	if(typeof(currentNode) == 'string') currentNode = document.getElementById(currentNode);
	var breadscrumbsContainer = document.getElementById("BreadcumbsContainer");
	breadscrumbsContainer.innerHTML = '';
	var beforeNode = null;
	while(currentNode.className != "LeftWorkspace") {
		var curName = currentNode.getAttribute('name');
		if(curName != null) {
			var tmpNode = document.createElement("div");	
			tmpNode.className = 'BreadcumbTab';
			var strHTML = '';
			var strOnclick = '';
			var node = document.getElementById(currentNode.id);
			if(node) strOnclick = "eXoWCM.PluginUtils.actionBreadcrumbs('"+node.id+"')";		
			if(beforeNode == null) {
				strHTML += '<a class="Nomal" href="javascript:void(0);" onclick="'+strOnclick+'">'+curName+'</a>';
				tmpNode.innerHTML = strHTML;
				breadscrumbsContainer.appendChild(tmpNode);
			} else {
				strHTML += '<a class="Nomal" href="javascript:void(0);" onclick="'+strOnclick+'">'+curName+'</a>';
				strHTML += '<div class="RightArrowIcon"><span></span></div>';
				tmpNode.innerHTML = strHTML;
				breadscrumbsContainer.insertBefore(tmpNode, beforeNode);
			}
			beforeNode = tmpNode;
		}
		currentNode = currentNode.parentNode;
		if(currentNode != null && currentNode.className == 'ChildrenContainer'){
			currentNode = eXo.core.DOMUtil.findPreviousElementByTagName(currentNode, 'div');
			currentNode = currentNode.getElementsByTagName('div')[0].getElementsByTagName('a')[0];
		}
		
	}
};

PluginUtils.prototype.generateIdDriver = function(objNode) {
	if(!objNode) return;
	var id = '';
	while(objNode.tagName != 'Connector') {
		var curName = objNode.getAttribute("name").replace(" ", "");
		id =  curName+"_"+id;
		objNode = objNode.parentNode;
	}
	return id;
};

PluginUtils.prototype.generateIdNodes = function(objNode, idNode) {
	if(!objNode && !idNode) return;
	var id = '';
	while(objNode.tagName != 'Folders') {
		var curName = objNode.getAttribute("name").replace(" ", "");
		id =  idNode+"_"+curName;
		objNode = objNode.parentNode;
	}
	return id;
};

PluginUtils.prototype.actionBreadcrumbs = function(nodeId) {
	var element = document.getElementById(nodeId);
	var node =  eXo.core.DOMUtil.findAncestorByClass(element, "Node");
	eXoWCM.PluginUtils.actionColExp(node);
	eXoWCM.PluginUtils.renderBreadcrumbs(element);
};

PluginUtils.prototype.insertContent = function(objContent) {
	if(!objContent) return;
	var hostName = eXoPlugin.hostName;
	var nodeType = objContent.getAttribute('nodeType');
	var url 	= objContent.getAttribute('url');
	var name 	= objContent.innerHTML;
	var strHTML = '';
	if(nodeType.indexOf("image") >=0) {
		strHTML += "<img src='"+url+"' name='"+name+"' alt='"+name+"'/>";
	} else {
		strHTML += "<a href='"+url+"'>"+name+"</a>";
	}
	FCK.InsertHtml(strHTML);
	FCK.OnAfterSetHTML = window.close();
};

if(!window.eXoWCM) eXoWCM = new Object();
eXoWCM.PluginUtils = new PluginUtils();