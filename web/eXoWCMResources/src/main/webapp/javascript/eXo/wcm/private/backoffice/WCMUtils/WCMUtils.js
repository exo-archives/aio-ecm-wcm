function WCMUtils(){
}

WCMUtils.prototype.getHostName = function(){
	var parentLocation = window.parent.location;
	return parentLocation.href.substring(0, parentLocation.href.indexOf(parentLocation.pathname));
};

if(!window.wcm) eXo.wcm = new Object();
eXo.wcm.WCMUtils = new WCMUtils();