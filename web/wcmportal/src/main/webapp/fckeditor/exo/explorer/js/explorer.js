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
	var oNodeGroup = document.getElementById(obj.id + 'group/');
	var oNodeOpen = obj.parentNode;
	var oNodeExpand = document.getElementById(obj.id);
	var sClassName = oNodeExpand.className;
	var sOpenType = sClassName.substring(0, sClassName.indexOf('16x16Icon') + 9).replace('nt','Collapsent');
	var sStatus = '';
	if (oNodeGroup != null) {
		sStatus = oNodeGroup.style.display;
		if (sStatus == '') {
			sStatus = 'block';	
		}
		if (sStatus != 'block') {
			oNodeGroup.style.display = 'block';
			oNodeOpen.style.background = 'url(images/TreeMinus18x16.gif) no-repeat 5px top';		
			oNodeExpand.className += ' ' + sOpenType;
		} else { 
			oNodeGroup.style.display = 'none';
			oNodeOpen.style.background = 'url(images/TreePlus18x16.gif) no-repeat 5px top';
			oNodeExpand.className = oNodeExpand.className.replace(sOpenType, '');
		}
	} else {
		oNodeOpen.style.background = 'url(images/TreeMinus18x16.gif) no-repeat 5px top';
		oNodeExpand.className += ' ' + sOpenType;
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