var initFun = function(O) {
	// init function
}

FCKCommands.RegisterCommand( "InsertLink", new FCKDialogCommand( "InsertLink", "InsertLink", FCKConfig.eXoPath + "plugins/insertLink/insertLink.html"	, 600, 400) ) ;

var oInsertLink = new FCKToolbarButton("InsertLink") ;
oInsertLink.IconPath = FCKConfig.eXoPath + "plugins/insertLink/insertLink.gif" ;

FCKToolbarItems.RegisterItem("InsertLink", oInsertLink) ;	
	
