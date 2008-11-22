window.wcm = function() {}
wcm.insertCSSFromTextArea2FCK = function(Instance, ContentCSS) {
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

function showObject(div) {
	if (!div.style.display || div.style.display == 'none') div.style.display = 'block';
	else div.style.display = 'none';
}

function getHostName() {
	var parentLocation = window.parent.location;
	return parentLocation.href.substring(0, parentLocation.href.indexOf(parentLocation.pathname));
}

function validateUser() {

	var user = eXo.env.portal.userName;
	var rootObj = document.getElementById("classic-access");
	var loginContentObj = eXo.core.DOMUtil.findFirstDescendantByClass(rootObj, "div", "LoginContent");
	var welcomeObj = eXo.core.DOMUtil.findFirstDescendantByClass(rootObj, "div", "Welcome");
	var languageObj = eXo.core.DOMUtil.findFirstDescendantByClass(rootObj, "a", "LanguageIcon");
	var logXXXObj = eXo.core.DOMUtil.findPreviousElementByTagName(languageObj, "a");

	if (user != "null") {
		welcomeObj.style.display = "block";
		welcomeObj.innerHTML = "Welcome: <span>" + user + "</span>";		
		logXXXObj.innerHTML = "Logout";
		if (eXo.core.DOMUtil.hasClass(logXXXObj, "LoginIcon")) {
			eXo.core.DOMUtil.removeClass(logXXXObj, "LoginIcon");
			eXo.core.DOMUtil.addClass(logXXXObj, "LogoutIcon");
		}
		logXXXObj.onclick = eXo.portal.logout();;
	} else {
		welcomeObj.style.display = "none";
		if (eXo.core.DOMUtil.hasClass(logXXXObj, "LogoutIcon")) {
			eXo.core.DOMUtil.removeClass(logXXXObj, "LogoutIcon");
			eXo.core.DOMUtil.addClass(logXXXObj, "LoginIcon");
		}
		logXXXObj.innerHTML = "Login";
		logXXXObj.onclick = function() { if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ShowLoginForm', true)); };
	}

	languageObj.onclick = function () { if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ChangeLanguage', true)); }
}

eXo.core.Browser.addOnLoadCallback("validateUser", validateUser);