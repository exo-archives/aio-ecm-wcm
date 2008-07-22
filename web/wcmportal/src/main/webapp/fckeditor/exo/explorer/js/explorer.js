function getElementByClassName(sClassName) {
	var aElements = document.getElementsByTagName('*');
	var iLength = aElements.length;
	var aResults = [];
	for (var i = 0; i < iLength; i++) {
		if (aElements[i].className.indexOf(sClassName) >= 0) {
			aResults.push(aElements[i]);
		}
	}
	return aResults;
}

function getElementsByClassPath(root, path) {
	var root = document.getElementById(root) || root;
	if (!root.nodeType) return;
	var aLocation = path.split("/");
	var nMap = aLocation.length;
	var aElement = root.getElementsByTagName("*");
	var nLength = aElement.length;
	var oItem;
	var aResult = [];
	for (var o = 0 ; o < nLength; ++ o) {
		oItem = aElement[o];
		if (hasClass(oItem, aLocation[nMap-1])) {
			for (var i = nMap - 2; i >= 0 ; --i) {
				oItem = getParent(oItem, aLocation[i]);
			}
			if (oItem) 	aResult.push(aElement[o]);
		}
	}
	if (aResult.length) return aResult;
	return null;
	
	// private function
	function hasClass(element, className) {
		return (new RegExp("(^|\\s+)" + className + "(\\s+|$)").test(element.className)) ;
	}
	function getParent(element, className) {
		if (!element) return null;
		var parent = element.parentNode;
		while (parent && parent.nodeName != "HTML") {
			if (hasClass(parent, className)) return parent;
			parent =  parent.parentNode;
		}
		return null;
	}
}

function showSetting() {
	var oSetting = K('explorer').select({where: "className == 'Setting'"})[0];
	if (oSetting.style.display != 'block') {
		oSetting.style.display = 'block';
	} else { 
		oSetting.style.display = 'none';
	}
}

function showContextMenu(id, e, oTarget) {
	var ctx = document.getElementById('Context');
	
	var ctxAdd = getElementsByClassPath(ctx, 'AddNewDocument/MenuItem/IconAdd')[0];
	var ctxView = getElementsByClassPath(ctx, 'ListItemOption/MenuItem/IconView')[0];
	var ctxSelect = getElementsByClassPath(ctx, 'ListItemOption/MenuItem/IconSelect')[0];
	
	var obj = getElementsByClassPath(ctx, id)[0];
	obj.style.left = (e.clientX) + 'px';
	obj.style.top = (e.clientY) + 'px';
	obj.style.display = 'Block';
	
	ctxAdd.id = oTarget.id + 'add/';
	ctxView.id = oTarget.id + 'view/';
	ctxSelect.id = oTarget.id + 'select/';
	return false;
}

function hideContextMenu() {
	var aObjects = getElementByClassName('ContextMenu');
	iLength = aObjects.length;
	for (var i = 0; i < iLength; i++) {
		aObjects[i].style.display = 'none';
	}
}

function openTree(obj) {
	var oNodeOpen = obj.parentNode;
	var oNodeGroup = K(oNodeOpen).select({where: "className == 'NodeGroup'"})[0];
	if (oNodeGroup.style.display != "block") {
		oNodeGroup.style.display = "block";
		obj.className = "Expand";
	} else {
		oNodeGroup.style.display = "none";
		obj.className = "Collapse";
	}
}

