(function(){
		var oGadget = new Object() ;
		oGadget.Execute = function() {
			var width = 800;
			var height = 600;
			var iLeft = ( FCKConfig.ScreenWidth  - width ) / 2 ;
			var iTop  = ( FCKConfig.ScreenHeight - height ) / 2 ;
			var sOptions = "toolbar=no,status=no,resizable=yes,dependent=yes,scrollbars=yes" ;
			sOptions += ",width=" + width ;
			sOptions += ",height=" + height ;
			sOptions += ",left=" + iLeft ;
			sOptions += ",top=" + iTop ;
			var newWindow = window.open( FCKConfig.eXoPath + "explorer/explorer.html?Type=Gadget&Connector=/portal/rest/wcmGadget/", "eXoExplorer", sOptions );
			newWindow.focus();
		}
	oGadget.GetState = function() {};
	FCKCommands.RegisterCommand( "Insert Gadget", oGadget ) ;
	var oElement = new FCKToolbarButton( "Insert Gadget" ) ;
	oElement.IconPath = FCKConfig.eXoPath + "plugins/insertGadget/insertGadget.gif" ;
	FCKToolbarItems.RegisterItem( "Insert Gadget", oElement ) ;
	})();