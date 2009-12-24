//InsertPortalLink
var initFun = function(O) {
	// init function
}
FCKCommands.RegisterCommand( "WCMInsertPortalLink", new FCKDialogCommand("WCMInsertPortalLink", FCKLang.WCMInsertPortalLinkDialogTitle, FCKConfig.eXoPath + "plugins/insertPortalLink/insertPortalLink.html", 600, 400) );
var oElement = new FCKToolbarButton( "WCMInsertPortalLink" , FCKLang.WCMInsertPortalLinkPlugins);
oElement.IconPath = FCKConfig.eXoPath + "plugins/insertPortalLink/insertPortalLink.gif" ;
FCKToolbarItems.RegisterItem( "WCMInsertPortalLink", oElement );
