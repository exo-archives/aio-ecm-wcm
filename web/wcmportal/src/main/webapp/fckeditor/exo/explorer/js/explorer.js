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

function showContextMenu(selection, event, element) {
	var oContextMenu = K('contextMenu');
	var oSelection = getElementsByClassPath(oContextMenu, selection)[0];
	oSelection.style.left = K.get.X(event) + "px";
	oSelection.style.top = K.get.Y(event) + "px";
	oSelection.style.display = "block";
	var oActions =  K.select({from: oSelection, where: "className like '%IconItem%'"});
	if (oActions && oActions.length) {
		for (var i = 0 ; i < oActions.length; i ++) {
			oActions[i].title = element.title;
		}
	}
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
		if (!eXp.store.data.Select) return;
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
	
	function showAddForm(event, element) {
		var popupContainer = K("PopupContainer").show();
		var formContainer = K("hideContainer").select({where: "className == 'AddFormContainer'"})[0];
		popupContainer.innerHTML = formContainer.innerHTML.replace(/\${idShort}/g, element.title);
		K("Mask").add({
			event: "click",
			listener: function() {
				K("PopupContainer").innerHTML = "";
				K("Mask").hide();
			}
		}).show();
	}
		
	function doAddForm() {
		var popupContainer = K("PopupContainer");
		var sFolderName = popupContainer.select({where: "tagName == 'INPUT' && name == 'fileName'"})[0].value;
		var sCurrentFolder = popupContainer.select({where: "tagName == 'INPUT' && name == 'hidden'"})[0].value;
		var connector = eXoPlugin.hostName + eXp.connector + 'createFolder';
		var param = eXp.buildParam(
					"type=" + eXp.resourceType,
					"currentFolder=" + sCurrentFolder,
					"newFolderName=" + sFolderName,
					"currentPortal=" + eXoPlugin.portalName,
					buildXParam()
				);
		eXp.sendRequest(
			connector,
			param,
			function(sXML) {
				/*
					var oNodes = eXp.getNodes(sXML, "Error")[0];
					var sErrorNumber = eXp.getNodeValue(oNodes, "number");
					var sErrorText = eXp.getNodeValue(oNodes, "text");
					if (sErrorNumber != '0') {
						alert(sErrorText);
					} else {
						showAddForm(sCurrentFolder + 'add/', false);
						getDir(sCurrentFolder);
					}
				*/
				getDir(eXp.store.currentNode);
				K("PopupContainer").innerHTML = "";
				K("Mask").hide();
			}
		);
	}
	
	function showUploadForm() {
		var uploadContainer = K("UploadContainer");
		var popupContainer = K("PopupContainer");
		popupContainer.style.display = "block";
		if (eXp.store.currentNode && eXp.store.currentNode.title) {
			var sPath = eXp.store.currentNode.title;
		} else var sPath = "/";
		popupContainer.innerHTML = uploadContainer.innerHTML.replace(/\${idShort}/, sPath);
		var iFrame = popupContainer.select({where: "className == 'iFrameUpload'"})[0];
		var iContent = K("iContentUpLoad").innerHTML;
		with (iFrame.contentWindow.document) {
			open();
			write(iContent);
			close();
		}
		K("Mask").add({
			event: "click",
			listener: function() {
				K("PopupContainer").innerHTML = "";
				K("Mask").hide();
			}
		}).show();
	}
	
	function uploadFile() {
		var popupContainer = K("PopupContainer");
		var iFrameUpload = popupContainer.select({where: "className == 'iFrameUpload'"})[0];
		var formUpload = iFrameUpload.contentWindow.document.getElementsByTagName("form")[0];
		uploadFile.id =  eXp.getID();
		var param = eXp.buildParam("uploadId=" + uploadFile.id, "currentFolder=" + eXp.store.currentFolder, buildXParam());
		if (formUpload) {
			formUpload.action = eXp.connector + eXp.command.uploadFile + "?" + param;
			formUpload.submit();
		}
		uploadFile.stopUpload = false;
		var uploadField = popupContainer.select({where: "className == 'UploadField'"})[0];
		uploadField.style.display = "none";
		var UploadInfo = popupContainer.select({where: "className like 'UploadInfo%'"})[0];
		UploadInfo.style.display = "";

		K.set.timeout({
			until: function() {return uploadFile.stopUpload},
			method: function() {
				var connector = eXp.connector + eXp.command.controlUpload;
				var param = eXp.buildParam("action=progress", "uploadId=" + uploadFile.id, "currentFolder=" + eXp.store.currentFolder, buildXParam());
				eXp.sendRequest(
					connector,
					param,
					function(iXML) {
						var oProgress = eXp.getSingleNode(iXML, "UploadProgress");
						var nPercent = eXp.getNodeValue(oProgress, "percent");
						var popupContainer = K("PopupContainer");
						var uploadInfo = popupContainer.select({where: "className like 'UploadInfo%'"})[0];
						var graphProgress = popupContainer.select({where: "className == 'GraphProgress'"})[0];
						var numberProgress = popupContainer.select({where: "className == 'NumberProgress'"})[0];
						if (nPercent * 1 < 100) {
							graphProgress.style.width = nPercent + "%";
							numberProgress.innerHTML = nPercent + "%";
							uploadFile.stopUpload = false;
							uploadInfo.className = "UploadInfo Abort";
						} else {
							graphProgress.style.width = 100 + "%";
							numberProgress.innerHTML = 100 + "%";
							uploadFile.stopUpload = true;
							uploadInfo.className = "UploadInfo Delete";
							var uploadAction = popupContainer.select({where: "className == 'UploadAction'"})[0];
							uploadAction.style.display = "";
						}
					}
				);
			}
		});
	}
	
	uploadFile.Abort = function() {
		var connector = eXp.connector + eXp.command.controlUpload;
		var param = eXp.buildParam("action=abort", "uploadId=" + uploadFile.id, "currentFolder=" + eXp.store.currentFolder, buildXParam());
		eXp.sendRequest(connector, param);
		uploadFile.stopUpload = true;
		var oPopupContainer = K("PopupContainer");
		K(oPopupContainer.select({where: "className like 'UploadInfo%'"})[0]).hide();
		K(oPopupContainer.select({where: "className == 'UploadField'"})[0]).show();
		var iFrame = oPopupContainer.select({where: "className == 'iFrameUpload'"})[0];
		var iContent = K("iContentUpLoad").innerHTML;
		with (iFrame.contentWindow.document) {
			open();
			write(iContent);
			close();
		}
	};
	
	uploadFile.Cancel = function() {
		var connector = eXp.connector + eXp.command.controlUpload;
		var param = eXp.buildParam("action=delete", "uploadId=" + uploadFile.id, "currentFolder=" + eXp.store.currentFolder, buildXParam());
		eXp.sendRequest(connector, param);
		K("PopupContainer").innerHTML = "";
		K("Mask").hide();
	};

	uploadFile.Delete = function() {
		var connector = eXp.connector + eXp.command.controlUpload;
		var param = eXp.buildParam("action=delete", "uploadId=" + uploadFile.id, "currentFolder=" + eXp.store.currentFolder, buildXParam());
		eXp.sendRequest(connector, param);
		var oPopupContainer = K("PopupContainer");
		K(oPopupContainer.select({where: "className == 'UploadAction'"})[0]).hide();
		K(oPopupContainer.select({where: "className like 'UploadInfo%'"})[0]).hide();
		K(oPopupContainer.select({where: "className == 'UploadField'"})[0]).show();
	};

	uploadFile.Save = function() {
		var connector = eXp.connector + eXp.command.controlUpload;
		var nodeName = K("PopupContainer").select({where: "nodeName == 'INPUT' && name == 'fileName'"})[0];
		var param = eXp.buildParam("action=save", "uploadId=" + uploadFile.id, "fileName=" + nodeName.value, "currentFolder=" + eXp.store.currentFolder, buildXParam());
		eXp.sendRequest(connector, param);
		K("PopupContainer").innerHTML = "";
		K("Mask").hide();
	};