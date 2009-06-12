(function(){
		var oDocument = new Object() ;
		oDocument.Execute = function() {
			var width = 800;
			var height = 600;
			var iLeft = ( FCKConfig.ScreenWidth  - width ) / 2 ;
			var iTop  = ( FCKConfig.ScreenHeight - height ) / 2 ;
			var sOptions = "toolbar=no,status=no,resizable=yes,dependent=yes,scrollbars=yes" ;
			sOptions += ",width=" + width ;
			sOptions += ",height=" + height ;
			sOptions += ",left=" + iLeft ;
			sOptions += ",top=" + iTop ;
			var newWindow = window.open( FCKConfig.eXoPath + "explorer/explorer.html?Type=File&Connector=/portal/rest/wcmDocument/", "eXoExplorer", sOptions );
			newWindow.focus();
		}
	FCKCommands.RegisterCommand("WCMInsertDocument", oDocument ) ;
	var oElement = new FCKToolbarButton("WCMInsertDocument", FCKLang.WCMInsertDocumentPlugins) ;
	oElement.IconPath = FCKConfig.eXoPath + "plugins/insertDocument/insertDocument.gif" ;
	FCKToolbarItems.RegisterItem("WCMInsertDocument", oElement ) ;	
})();