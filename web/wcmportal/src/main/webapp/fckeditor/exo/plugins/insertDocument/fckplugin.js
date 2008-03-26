	var eUranium = new Object() ;
		eUranium.Name = 'Urani' ;
		
		eUranium.Execute = function() {
			var width = 800;
			var height = 600;
			var iLeft = ( FCKConfig.ScreenWidth  - width ) / 2 ;
			var iTop  = ( FCKConfig.ScreenHeight - height ) / 2 ;

			var sOptions = "toolbar=no,status=no,resizable=yes,dependent=yes,scrollbars=yes" ;
			sOptions += ",width=" + width ;
			sOptions += ",height=" + height ;
			sOptions += ",left=" + iLeft ;
			sOptions += ",top=" + iTop ;
			var newWindow = window.open( eXoPlugin.ExoFileBrowserURL, 'eXoFileBrowseWindow', sOptions );
			newWindow.focus();
		}
	
	eUranium.GetState = function() {}
	
	FCKCommands.RegisterCommand( 'InsertDocument', eUranium ) ;
	
	var oElement = new FCKToolbarButton('InsertDocument') ;
	oElement.IconPath = FCKConfig.eXoPath + "plugins/insertDocument/insertDocument.gif" ;
	FCKToolbarItems.RegisterItem('Insert DMS Document', oElement) ;	

