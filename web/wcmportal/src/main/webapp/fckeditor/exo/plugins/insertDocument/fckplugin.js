var initFun = function(O) {
	// init function
}

FCKCommands.RegisterCommand( "InsertDocument", new FCKDialogCommand( "InsertDocument", "InsertDocument", FCKConfig.eXoPath + "plugins/insertDocument/insertDocument.html"	, 600, 400) ) ;

var oInsertDocument = new FCKToolbarButton("InsertDocument") ;
oInsertDocument.IconPath = FCKConfig.eXoPath + "plugins/insertDocument/insertDocument.gif" ;

FCKToolbarItems.RegisterItem("InsertDocument", oInsertDocument) ;	
