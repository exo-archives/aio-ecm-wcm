var initFun = function(O) {
	// init function
}

FCKCommands.RegisterCommand( "InsertDocument", new FCKDialogCommand( "InsertDocument", "InsertDocument", FCKConfig.eXoPlugins + "insertDocument/insertDocument.html"	, 600, 400) ) ;

var oInsertDocument = new FCKToolbarButton("InsertDocument") ;
oInsertDocument.IconPath = FCKConfig.eXoPlugins + "insertDocument/insertDocument.gif" ;

FCKToolbarItems.RegisterItem("InsertDocument", oInsertDocument) ;	
