window.wcm = function() {}
wcm.insertCSSFromTextArea2FCK = function(Instance, ContentCSS) {
	if (!Instance) return;
	
	var eContentCSS = document.getElementById(ContentCSS);
	var sContentCSSId = ContentCSS + "_Inline";
	var count = 1;
	eContentCSS.onblur = updateStyle;
	
	function updateStyle() {
		var sValue = eContentCSS.value;
		
		var iDoc = FCKeditorAPI.Instances[Instance].EditorWindow.document;
		if (eXo.core.Browser.isFF()) { //for FF			
			var eHead = iDoc.getElementsByTagName("head")[0];
			var eStyle = iDoc.getElementById(sContentCSSId);
			if (eStyle) {
				eHead.removeChild(eStyle);
			}
			eStyle = iDoc.createElement("style");
			eStyle.setAttribute("type", "text/css");
			eStyle.setAttribute("id", sContentCSSId);
			eStyle.innerHTML = sValue;
			eHead.appendChild(eStyle);
		} else {
			var eHtml = iDoc.getElementsByTagName('html')[0];
			var strHTML = eHtml.innerHTML;
			var eStyle = iDoc.getElementById(sContentCSSId);
			if (eStyle) {
				strHTML = strHTML.replace(eStyle.outerHTML, '');
			}
			strHTML += '<style id="' + sContentCSSId + '">' + sValue + '</style>';

			iDoc.open();
			iDoc.write(strHTML);
			iDoc.close();
		}		
	};
	
	(function checkFCKEditorAPI() {
		if (count <= 5) {
			try {
				updateStyle();
				if (updateStyle.time) {
					clearTimeout(updateStyle.time);
					updateStyle.time = null;
				}
			} catch(e) {
				count++;
				updateStyle.time = setTimeout(checkFCKEditorAPI, 500);
			}
		}
	})();
}

Utils = function(){
	Utils.prototype.removeQuickeditingBlock = function(portletID, quickEditingBlockId) {
		var presentation = document.getElementById(portletID);
		var parentNode = presentation.parentNode;
		var quickEditingBlock = document.getElementById(quickEditingBlockId);
		if(quickEditingBlock != null) {
			quickEditingBlock.parentNode.removeChild(quickEditingBlock);
		}
	};
		
	Utils.prototype.insertQuickeditingBlock = function(portletID, quickEditingBlockId) {
		var presentation = document.getElementById(portletID);		
		var parentNode = presentation.parentNode;
		var fistChild = eXo.core.DOMUtil.getChildrenByTagName(parentNode, "div")[0];
		if (fistChild.id == quickEditingBlockId) {
			var quickEditingBlock = document.getElementById(quickEditingBlockId);
			quickEditingBlock.parentNode.removeChild(quickEditingBlock);
		}
		var quickEditingBlock = document.getElementById(quickEditingBlockId);		
		if(quickEditingBlock != null) {
			parentNode.insertBefore(quickEditingBlock, presentation);
		}
	};
}
eXo.wcm = new Utils();

function showObject(obj) {
	var element = eXo.core.DOMUtil.findNextElementByTagName(obj, "div");
	if (!element.style.display || element.style.display == 'none') {
		element.style.display = 'block';
	} else {
		element.style.display = 'none';
	}
}

function getHostName() {
	var parentLocation = window.parent.location;
	return parentLocation.href.substring(0, parentLocation.href.indexOf(parentLocation.pathname));
}

function getRuntimeContextPath() {
	return getHostName() + eXo.env.portal.context + '/' + eXo.env.portal.accessMode + '/' + eXo.env.portal.portalName + '/';
}
/*--------------------------------------SEARCH------------------------------------*/
function getKeynum(event) {
  var keynum = false ;
  if(window.event) { /* IE */
    keynum = window.event.keyCode;
    event = window.event ;
  } else if(event.which) { /* Netscape/Firefox/Opera */
    keynum = event.which ;
  }
  if(keynum == 0) {
    keynum = event.keyCode ;
  }
  return keynum ;
}

function quickSearch(resultPageURI) {
  var searchBox = document.getElementById("siteSearchBox");
  var keyWordInput = eXo.core.DOMUtil.findFirstDescendantByClass(searchBox, "input", "keyword");
  var keyword = keyWordInput.value;
  var resultPageURIDefault = "searchResult";
  var params = "portal=" + eXo.env.portal.portalName + "&keyword=" + keyword;
  var baseURI = getHostName() + eXo.env.portal.context + "/" + eXo.env.portal.accessMode + "/" + eXo.env.portal.portalName; 
  if (resultPageURI != undefined) {
		baseURI = baseURI + "/" + resultPageURI; 
  } else {
		baseURI = baseURI + "/" + resultPageURIDefault;  
  }
  window.location = baseURI + "?" + params;
}

