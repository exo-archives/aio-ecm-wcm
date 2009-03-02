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

/*------------------the top toolbar---------------*/
function findPreviousElementByClass(element, clazz) {
	var previousElement = element.previousSibling ;
	while (previousElement != null) {
		if (eXo.core.DOMUtil.hasClass(previousElement, clazz)) return previousElement;
		previousElement = previousElement.previousSibling ;
	}
	return null ;
}

function viewMoreActions(viewMoreObj) {		
	var moreActionsMenu = eXo.core.DOMUtil.findNextElementByTagName(viewMoreObj, "div");
	var onOffAction = findPreviousElementByClass(viewMoreObj, "BoxOnOff");
	moreActionsMenu.style.right = onOffAction.offsetWidth + "px";
	var objToolbarPortlet = eXo.core.DOMUtil.findAncestorByClass(moreActionsMenu, "UISiteAdministrationPortlet");
	if (document.all) {
		if(document.getElementById(moreActionsMenu.id + "1")) eXo.core.DOMUtil.removeElement(document.getElementById(moreActionsMenu.id + "1").parentNode.parentNode);
		var tmp = document.createElement("div");
		tmp.className = "UISiteAdminToolBar";
		var id = moreActionsMenu.id + 1;
		moreActionsMenu = moreActionsMenu.cloneNode(true);
		moreActionsMenu.id = id;
		tmp.appendChild(moreActionsMenu);
		var tmp1 = document.createElement("div");
		tmp1.className = "UISiteAdministrationPortlet";
		tmp1.appendChild(tmp);
		tmp1.style["position"] = "static";
		document.body.appendChild(tmp1);
		
		moreActionsMenu.style.top = viewMoreObj.offsetHeight + "px";
		moreActionsMenu.style.display = "block";
	} else {
		moreActionsMenu.style.display = "block";
	}
	moreActionsMenu.onmouseover = function(){
		if(window.hiddenmenu) clearTimeout(window.hiddenmenu);
	}
	moreActionsMenu.onmouseout = function(){
		window.hiddenmenu = setTimeout("hideElement('" + this.id + "');",100);
	}
	if(window.hiddenmenu) clearTimeout(window.hiddenmenu);
}

function hideMoreActions(viewMoreObj) {
	var moreActionsMenu = eXo.core.DOMUtil.findNextElementByTagName(viewMoreObj, "div");
	var id = moreActionsMenu.id;
	if(document.all) id += 1;
	window.hiddenmenu = setTimeout("hideElement('" + id + "');",100);
}

function hideElement(element) {
	var element = (typeof(element) == "string")?document.getElementById(element):element;
	element.style.display = "none";
}

function showToptoolbarNavs(exoLogo) {
	var navs = eXo.core.DOMUtil.findAncestorByClass(exoLogo, "StartMenuContainer");
	eXo.portal.UIExoStartMenu.buildMenu(navs);		
}

UIExoStartMenu.prototype.onMenuItemOut = function(event) {
	var portalNav = document.getElementById("PortalNavigationTopContainer");
	if(eXo.core.Browser.browserType == "ie") portalNav.style.position = "relative";
	this.className = eXo.portal.UIExoStartMenu.itemStyleClass ;
	if (this.menuItemContainer) {
    eXo.portal.UIExoStartMenu.superClass.pushHiddenContainer(this.menuItemContainer.id) ;
    eXo.portal.UIExoStartMenu.superClass.popVisibleContainer() ;
    eXo.portal.UIExoStartMenu.superClass.setCloseTimeout() ;
	}
};

UIExoStartMenu.prototype.onMenuItemOver = function(event) {
	this.className = eXo.portal.UIExoStartMenu.itemOverStyleClass ;
	if (this.menuItemContainer) {
		var menuItemContainer = this.menuItemContainer ;
		menuItemContainer.style.display = "block" ;
		menuItemContainer.style.visibility = "" ;
		var x = this.offsetWidth ;
		var posRight = eXo.core.Browser.getBrowserWidth() - eXo.core.Browser.findPosX(this) - this.offsetWidth ; 
	  var rootX = (eXo.core.I18n.isLT() ? eXo.core.Browser.findPosX(this) : posRight) ;
		if (x + menuItemContainer.offsetWidth + rootX > eXo.core.Browser.getBrowserWidth()) {
    	x -= (menuItemContainer.offsetWidth + this.offsetWidth) ;
	  }
	 	if(eXo.core.I18n.isLT()) {
	 		if(eXo.core.Browser.isIE6()) x -= 10 ;
	 		menuItemContainer.style.left = x + "px" ;
	 	}	else menuItemContainer.style.right =  x + "px" ;
		eXo.portal.UIExoStartMenu.createSlide(this);
    eXo.portal.UIExoStartMenu.superClass.pushVisibleContainer(this.menuItemContainer.id) ;
    
    var y ;
	 	var browserHeight = eXo.core.Browser.getBrowserHeight() ;
	 	var hline = eXo.core.DOMUtil.findFirstChildByClass(this.parentNode, "div", "HLineSeparator") ;
	 	
    if(hline) {
		 	var posParent = eXo.portal.UIExoStartMenu.findPositionParent(this) ;
		 	var objTop = eXo.core.Browser.findPosY(this) ;
		 	y = objTop - eXo.core.Browser.findPosY(posParent) ;
		 	if(objTop + menuItemContainer.offsetHeight >= browserHeight) {
				y += (this.offsetHeight - menuItemContainer.offsetHeight) ;
			 	if(y + (eXo.core.Browser.findPosY(posParent) - document.documentElement.scrollTop) < 0) {
				 	var objBottom = objTop + this.offsetHeight ;
			 		y += (browserHeight - objBottom) - (browserHeight - menuItemContainer.offsetHeight)/2 + document.documentElement.scrollTop ;
			 	}
		 	}
	 	} else {
			var parentMenu = eXo.core.DOMUtil.findAncestorByClass(this, "MenuItemContainer") ;
			var blockMenu = eXo.core.DOMUtil.findAncestorByClass(this, "BlockMenu") ;
	 		var objTop = eXo.core.Browser.findPosY(this) ;
	 		y = objTop - eXo.core.Browser.findPosY(parentMenu) - blockMenu.scrollTop ;
	 		if(y + menuItemContainer.offsetHeight + 15 > browserHeight) {
	 			y += (this.offsetHeight - menuItemContainer.offsetHeight) ;
	 			if(y <= 0) y = 1 ;
	 		}
	 	}
		menuItemContainer.style.top = y + "px" ;
	}

  var portalNav = document.getElementById("PortalNavigationTopContainer");
  if(eXo.core.Browser.browserType == "ie") portalNav.style.position = "static";
};

