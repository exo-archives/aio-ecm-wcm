(function(){
		var oImage = new Object() ;
		oImage.Execute = function() {
			var width = 800;
			var height = 600;
			var iLeft = ( FCKConfig.ScreenWidth  - width ) / 2 ;
			var iTop  = ( FCKConfig.ScreenHeight - height ) / 2 ;
			var sOptions = "toolbar=no,status=no,resizable=yes,dependent=yes,scrollbars=yes" ;
			sOptions += ",width=" + width ;
			sOptions += ",height=" + height ;
			sOptions += ",left=" + iLeft ;
			sOptions += ",top=" + iTop ;
			var newWindow = window.open( FCKConfig.eXoPath + "explorer/explorer.html?Type=File&Connector=/portal/rest/wcmImage/", "eXoExplorer", sOptions );
			newWindow.focus();
		}
	oImage.GetState = function() {};
	FCKCommands.RegisterCommand( "Insert Image", oImage ) ;
	var oElement = new FCKToolbarButton( "Insert Image" ) ;
	oElement.IconPath = FCKConfig.eXoPath + "plugins/insertImage/insertImage.gif" ;
	FCKToolbarItems.RegisterItem( "Insert Image", oElement ) ;
	})();