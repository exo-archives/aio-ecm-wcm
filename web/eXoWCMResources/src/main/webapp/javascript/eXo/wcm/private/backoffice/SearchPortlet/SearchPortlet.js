function SearchPortlet() {
}

SearchPortlet.prototype.showObject = function(obj) {
	var element = eXo.core.DOMUtil.findNextElementByTagName(obj, "div");
	if (!element.style.display || element.style.display != 'block') {
		element.style.display = 'block';
	} else {
		element.style.display = 'none';
	}
};

SearchPortlet.prototype.getRuntimeContextPath = function() {
	return eXo.wcm.WCMUtils.getHostName() + eXo.env.portal.context + '/' + eXo.env.portal.accessMode + '/' + eXo.env.portal.portalName + '/';
};

SearchPorlet.prototype.quickSearch = function(){
	var searchBox = document.getElementById("siteSearchBox");
	var keyWordInput = eXo.core.DOMUtil.findFirstDescendantByClass(searchBox, "input", "keyword");
	var keyword = encodeURI(keyWordInput.value);
	var resultPageURIDefault = "searchResult";
	var params = "portal=" + eXo.env.portal.portalName + "&keyword=" + keyword;
	var baseURI = getHostName() + eXo.env.portal.context + "/" + eXo.env.portal.accessMode + "/" + eXo.env.portal.portalName; 
	if (resultPageURI != undefined) {
		baseURI = baseURI + "/" + resultPageURI; 
	} else {
		baseURI = baseURI + "/" + resultPageURIDefault;  
	}
	window.location = baseURI + "?" + params;
};

SearchPortlet.prototype.quickSearchOnEnter = function(event, resultPageURI) {
	var keyNum = eXo.core.Keyboard.getKeynum(event)
  if (keyNum == 13)	quickSearch(resultPageURI);
};

SearchPortlet.prototype.search = function(){
	var searchForm = document.getElementById(comId);
	var inputKey = eXo.core.DOMUtil.findDescendantById(searchForm, "keywordInput");
	searchForm.onsubmit = function() {return false;};
	inputKey.onkeypress = function(event) {
		var keyNum = eXo.core.Keyboard.getKeynum(event);
		if (keyNum == 13) {
			var searchButton = eXo.core.DOMUtil.findFirstDescendantByClass(this.form, "div", "SearchButton");
			searchButton.onclick();
  	 }		
	}
};

SearchPortlet.prototype.keepKeywordOnBoxSearch = function() {
	var queryRegex = /^portal=[\w%]+&keyword=[\w%]+/;
	var searchBox = document.getElementById("siteSearchBox");
	var keyWordInput = eXo.core.DOMUtil.findFirstDescendantByClass(searchBox, "input", "keyword");
	var queryString = location.search.substring(1);
	if (!queryString.match(queryRegex)) {return;}
	var portalParam = queryString.split('&')[0];
	var keyword = decodeURI(queryString.substring((portalParam + "keyword=").length +1));
	if (keyword != undefined && keyword.length != 0) {
		keyWordInput.value = unescape(keyword); 
	}
};

eXo.core.Browser.addOnLoadCallback("keepKeywordOnBoxSearch", eXo.wcm.SearchPortlet.keepKeywordOnBoxSearch);
if(!window.wcm) eXo.wcm = new Object();
eXo.wcm.SearchPortlet = new SearchPortlet();