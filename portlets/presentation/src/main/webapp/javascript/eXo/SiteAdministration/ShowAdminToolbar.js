function showWorkspaceArea(showable) {	
	if (showable === true) {		
		eXo.portal.UIControlWorkspace.showControlWorkspace = false;
		eXo.portal.UIControlWorkspace.showWorkspace();
	} 
}