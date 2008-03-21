var initFun = function(O) {
	// init function
}

FCKCommands.RegisterCommand( "InsertPortalLink", new FCKDialogCommand( "InsertPortalLink", "InsertPortalLink", FCKConfig.eXoPath + "plugins/insertPortalLink/insertPortalLink.html"	, 600, 400) ) ;

var oInsertPortalLink = new FCKToolbarButton("InsertPortalLink") ;
oInsertPortalLink.IconPath = FCKConfig.eXoPath + "plugins/insertPortalLink/insertPortalLink.gif" ;

FCKToolbarItems.RegisterItem("InsertPortalLink", oInsertPortalLink) ;
