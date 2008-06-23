function getElementByClassName(sClassName) {
	var aElements = document.getElementsByTagName('*');
	var iLength = aElements.length;
	var aResults = [];
	for (var i = 0; i < iLength; i++) {
		if (aElements[i].className.indexOf(sClassName) >= 0) {
			aResults.push(aElements[i]);
		}
	}
	if (aResults.length == 1)
		return aResults[0];
	else 
		return aResults;
}

function showSetting(sClassName) {
	var obj = getElementByClassName(sClassName)[0];
	var sStatus = obj.style.display;
	if (sStatus != 'block') {
		obj.style.display = 'block';
	} else { 
		obj.style.display = 'none';
	}
	elementResize();
}

function showContextMenu(sClassName, e) {
	var obj = getElementByClassName(sClassName);
	obj.style.left = (e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft) + 'px';
	obj.style.top = (e.clientY + document.body.scrollTop + document.documentElement.scrollTop) + 'px';
	obj.style.display = 'Block';
	return false;
}

function hideContextMenu() {
	var aObjects = getElementByClassName('ContextMenuContainer');
	iLength = aObjects.length;
	for (var i = 0; i < iLength; i++) {
		aObjects[i].style.display = 'none';
	}
}

function openTree(obj) {
	var oNodeGroup = document.getElementById(obj.id + '-gr/');
	var oNodeOpen = document.getElementById(obj.id + '-ec');
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
			oNodeOpen.style.background = 'url(img/TreeMinus18x16.gif) no-repeat 5px top';		
			oNodeExpand.className += ' ' + sOpenType;
		} else { 
			oNodeGroup.style.display = 'none';
			oNodeOpen.style.background = 'url(img/TreePlus18x16.gif) no-repeat 5px top';
			oNodeExpand.className = oNodeExpand.className.replace(sOpenType, '');
		}
	} else {
		oNodeOpen.style.background = 'url(img/TreeMinus18x16.gif) no-repeat 5px top';
		oNodeExpand.className += ' ' + sOpenType;
	}
}