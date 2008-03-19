var initFun = function(O) {
	// init function
}

FCKCommands.RegisterCommand( "InsertLink", new FCKDialogCommand( "InsertLink", "InsertLink", FCKConfig.eXoPlugins + "insertLink/insertLink.html"	, 600, 400) ) ;

var oInsertLink = new FCKToolbarButton("InsertLink") ;
oInsertLink.IconPath = FCKConfig.eXoPlugins + "insertLink/insertLink.gif" ;

FCKToolbarItems.RegisterItem("InsertLink", oInsertLink) ;	
