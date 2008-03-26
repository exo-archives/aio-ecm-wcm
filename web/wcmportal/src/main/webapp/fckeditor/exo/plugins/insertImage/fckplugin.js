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
			var newWindow = window.open( eXoPlugin.ExoImageBrowserURL, 'eXoImagesBrowseWindow', sOptions );
			newWindow.focus();
		}
	
	eUranium.GetState = function() {}
	
	FCKCommands.RegisterCommand( 'Insert Image', eUranium ) ;
	
	var oElement = new FCKToolbarButton('Insert Image') ;
	oElement.IconPath = FCKConfig.eXoPath + "plugins/insertImage/insertImage.gif" ;
	FCKToolbarItems.RegisterItem('Insert Image', oElement) ;	

