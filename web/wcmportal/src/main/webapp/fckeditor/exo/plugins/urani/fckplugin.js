	var eUranium = new Object() ;
		eUranium.Name = 'Urani' ;
		
		eUranium.Execute = function() {
			alert(92);
		}
	
	eUranium.GetState = function() {}
	
	FCKCommands.RegisterCommand( 'Urani', eUranium ) ;
	
	var oElement = new FCKToolbarButton('Urani') ;
	oElement.IconPath = FCKConfig.eXoPlugins + 'urani/urani.gif' ;
	FCKToolbarItems.RegisterItem('Urani', oElement) ;	
