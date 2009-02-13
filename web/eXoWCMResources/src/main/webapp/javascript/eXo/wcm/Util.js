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
	var onOffAction = findPreviousElementByClass(viewMoreObj, "OnOffAction");
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

/*---------------------- => will remmove when WCM use Portal 2.5.2 ---------------------------*/
UIExoStartMenu.prototype.buildMenu = function(popupMenu) {
	if(typeof(popupMenu) == "string") popupMenu = document.getElementById(popupMenu) ;
	
  var blockMenuItems = eXo.core.DOMUtil.findDescendantsByClass(popupMenu, "div", this.containerStyleClass) ;
  for (var i = 0; i < blockMenuItems.length; i++) {
    if (!blockMenuItems[i].id) blockMenuItems[i].id = Math.random().toString() ;
		blockMenuItems[i].resized = false ;
  }
	
  var menuItems = eXo.core.DOMUtil.findDescendantsByClass(popupMenu, "div", this.itemStyleClass) ;
  for(var i = 0; i < menuItems.length; i++) {
		var menuItemContainer = eXo.core.DOMUtil.findFirstChildByClass(menuItems[i], "div", "MenuItemContainer") ;
		if (menuItemContainer) menuItems[i].menuItemContainer = menuItemContainer ;
		
		menuItems[i].onmouseover = this.onMenuItemOver ; 
		menuItems[i].onmouseout = this.onMenuItemOut ;

    var labelItem = eXo.core.DOMUtil.findFirstDescendantByClass(menuItems[i], "div", "LabelItem") ;
    var link = eXo.core.DOMUtil.findDescendantsByTagName(labelItem, "a")[0] ;
    this.superClass.createLink(menuItems[i], link) ;
  }
};

UIExoStartMenu.prototype.onMenuItemOver = function(event) {
	this.className = eXo.portal.UIExoStartMenu.itemOverStyleClass ;
	this.style.position = "relative" ;
	if (this.menuItemContainer) {
		
		var menuItemContainer = this.menuItemContainer ;
		var x = this.offsetWidth + this.offsetLeft ;
	  var rootX = eXo.core.Browser.findPosX(this) ;
		if (x + menuItemContainer.offsetWidth + rootX > eXo.core.Browser.getBrowserWidth()) {
	    	x -= (menuItemContainer.offsetWidth + this.offsetWidth) ;
	  }
	  if (eXo.core.Browser.isIE6()) x -= 10;
	 	menuItemContainer.style.left = x + "px" ;
		eXo.portal.UIExoStartMenu.createSlide(this);
    eXo.portal.UIExoStartMenu.superClass.pushVisibleContainer(this.menuItemContainer.id) ;
	
	}
};

