var initFun = function(O) {
	// init function
}

FCKCommands.RegisterCommand("Insert link to a site page", new FCKDialogCommand( "Insert Portal Link", "Insert Portal Link", FCKConfig.eXoPath + "plugins/insertPortalLink/insertPortalLink.html"	, 600, 400) ) ;
var oInsertPortalLink = new FCKToolbarButton("Insert link to a site page") ;
oInsertPortalLink.IconPath = FCKConfig.eXoPath + "plugins/insertPortalLink/insertPortalLink.gif" ;
FCKToolbarItems.RegisterItem("Insert link to a site page", oInsertPortalLink) ;