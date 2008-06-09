	var oExplorer = new Object() ;
		oExplorer.Name = 'FileIO' ;
		
		oExplorer.Execute = function() {
			alert("xong");
		}
	
	oExplorer.GetState = function() {}
	
	FCKCommands.RegisterCommand( 'Explorer', oExplorer ) ;
	
	var oElement = new FCKToolbarButton('Explorer') ;
	oElement.IconPath = FCKConfig.eXoPath + "plugins/explorer/explorer.jpg" ;
	FCKToolbarItems.RegisterItem('Explorer', oElement) ;	