function dateFormat(sFullDate) {
	var sYear = sFullDate.substring(0, 4);
	var sMonth = sFullDate.substring(5, 7);
	var sDay = sFullDate.substring(8, 10);
	var sHour = sFullDate.substring(sFullDate.indexOf('T') + 1, sFullDate.indexOf('T') + 3);
	var sMinute = sFullDate.substring(sFullDate.indexOf('T') + 4, sFullDate.indexOf('T') + 6);
	var sSecond = sFullDate.substring(sFullDate.indexOf('T') + 7, sFullDate.indexOf('T') + 9);
	var sMillisecond = sFullDate.substring(sFullDate.indexOf('T') + 10, sFullDate.indexOf('T') + 13);
	
	var date = new Date();
	date.setDate(sDay);
	date.setMonth(sMonth);
	date.setYear(sYear);
	date.setHours(sHour);
	date.setMinutes(sMinute);
	date.setSeconds(sSecond);
	date.setMilliseconds(sMillisecond);
	
	return (date);
} 

	function treeInit(sXML) {
		var oExplorer = K('explorer');
		var oStatus = K('statusBar');
		var oStatusFolder = getElementsByClassPath(oStatus, 'Folder')[0];
		var oTree = getElementsByClassPath(oExplorer, 'Navigation/Tree')[0];
		
		var sHTML = '';
		var oFolders = eXp.getNodes(sXML, "Folder");
		var iLength = oFolders.length;
		if (oFolders && iLength) {
			for (var i = 0 ; i < iLength; i++) {
				var sName = eXp.getNodeValue(oFolders[i], "name");
					sName = sName + '/';
				var sType = eXp.getNodeValue(oFolders[i], "folderType");
					sType = sType.replace(":", "_") + "16x16Icon";
				var sTreeNode = K('hideContainer').select({where: "className == 'TreeNode'"})[0].innerHTML;
				if (i == iLength - 1) sTreeNode = sTreeNode.replace(/\${sClass}/g, 'LastNode');
				else sTreeNode = sTreeNode.replace(/\${sClass}/g, '');
				sTreeNode = sTreeNode.replace(/\${sName}/g, sName);
				sTreeNode = sTreeNode.replace(/\${sType}/g, sType);
				sTreeNode = sTreeNode.replace(/\${sCurrentFolder}/g, '/');
				sHTML += sTreeNode;
			}
			oStatusFolder.innerHTML = iLength + ' folder(s)';
		} else {
			oStatusFolder.innerHTML = '0 folder(s)';
		}
		var oNodeGroup = getElementsByClassPath(oExplorer, 'Navigation/Tree/NodeGroup')[0];
		oNodeGroup.style.display = "block";
		oNodeGroup.innerHTML = sHTML;
	}
	
	function sort(sCondition) {
		var oExplorer = K('explorer');
		var oDocument = getElementsByClassPath(oExplorer, 'Workspace/DisplayArea')[0];
		var oHide = K('hideContainer');
		
		oDocument.innerHTML = '';
		var sHTML = '';
		var aResult = eXp.store.data.Select({orderBy: sCondition});
		var iLength = aResult.length;
		for (var i = 0; i < iLength; i++) {
			var HideTreeItem = getElementsByClassPath(oHide, 'HideTreeItem')[0].innerHTML;
			HideTreeItem = HideTreeItem.replace(/\${sName}/g, aResult[i].name);
			HideTreeItem = HideTreeItem.replace(/\${sType}/g, aResult[i].type);
			HideTreeItem = HideTreeItem.replace(/\${sDateCreated}/g, aResult[i].date.getDate() + '/' + aResult[i].date.getMonth() + '/' + aResult[i].date.getFullYear() + ' ' + aResult[i].date.getHours() + ':' + aResult[i].date.getMinutes());
			HideTreeItem = HideTreeItem.replace(/\${sURL}/g, aResult[i].url);
			HideTreeItem = HideTreeItem.replace(/\${sSize}/g,  aResult[i].size);
			sHTML += '<input type="hidden" id="checkgen">';
			sHTML += HideTreeItem;
		}
		oDocument.innerHTML += sHTML;
	}
	
	function showAddForm(id, show) {
		var oExplorer = K('explorer');
		var oDocument = getElementsByClassPath(oExplorer, 'Workspace/DisplayArea')[0];
		var oHide = K('hideContainer');
		var idOrig = id.replace('add/','');
		var sMaxLength = 23;
		var idShort = (idOrig == '/' ? '/' : (idOrig.length > sMaxLength ? ('... ' + idOrig.substring(idOrig.length - sMaxLength)) : idOrig))
		var idTxt = id.replace('add/','txt/');
		
		var sHideAddForm = getElementsByClassPath(oHide, 'HideAddForm')[0].innerHTML;
		sHideAddForm = sHideAddForm.replace(/\${idOrig}/g, idOrig);
		sHideAddForm = sHideAddForm.replace(/\${idShort}/g, idShort);
		sHideAddForm = sHideAddForm.replace(/\${idTxt}/g, idTxt);
		sHideAddForm = sHideAddForm.replace(/\${id}/g, id);
		
		if (show) {
			oExplorer.innerHTML += sHideAddForm;
			oExplorer.innerHTML += oHideMask.innerHTML;
		} else {
			oExplorer.innerHTML = oExplorer.innerHTML.replace(sHideAddForm, '');
			oExplorer.innerHTML = oExplorer.innerHTML.replace(oHideMask.innerHTML, '', 'g');
		}
	}
		
	function doAddForm(sCurrentFolder, sFolderName) {
		var connector = eXoPlugin.hostName + eXp.connector + 'createFolder';
		var param = eXp.buildParam(
					"type=" + eXp.resourceType,
					"currentFolder=" + sCurrentFolder,
					"newFolderName=" + sFolderName,
					"currentPortal=" + eXoPlugin.portalName
				);
		eXp.sendRequest(
			connector,
			param,
			function(sXML) {
				var oNodes = eXp.getNodes(sXML, "Error")[0];
				var sErrorNumber = eXp.getNodeValue(oNodes, "number");
				var sErrorText = eXp.getNodeValue(oNodes, "text");
				if (sErrorNumber != '0') {
					alert(sErrorText);
				} else {
					showAddForm(sCurrentFolder + 'add/', false);
					getDir(sCurrentFolder);
				}
			}
		);
	}
	
	function showUploadForm(show) {
		var uploadContainer = K("UploadContainer");
		var popupContainer = K("PopupContainer");
		popupContainer.style.display = "block";
		popupContainer.innerHTML = uploadContainer.innerHTML;
		var mask = K("Mask");
		mask.style.display = "block";
	}
	
	function uploadFile() {
		var popupContainer = K("PopupContainer");
		var formUpload = K.select({from: popupContainer, where: "nodeName == 'FORM'"})[0];
		formUpload.id =  eXp.getID();
		var param = eXp.buildParam("uploadId=" + formUpload.id, "currentFolder=/", buildXParam());
		if (formUpload) {
			formUpload.action = eXp.connector + eXp.command.uploadFile + "?" + param;
			formUpload.submit();
			
		}
		eXp.stopUpload = false;
		K.set.timeout({
			until: function() {return eXp.stopUpload;},
			amount: 3,
			method: function() {
				var connector = eXp.connector + eXp.command.controlUpload;
				var param = eXp.buildParam("action=progress", "uploadId=" + formUpload.id, "currentFolder=/", buildXParam());
				eXp.sendRequest(connector, param, function(iXML) {alert(iXML)});
			}
		});
	}