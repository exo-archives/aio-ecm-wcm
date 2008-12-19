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
	var searchBox = document.getElementById("siteSearchBox");
  var keyWordInput = eXo.core.DOMUtil.findFirstDescendantByClass(searchBox, "input", "keyword");
  var queryString = location.search;  
  var portalParam = queryString.split('&')[0];
  var keyword = queryString.substring((portalParam + "keyword=").length +1);
  if (keyword != undefined && keyword.length != 0) {
  	keyWordInput.value = unescape(keyword); 
  }
}

eXo.core.Browser.addOnLoadCallback("keepKeywordOnBoxSearch", keepKeywordOnBoxSearch);