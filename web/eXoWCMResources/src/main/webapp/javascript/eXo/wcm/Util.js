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

eXo.core.Browser.addOnLoadCallback('getNavigations', getNavigations);
function getNavigations() {
	var serviceUrl = getHostName() + '/portal/rest/wcmNavigation/getPortalNavigations?portalName=' + eXo.env.portal.portalName + '&language=en';
	var navigation = ajaxAsyncGetRequest(serviceUrl, false);
	navigation = navigation.substring(1, navigation.length - 1);
	navigation = navigation.replace('"navigations":', '');
	window.navigations = eval(navigation);
}

/*----------------------------Login web content-------------------------------*/
function validateUser() {
	var baseURL;
	var gettingRemoteUserFullRequest;

	var gettingRemoteUserRestRequest = "/portal/rest/organization/getCurrentUserID";
	var gettingBundleMessageRestRequest = "/portal/rest/resourceBundle/getString/en/";

	var loginBundleKey  = "ClassicLogin.LoginContent.Login";
	var logoutBundleKey  = "ClassicLogin.LoginContent.Logout";
	var welcomeBundleKey  = "ClassicLogin.LoginContent.Welcome";

	with (window.location) {
			baseURL = protocol + "//" + hostname + ":" + port;
	}
	gettingRemoteUserFullRequest = baseURL + gettingRemoteUserRestRequest;	

	var user = ajaxAsyncGetRequest(gettingRemoteUserFullRequest, false);

	var loginBundle = ajaxAsyncGetRequest(baseURL + gettingBundleMessageRestRequest + loginBundleKey, false);
	var logutBundle = ajaxAsyncGetRequest(baseURL + gettingBundleMessageRestRequest + logoutBundleKey, false);
	var welcomeBundle = ajaxAsyncGetRequest(baseURL + gettingBundleMessageRestRequest + welcomeBundleKey, false);

	var classicLoginObj = document.getElementById("classic-access");
	var loginContentObj = eXo.core.DOMUtil.findFirstDescendantByClass(classicLoginObj, "div", "LoginContent");
	var welcomeObj = eXo.core.DOMUtil.findFirstDescendantByClass(loginContentObj, "div", "Welcome");	
	var languageObj = eXo.core.DOMUtil.findFirstDescendantByClass(loginContentObj, "a", "LanguageIcon");
	var logXXXObj = eXo.core.DOMUtil.findPreviousElementByTagName(languageObj, "a");
	if (user != 'null') {
		welcomeObj.innerHTML = welcomeBundle + ": <span>" + user + "</span>";		
		logXXXObj.innerHTML = logutBundle;		
		if (eXo.core.DOMUtil.hasClass(logXXXObj, "LoginIcon")) {
			eXo.core.DOMUtil.removeClass(logXXXObj, "LoginIcon");			
		} 
		eXo.core.DOMUtil.addClass(logXXXObj, "LogoutIcon");
		logXXXObj.onclick = eXo.portal.logout;
	} else {
		welcomeObj.style.display = "none";
		var loginAction = "if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ShowLoginForm', true));";
		if (eXo.core.DOMUtil.hasClass(logXXXObj, "LogoutIcon")) {
			eXo.core.DOMUtil.removeClass(logXXXObj, "LogoutIcon");
		} 
	  eXo.core.DOMUtil.addClass(logXXXObj, "LoginIcon");
		logXXXObj.innerHTML = loginBundle;
		logXXXObj.onclick =  function() { eval(loginAction); }
	}
	
}	

function changeLanguage() {
	var baseURL;
	var changeLangBundleKey  = "ClassicLogin.LoginContent.ChangeLanguage";
	var gettingBundleMessageRestRequest = "/portal/rest/resourceBundle/getString/en/";

	with (window.location) {
			baseURL = protocol + "//" + hostname + ":" + port;
	}
	var changeLanguageAction = "if(document.getElementById('UIMaskWorkspace')) ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ChangeLanguage', true));";
	var classicLoginObj = document.getElementById("classic-access");
	var loginContentObj = eXo.core.DOMUtil.findFirstDescendantByClass(classicLoginObj, "div", "LoginContent");
	var languageObj = eXo.core.DOMUtil.findFirstDescendantByClass(loginContentObj, "a", "LanguageIcon");
	var changeLangBundle = ajaxAsyncGetRequest(baseURL + gettingBundleMessageRestRequest + changeLangBundleKey, false);
	languageObj.innerHTML = changeLangBundle;
	languageObj.onclick = function() { eval(changeLanguageAction); }
}

eXo.core.Browser.addOnLoadCallback('validateUser', validateUser);                 
eXo.core.Browser.addOnLoadCallback('changeLanguage', changeLanguage);
/*-----------------------------------------------------------------------------*/