function quickSearchOnEnter(event, resultPageURI) {
  var keyNum = getKeynum(event);
  if (keyNum == 13) {
    quickSearch(resultPageURI);
  }
}

function search(comId) {
	var searchForm = document.getElementById(comId);
	var inputKey = eXo.core.DOMUtil.findDescendantById(searchForm, "keywordInput");
	searchForm.onsubmit = function() {return false;};
	inputKey.onkeypress = function(event) {
		var keyNum = getKeynum(event);
		if (keyNum == 13) {
			var searchButton = eXo.core.DOMUtil.findFirstDescendantByClass(this.form, "div", "SearchButton");
			searchButton.onclick();
  	  	}		
	}
}	

function keepKeywordOnBoxSearch() {
	var queryRegex = /^portal=\w+&keyword=\w+/;
	var searchBox = document.getElementById("siteSearchBox");
	var keyWordInput = eXo.core.DOMUtil.findFirstDescendantByClass(searchBox, "input", "keyword");
	var queryString = location.search.substring(1);  
	if (!queryString.match(queryRegex)) { return; }
	var portalParam = queryString.split('&')[0];
	var keyword = queryString.substring((portalParam + "keyword=").length +1);
	if (keyword != undefined && keyword.length != 0) {
		keyWordInput.value = unescape(keyword); 
	}
}

eXo.core.Browser.addOnLoadCallback("keepKeywordOnBoxSearch", keepKeywordOnBoxSearch);

/*------------------Overrite method eXo.webui.UIPopup.init to show popup display center-------------------------------*/
UIPopupWindow.prototype.init = function(popupId, isShow, isResizable, showCloseButton, isShowMask) {
	var DOMUtil = eXo.core.DOMUtil ;
	this.superClass = eXo.webui.UIPopup ;
	var popup = document.getElementById(popupId) ;
	var portalApp = document.getElementById("UIPortalApplication") ;
	if(popup == null) return;
	popup.style.visibility = "hidden" ;
	
	//TODO Lambkin: this statement create a bug in select box component in Firefox
	//this.superClass.init(popup) ;
	var contentBlock = DOMUtil.findFirstDescendantByClass(popup, 'div' ,'PopupContent');
	if((eXo.core.Browser.getBrowserHeight() - 100 ) < contentBlock.offsetHeight) {
		contentBlock.style.height = (eXo.core.Browser.getBrowserHeight() - 100) + "px";
	}
	var popupBar = DOMUtil.findFirstDescendantByClass(popup, 'div' ,'PopupTitle') ;

	popupBar.onmousedown = this.initDND ;
	
	if(isShow == false) {
		this.superClass.hide(popup) ;
		if(isShowMask) eXo.webui.UIPopupWindow.showMask(popup, false) ;
	} 
	
	if(isResizable) {
		var resizeBtn = DOMUtil.findFirstDescendantByClass(popup, "div", "ResizeButton");
		resizeBtn.style.display = 'block' ;
		resizeBtn.onmousedown = this.startResizeEvt ;
		portalApp.onmouseup = this.endResizeEvt ;
	}
	
	popup.style.visibility = "hidden" ;
	if(isShow == true) {
		var iframes = DOMUtil.findDescendantsByTagName(popup, "iframe") ;
		if(iframes.length > 0) {
			setTimeout("eXo.webui.UIPopupWindow.show('" + popupId + "'," + isShowMask + ")", 500) ;
		} else {
		if(popup.offsetHeight == 0){
			setTimeout("eXo.webui.UIPopupWindow.show('" + popupId + "'," + isShowMask + ")", 500) ;
			return ;
		}
			this.show(popup, isShowMask) ;
		}
	}
} ;
/*----------------------------------------------End of overrite-------------------------------------------------------*/
function initCheckedRadio(id) {
	eXo.core.Browser.chkRadioId = id;
};

function initCondition(formid){
	var formElement = document.getElementById(formid);
	var radioboxes = [];
	for(var i=0; i < formElement.elements.length;i++){
		if(formElement.elements[i].type=="radio") radioboxes.push(formElement.elements[i]);
	}
	var i = radioboxes.length;
	while(i--){
		radioboxes[i].onclick = chooseCondition;
	}
	if(eXo.core.Browser.chkRadioId && eXo.core.Browser.chkRadioId != "null"){
		var selectedRadio = document.getElementById(eXo.core.Browser.chkRadioId);
	} else{		
		var selectedRadio = radioboxes[0];
	}
	var itemSelectedContainer = eXo.core.DOMUtil.findAncestorByClass(selectedRadio,"ContentSearchForm");
	var itemContainers = eXo.core.DOMUtil.findDescendantsByClass(selectedRadio.form,"div","ContentSearchForm");
	for(var i=1;i<itemContainers.length;i++){
		setCondition(itemContainers[i],true);
	}
	enableCondition(itemSelectedContainer);
}