/*--------------------scroll toptoolbar----------------------*/
function ScrollTopToolbar() {
};

ScrollTopToolbar.prototype.getChildrenByClass = function(root, clazz) {
	var list = [];
	var children = root.childNodes;
	var len = children.length;
	for(var i = 0; i < len; i++){
		if(eXo.core.DOMUtil.hasClass(children[i],clazz))	list.push(children[i]);
	}
	return list.reverse();
};

ScrollManager.prototype.loadItems = function(root, clazz) {
	this.elements.clear();
	this.elements.pushAll(eXo.wcm.ScrollTopToolbar.getChildrenByClass(root, clazz));
};

ScrollManager.prototype.checkAvailableArea = function(maxSpace) {
	if (!maxSpace) maxSpace = this.mainContainer.offsetWidth - 200;
	var elementsSpace = 0;
	var margin = 0;
	var length =  this.elements.length;
	for (var i = 0; i < length; i++) {
		elementsSpace += this.getElementSpace(this.elements[i]);
		if (i+1 < length) margin = this.getElementSpace(this.elements[i+1]) / 3;
		else margin = this.margin;
		if (elementsSpace + margin < maxSpace) {
			this.elements[i].isVisible = true;
			this.lastVisibleIndex = i;
		} else {
			this.elements[i].isVisible = false;
		}
	}
} ;

ScrollTopToolbar.prototype.execute = function() {
	var obj = eXo.wcm.ScrollTopToolbar;
	obj.manager.checkAvailableArea();	
	obj.manager.renderItems();
};

ScrollManager.prototype.renderItems = function() {
	var obj = eXo.wcm.ScrollTopToolbar;
	for (var i = 0; i < this.elements.length; i++) {
		if (this.elements[i].isVisible) {
			this.elements[i].style.display = "block";
			if (this.elements[i].style.display == "block" && !eXo.core.DOMUtil.hasClass(this.elements[i], "SeparationIcon")) {
				var mockClazz = this.elements[i].className.replace('HozIcon', '').replace('TopToolbarMenuItem', '').replace(/\s*/g,'');		
				for (var j = 0; j < obj.manager.moreActionsContainer.childNodes.length; j++) {					
					if (obj.manager.moreActionsContainer.childNodes[j] && eXo.core.DOMUtil.hasClass(obj.manager.moreActionsContainer.childNodes[j], mockClazz)) {
						obj.manager.moreActionsContainer.childNodes[j].style.display = "none";
					}
				}
			}
		} else {
			this.elements[i].style.display = "none";
			if (this.elements[i].style.display == "none" && !eXo.core.DOMUtil.hasClass(this.elements[i], "SeparationIcon")) {
				var mockClazz = this.elements[i].className.replace('HozIcon', '').replace('TopToolbarMenuItem', '').replace(/\s*/g,'');
				for (var j = 0; j < obj.manager.moreActionsContainer.childNodes.length; j++) {
					if (obj.manager.moreActionsContainer.childNodes[j] && eXo.core.DOMUtil.hasClass(obj.manager.moreActionsContainer.childNodes[j], mockClazz)) {
						obj.manager.moreActionsContainer.childNodes[j].style.display = "block";
					}
				}
			}
		}
	}
};

ScrollTopToolbar.prototype.init = function() {
	var obj = eXo.wcm.ScrollTopToolbar;
	obj.manager = eXo.portal.UIPortalControl.newScrollManager("UISiteAdministrationPortlet");
	var topToolbarObj = document.getElementById("UISiteAdministrationPortlet");
	obj.manager.mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(topToolbarObj, "div", "UISiteAdminToolBar");
	obj.manager.moreActionsContainer = eXo.core.DOMUtil.findDescendantById(obj.manager.mainContainer, "MoreActionsMenu");

	obj.manager.loadItems(obj.manager.mainContainer, "TopToolbarMenuItem");	
	obj.execute();
	obj.manager.initFunction = obj.execute;
};

eXo.wcm.ScrollTopToolbar = new ScrollTopToolbar();
eXo.core.Browser.addOnLoadCallback("resizeTopToolbar", eXo.wcm.ScrollTopToolbar.init);

/*------------------end the top toolbar--------------------*/

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