UIExoStartMenu.prototype.createSlide = function(menuItem) {

		var menuItemContainer = menuItem.menuItemContainer ;
		menuItemContainer.style.display = "block" ;
		// fix width for menuContainer, only IE.
		if (!menuItemContainer.resized) setContainerSize(menuItemContainer);
		
	 	var blockMenu = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "BlockMenu") ;
		var parentMenu = blockMenu.parentNode;
		var topElement = eXo.core.DOMUtil.findFirstChildByClass(parentMenu, "div", "TopNavigator") ;
	 	var bottomElement = eXo.core.DOMUtil.findFirstChildByClass(parentMenu, "div", "BottomNavigator") ;

		var menuContainer = eXo.core.DOMUtil.findFirstDescendantByClass(blockMenu, "div", "MenuContainer") ;
		
		if (!menuContainer.id) menuContainer.id = "eXo" + new Date().getTime() + Math.random().toString().substring(2) ;
		
		var browserHeight = eXo.core.Browser.getBrowserHeight() ;
		if (menuContainer.offsetHeight + 64 > browserHeight) {
				var curentHeight = browserHeight - 64;
				blockMenu.style.height = curentHeight + "px" ;
				topElement.style.display = "block" ;
				bottomElement.style.display = "block" ;

				if(!menuContainer.curentHeight || (menuContainer.curentHeight != curentHeight)) {
					eXo.portal.UIExoStartMenu.initSlide(menuContainer, curentHeight) ;
				}
				topElement.onmousedown = function() {
					eXo.portal.UIExoStartMenu.scrollDown(menuContainer.id, curentHeight) ;
				};
				topElement.onmouseoup = function() {
					if (menuContainer.repeat) {
						clearTimeout(menuContainer.repeat) ;
						menuContainer.repeat = null ;
					}
				};
				topElement.onclick = function(event) {
					clearTimeout(menuContainer.repeat) ;
					menuContainer.repeat = null ;
					event = event || window.event ;
					event.cancelBubble = true ;
				};
				
				bottomElement.onmousedown = function() {
					eXo.portal.UIExoStartMenu.scrollUp(menuContainer.id, curentHeight) ;
				};
				bottomElement.onmouseoup = function() {
					if (menuContainer.repeat) {
						clearTimeout(menuContainer.repeat) ;
						menuContainer.repeat = null ;
					}
				};			
				bottomElement.onclick = function(event) {
					clearTimeout(menuContainer.repeat) ;
					menuContainer.repeat = null ;
					event = event || window.event ;
					event.cancelBubble = true ;
				};
	  } else {
			blockMenu.style.height = menuContainer.offsetHeight + "px" ;
			menuContainer.style.clip = "rect(0px 1280px auto auto)" ;
			menuContainer.curentHeight = null;
			menuContainer.style.position = "static";
			topElement.style.display = "none" ;
			bottomElement.style.display = "none" ;
	  }
		var Y = eXo.portal.UIExoStartMenu.getDimension(menuItem) ;
		if (Y != undefined)	menuItemContainer.style.top = Y + "px" ;
};

function setContainerSize(menuItemContainer) {
  var menuCenter = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "StartMenuML") ;
  var menuTop = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "StartMenuTL") ;
  var decorator = eXo.core.DOMUtil.findFirstDescendantByClass(menuTop, "div", "StartMenuTR") ;
  var menuBottom = menuTop.nextSibling ;
  while (menuBottom.className != "StartMenuBL") menuBottom = menuBottom.nextSibling ;
  var w = menuCenter.offsetWidth - decorator.offsetLeft ;
  menuTop.style.width = w + "px" ;
  menuBottom.style.width = w + "px" ;
  menuCenter.style.width = w + "px" ;
  menuItemContainer.resized = true ; 
};
/*---------------------- => will remmove when WCM use Portal 2.5.2 ---------------------------*/

/*--------------------scroll toptoolbar----------------------*/
function ScrollTopToolbar() { }

function getChildrenByClass(root, clazz) {
	var list = [];
	var children = root.childNodes;
	var len = children.length;
	for(var i = 0; i < len; i++){
		if(eXo.core.DOMUtil.hasClass(children[i],clazz))	list.push(children[i]);
	}
	return list.reverse();
}

ScrollManager.prototype.loadItems = function(root, clazz) {
	this.elements.clear();
	this.elements.pushAll(getChildrenByClass(root, clazz));
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
} 

ScrollTopToolbar.prototype.execute = function() {
	var obj = eXo.wcm.ScrollTopToolbar;
	obj.manager.checkAvailableArea();	
	obj.manager.renderItems();
}

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
}

ScrollTopToolbar.prototype.init = function() {
	var obj = eXo.wcm.ScrollTopToolbar;
	obj.manager = eXo.portal.UIPortalControl.newScrollManager("UISiteAdministrationPortlet");
	var topToolbarObj = document.getElementById("UISiteAdministrationPortlet");
	obj.manager.mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(topToolbarObj, "div", "UISiteAdminToolBar");
	obj.manager.moreActionsContainer = eXo.core.DOMUtil.findDescendantById(obj.manager.mainContainer, "MoreActionsMenu");

	obj.manager.loadItems(obj.manager.mainContainer, "TopToolbarMenuItem");	
	obj.execute();
	obj.manager.initFunction = obj.execute;
}

