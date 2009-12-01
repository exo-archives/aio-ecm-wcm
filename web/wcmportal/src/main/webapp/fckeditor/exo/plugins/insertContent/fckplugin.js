(function() {
	var oContent = new Object();
	oContent.Execute = function() {
		var width = 900;
		var height = 600;
		var iLeft = ( FCKConfig.ScreenWidth  - width ) / 2;
		var iTop  = ( FCKConfig.ScreenHeight - height ) / 2;
		var sOptions = "toolbar=no,status=no,resizable=yes,dependent=yes,scrollbars=yes";
		sOptions += ",width=" + width ;
		sOptions += ",height=" + height;
		sOptions += ",left=" + iLeft;
		sOptions += ",top=" + iTop;
		var newWindow = window.open( FCKConfig.eXoPath + "content/content.html?Type=File&Connector=/portal/rest/wcmDriver/getDrivers?repositoryName=repository&workspaceName=collaboration", "eXoContent", sOptions );
		newWindow.focus();
	}
	FCKCommands.RegisterCommand( "WCMInsertContent", oContent );
	var oElement = new FCKToolbarButton( "WCMInsertContent" );
	oElement.IconPath = FCKConfig.eXoPath + "plugins/insertContent/content.jpg";
	FCKToolbarItems.RegisterItem( "WCMInsertContent", oElement );
})();

