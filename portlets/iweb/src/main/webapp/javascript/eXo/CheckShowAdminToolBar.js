function isSiteToolbarShowed() {
	var root = document.getElementById("admintoolbar");
	var siteAdminToolbar = eXo.core.DOMUtil.findDescendantById(root, "ViewMode");
	if (siteAdminToolbar) {
		var uiBanner = document.getElementById("UIBannerPortlet");
		var loginContent = eXo.core.DOMUtil.findFirstDescendantByClass(uiBanner, "div", "LoginContent");
		loginContent.style.top = "70px";
	}
}