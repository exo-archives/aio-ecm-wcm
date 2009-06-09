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
			var newWindow = window.open( FCKConfig.eXoPath + "explorer/explorer.html?Type=Image&Connector=/portal/rest/wcmImage/", "eXoExplorer", sOptions );
			newWindow.focus();
		}
	oImage.GetState = function() {};
	FCKCommands.RegisterCommand( "WCMInsertImage", oImage ) ;
	var oElement = new FCKToolbarButton("WCMInsertImage", FCKLang.WCMInsertImagePlugins) ;
	oElement.IconPath = FCKConfig.eXoPath + "plugins/insertImage/insertImage.gif" ;
	FCKToolbarItems.RegisterItem( "WCMInsertImage", oElement ) ;
	})();