function chooseCondition() {
	var me = this;
	var hiddenField = eXo.core.DOMUtil.findFirstDescendantByClass(me.form,"input","hidden");
	hiddenField.value = me.id;
	var itemSelectedContainer = eXo.core.DOMUtil.findAncestorByClass(me,"ContentSearchForm");
	var itemContainers = eXo.core.DOMUtil.findDescendantsByClass(me.form,"div","ContentSearchForm");
	for(var i=1;i<itemContainers.length;i++){
		setCondition(itemContainers[i],true);
	}
	enableCondition(itemSelectedContainer);
	window.wcm.lastCondition = itemSelectedContainer; 
};

function enableCondition(itemContainer) {
	if(window.wcm.lastCondition) setCondition(window.wcm.lastCondition,true);
	setCondition(itemContainer,false);
};

function setCondition(itemContainer,state) {
	var domUtil = eXo.core.DOMUtil;
	var action = domUtil.findDescendantsByTagName(itemContainer,"img");
	if(action && (action.length > 0)){
		for(var i = 0; i < action.length; i++){
			if(state) {
				action[i].style.visibility = "hidden";
			}	else {
				action[i].style.visibility = "";	
			}	
		}
	}
	var action = domUtil.findDescendantsByTagName(itemContainer,"input");
	if(action && (action.length > 0)){
		for(i = 0; i < action.length; i++){
			if(action[i].type != "radio") action[i].disabled = state;
		}
	}
	var action = domUtil.findDescendantsByTagName(itemContainer,"select");
	if(action && (action.length > 0)){
		for(i = 0; i < action.length; i++){
			action[i].disabled = state;
		}
	}
};
function removeCondition() {
	
};

function setHiddenValue() {
	var inputHidden = document.getElementById("checkedRadioId");
	if(eXo.core.Browser.chkRadioId == "null") {
		inputHidden.value = "name";
		document.getElementById("name").checked = true;
	} else {
		inputHidden.value = eXo.core.Browser.chkRadioId; 
		document.getElementById(eXo.core.Browser.chkRadioId).checked = true;
	}
}

function showHideOrderBy() {
	var formObj = document.getElementById('UIViewerManagementForm');
	var viewerModeObj = formObj['ViewerMode'];
	var orderXXX = eXo.core.DOMUtil.findDescendantsByClass(formObj, 'tr', 'OrderBlock');			
	viewerModeObj[0].onclick = function() {
		for (var i = 0; i < orderXXX.length; i++) {
			orderXXX[i].style.display = '';
		}
	}
	viewerModeObj[1].onclick = function() {
		for (var i = 0; i < orderXXX.length; i++) {
			orderXXX[i].style.display = 'none';
		}
	}
}  

function showPopupMenu(obj) {
	if(!obj) return; 
	var mnuItemContainer = eXo.core.DOMUtil.findNextElementByTagName(obj, "div");
	var uiNavi = document.getElementById('PortalNavigationTopContainer');
	if(mnuItemContainer && mnuItemContainer.style.display != "block") {
		mnuItemContainer.style.display = 'block';
		if(eXo.core.Browser.browserType == 'ie')   uiNavi.style.position = "static";
		mnuItemContainer.onmouseout = function(){
			obj.Timeout = setTimeout(function() {
				mnuItemContainer.style.display = 'none';
				mnuItemContainer.onmouseover = null;
				mnuItemContainer.onmouseout = null;
				if(eXo.core.Browser.browserType == 'ie') uiNavi.style.position = "relative";
			},1*200);
		}

		mnuItemContainer.onmouseover = function() {
			if(obj.Timeout) clearTimeout(obj.Timeout);
			obj.Timeout = null;
		}
		obj.onmouseout = mnuItemContainer.onmouseout;	
		mnuItemContainer.style.width = mnuItemContainer.offsetWidth + 'px';
		//mnuItemContainer.style.width = mnuItemContainer.offsetWidth - parseInt(DOMUtil.getStyle(mnuItemContainer, "borderLeft")) - parseInt(DOMUtil.getStyle(mnuItemContainer, "borderRight")) + 'px';
	}
}	