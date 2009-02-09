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
			var newWindow = window.open( FCKConfig.eXoPath + "explorer/explorer.html?Type=Gadget&Thumbnail=true&Connector=/portal/rest/wcmGadget/", "eXoExplorer", sOptions );
			newWindow.focus();
		}
	oGadget.GetState = function() {};
	FCKCommands.RegisterCommand( "Insert Gadget", oGadget ) ;
	FCKCommands.RegisterCommand( "Edit Gadget", new FCKDialogCommand("Edit Gadget", "Edit Gadget", FCKPlugins.Items["insertGadget"].Path + "dialog/gadgets.htm", 450, 428 ) ) ;
	var oElement = new FCKToolbarButton( "Insert Gadget" ) ;
	oElement.IconPath = FCKConfig.eXoPath + "plugins/insertGadget/insertGadget.gif" ;
	FCKToolbarItems.RegisterItem( "Insert Gadget", oElement ) ;
	})();
	
	
var Gadgets_CommentsProcessorParser = function(oNode, oContent, index) {
	if (/WCM gadgets/.test(oContent)) {
		var oFakeNode = FCK.EditorDocument.createElement( 'IMG' ) ;
		if ( !oNode ) {
			FCK.InsertElement(oNode);
		}
		oFakeNode.className = 'FCK__Flash' ; 
		var start = oContent.indexOf('\/\/') + 'WCM gadgets'.length + 3;
		var stop = oContent.indexOf('eXo.core', start);
		var thumbnail = oContent.substring(start, stop);
		oFakeNode.src = thumbnail;
		oFakeNode.setAttribute( '_fckfakelement', 'true', 0 ) ;
		oFakeNode.setAttribute( '_fckrealelement', FCKTempBin.AddElement( oNode ), 0 ) ;
		oFakeNode.setAttribute( '_fckgadgetthumbnail', thumbnail, 0 ) ;
		oNode.parentNode.insertBefore( oFakeNode, oNode ) ;
		oNode.parentNode.removeChild( oNode ) ;
	}
}
FCKCommentsProcessor.AddParser(Gadgets_CommentsProcessorParser);
FCK.RegisterDoubleClickHandler(editGadget, 'IMG' ) ;
function editGadget(oNode) {
	if (!oNode.getAttribute('_fckgadgetthumbnail'))
		return ;
	FCK.Commands.GetCommand('Edit Gadget').Execute();
}