eXo.wcm.ScrollTopToolbar = new ScrollTopToolbar();
eXo.core.Browser.addOnLoadCallback("resizeTopToolbar", eXo.wcm.ScrollTopToolbar.init);

/*------------------end the top toolbar--------------------*/

//TODO this code need be removed after portal support this 
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
		} else if(eXo.core.Browser.browserType == 'ie' ) {		
		  var pageWinzard = DOMUtil.findDescendantById(portalApp, "UIPageCreationWizard") ;				  
		  if(pageWinzard) {
			setTimeout("eXo.webui.UIPopupWindow.show('" + popupId + "'," + isShowMask + ")", 1) ;
		  } else {
			this.show(popup, isShowMask) ;
		  }
		} else {			
			this.show(popup, isShowMask) ;
		}
	}
} ;

UIPopupWindow.prototype.show = function(popup, isShowMask, middleBrowser) {
	var DOMUtil = eXo.core.DOMUtil ;
	if(typeof(popup) == "string") popup = document.getElementById(popup) ;
	var portalApp = document.getElementById("UIPortalApplication") ;

	var maskLayer = DOMUtil.findFirstDescendantByClass(portalApp, "div", "UIMaskWorkspace") ;
	var zIndex = 0 ;
	var currZIndex = 0 ;
	if (maskLayer != null) {
		currZIndex = DOMUtil.getStyle(maskLayer, "zIndex") ;
		if (!isNaN(currZIndex) && currZIndex > zIndex) zIndex = currZIndex ;
	}
	var popupWindows = DOMUtil.findDescendantsByClass(portalApp, "div", "UIPopupWindow") ;
	var len = popupWindows.length ;
	for (var i = 0 ; i < len ; i++) {
		currZIndex = DOMUtil.getStyle(popupWindows[i], "zIndex") ;
		if (!isNaN(currZIndex) && currZIndex > zIndex) zIndex = currZIndex ;
	}
	if (zIndex == 0) zIndex = 2000 ;
	// We don't increment zIndex here because it is done in the superClass.show function
	if(isShowMask) eXo.webui.UIPopupWindow.showMask(popup, true) ;
	popup.style.visibility = "hidden" ;
	this.superClass.show(popup) ;
 	var offsetParent = popup.offsetParent ;
 	var scrollY = 0;
	if (window.pageYOffset != undefined) scrollY = window.pageYOffset;
	else if (document.documentElement != undefined) scrollY = document.documentElement.scrollTop;
	else	scrollY = document.body.scrollTop;
	//reference	
	if(offsetParent) {
		var middleWindow = (eXo.core.DOMUtil.hasClass(offsetParent, "UIPopupWindow") || eXo.core.DOMUtil.hasClass(offsetParent, "UIWindow"));	
		if (middleWindow) {			
			popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px" ;
		} 
		if (middleBrowser || !middleWindow) {
			popup.style.top = Math.ceil((eXo.core.Browser.getBrowserHeight() - popup.offsetHeight ) / 2) + scrollY + "px";
		}		
		if(eXo.core.DOMUtil.hasClass(offsetParent, "UIMaskWorkspace")) {			
			if(eXo.core.Browser.browerType=='ie') 	offsetParent.style.position = "relative";
			popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + scrollY + "px" ;
		}		
		// hack for position popup alway top in IE6.
		var checkHeight = popup.offsetHeight > 300; 

		if (document.getElementById("UIDockBar") && checkHeight) {
			popup.style.top = "6px";
		}
		popup.style.left = Math.ceil((offsetParent.offsetWidth - popup.offsetWidth) / 2) + "px" ;
	}
	if (eXo.core.Browser.findPosY(popup) < 0) popup.style.top = scrollY + "px" ;
  popup.style.visibility = "visible" ;
};

function initCheckedRadio(id) {
	eXo.core.Browser.chkRadioId = id;
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