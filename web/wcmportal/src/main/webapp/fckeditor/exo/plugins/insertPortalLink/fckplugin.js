var initFun = function(O) {
	// init function
}

FCKCommands.RegisterCommand( "Insert link", new FCKDialogCommand( "Insert Portal Link", "Insert Portal Link", FCKConfig.eXoPath + "plugins/insertPortalLink/insertPortalLink.html"	, 600, 400) ) ;
var oInsertPortalLink = new FCKToolbarButton("Insert link") ;
oInsertPortalLink.IconPath = FCKConfig.eXoPath + "plugins/insertPortalLink/insertPortalLink.gif" ;
FCKToolbarItems.RegisterItem("Insert Portal Link", oInsertPortalLink) ;