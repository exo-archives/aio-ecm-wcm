// set thumbnail size;
FCKConfig.thumbnailWidth = 80;
FCKConfig.thumbnailHeight = 80;

// set eXo plugin path;
FCKConfig.eXoPath = FCKConfig.BasePath.substr(0, FCKConfig.BasePath.length - 7) + "exo/" ;
FCKConfig.Plugins.Add( 'urani', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertImage', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertDocument', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertPortalLink', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertGadget', null, FCKConfig.eXoPath + "plugins/") ;

//config style
FCKConfig.EditorAreaCSS = '';
FCKConfig.EditorAreaStyles = 'body{	background: none;	margin: 0px;}' ;
FCKConfig.InsertedLinkColor = 'green';

FCKConfig.ToolbarSets["CompleteWCM"] = [
	['Source','DocProps','-','NewPage','Preview','-','Templates'],
	['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],
	['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
	'/',
	['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
	['OrderedList','UnorderedList','-','Outdent','Indent','Blockquote','CreateDiv'],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
	['Link','Insert link to a site page','Unlink','Anchor'],
	['Insert Gadget', 'Insert Image','Insert DMS Document','Flash','Table','Rule','SpecialChar','PageBreak'],
	['TextColor','BGColor'],
	['FitWindow','ShowBlocks'],
	['Style','FontFormat','FontName','FontSize']
] ;

FCKConfig.ToolbarSets["BasicWCM"] = [
	['Source','-','Bold','Italic','Underline','StrikeThrough','-','OrderedList','UnorderedList','Outdent','Indent'],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
	['Blockquote','-','Link','Unlink','Insert link to a site page','Insert Image','Insert DMS Document','-','FitWindow','ShowBlocks'],	
	['Style','FontFormat','FontName','FontSize']
] ;

FCKConfig.ToolbarSets["CSSToolBar"] = [
	['Insert Image']
];

FCKConfig.ToolbarSets["JSToolBar"] = [ ];

FCKConfig.SourceModeCommands = ['Insert Image'];
//eXoPlugin config
window.eXoPlugin = {
	init: function() {
		with (window.parent.eXo.env.portal) {
   this.originPortalName = this.portalName = portalName;
			this.context = context;
			this.accessMode = accessMode;
		}
		
		var parentLocation = window.parent.location;
		this.hostName = parentLocation.href.substring(0, parentLocation.href.indexOf(parentLocation.pathname));
		
		this.eXoFileManager = {
			Connector: "/portal/rest/fckconnector/jcr/",
			ResourceType : "File"
		};
		
		this.ExoImageBrowserURL = FCKConfig.eXoPath + 'filemanager/browser/default/browser.html?Type=Image&Connector=/portal/rest/fckconnector/jcr/getFiles' ;
		this.ExoFileBrowserURL = FCKConfig.eXoPath + 'filemanager/browser/default/browser.html?Type=File&Connector=/portal/rest/fckconnector/jcr/getFiles' ;
		this.ExoGadgetBrowserURL = FCKConfig.eXoPath + 'filemanager/browser/default/browser.html?Type=Gadget&Connector=/portal/rest/wcmGadget/' ;
		this.ExoPortalLinkBrowserURL = FCKConfig.eXoPath + 'explorer/explorer.html?Type=PortalLink&Connector=/portal/rest/portalLinks/&disableUploading=true&disableCreatingFolder=true' ;
		FCKConfig.LinkBrowserURL = FCKConfig.eXoPath + 'explorer/explorer.html?Type=Link&Connector=/portal/rest/wcmLink/';
		//detect user language
		this.userLanguage = FCK.Language.GetActiveLanguage() || "en";
	},
	switchToolBar: function(r) {
		var Setting = {
			oldBar: r.oldBar || "" ,
			newBar: r.newBar || "",
			useBar: r.useBar || []
		};
		with (Setting) {
			if (oldBar && newBar && FCKConfig.ToolbarSets[newBar] && FCKConfig.ToolbarSets[oldBar]) {
				FCKConfig.ToolbarSets[oldBar] = FCKConfig.ToolbarSets[newBar];
			}
		}
		//demo =>  eXoPlugin.switchToolBar({oldBar: "Basic", newBar: "eXoBar"});
	},
	addBar: function(r) {
		var Setting = {
			newBar: r.newBar || "",
			targetBar: r.targetBar || ""
		}

		with (Setting) {
			if (newBar == targetBar) return;
			if (newBar && targetBar && FCKConfig.ToolbarSets[newBar] && FCKConfig.ToolbarSets[targetBar]) {
				FCKConfig.ToolbarSets[targetBar].push("/");
				for (var o = 0; o < FCKConfig.ToolbarSets[newBar].length; ++o) {
					FCKConfig.ToolbarSets[targetBar].push(FCKConfig.ToolbarSets[newBar][o]);
				}
			}
		}
		//demo => eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Basic" });
	},
	getContent: function() {
		var content = "";
		if (document.selection) {
			var range = FCK.EditorWindow.document.selection.createRange();
			content = range.text;
		} else  {
			var range = FCK.EditorWindow.getSelection();
			content = range.getRangeAt(0);
		}
		if (content) content = content.toString().replace(/^\s+|\s+$/g, "");
		if (content != "") return content;
		else return null;
	},
	loadScript: function() {
		if (arguments.length < 2) {
			return;
		} else {
			var win = arguments[0];
			var src = arguments[1];
		}
		if (!win || !win.document) return;
		var eScript = win.document.createElement("script");
		eScript.setAttribute("src", src);
		var eHead = win.document.getElementsByTagName("head")[0];
		eHead.appendChild(eScript);
	}
};

/**
	FCKCommentsProcessor
	---------------------------
	It's run after a document has been loaded, it detects all the protected source elements

	In order to use it, you add your comment parser with 
	FCKCommentsProcessor.AddParser( function )
*/
if (typeof FCKCommentsProcessor === 'undefined')
{
	FCKCommentsProcessor = FCKDocumentProcessor.AppendNew() ;
	FCKCommentsProcessor.ProcessDocument = function( oDoc )
	{
		if ( FCK.EditMode != FCK_EDITMODE_WYSIWYG )
			return ;

		if ( !oDoc )
			return ;

	//Find all the comments: <!--{PS..0}-->
	//try to choose the best approach according to the browser:
		if ( oDoc.evaluate )
			this.findCommentsXPath( oDoc );
		else
		{
			if (oDoc.all)
				this.findCommentsIE( oDoc.body ) ;
			else
				this.findComments( oDoc.body ) ;
		}

	}

	FCKCommentsProcessor.findCommentsXPath = function(oDoc) {
		var nodesSnapshot = oDoc.evaluate('//body//comment()', oDoc.body, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null );

		for ( var i=0 ; i < nodesSnapshot.snapshotLength; i++ )
		{
			this.parseComment( nodesSnapshot.snapshotItem(i) ) ;
		}
	}

	FCKCommentsProcessor.findCommentsIE = function(oNode) {
		var aComments = oNode.getElementsByTagName( '!' );
		for(var i=aComments.length-1; i >=0 ; i--)
		{
			var comment = aComments[i] ;
			if (comment.nodeType == 8 ) // oNode.COMMENT_NODE) 
				this.parseComment( comment ) ;
		}
	}

	// Fallback function, iterate all the nodes and its children searching for comments.
	FCKCommentsProcessor.findComments = function( oNode ) 
	{
		if (oNode.nodeType == 8 ) // oNode.COMMENT_NODE) 
		{
			this.parseComment( oNode ) ;
		}
		else 
		{
			if (oNode.hasChildNodes()) 
			{
				var children = oNode.childNodes ;
				for (var i = children.length-1; i >=0 ; i--) 
					this.findComments( children[ i ] );
			}
		}
	}

	// We get a comment node
	// Check that it's one that we are interested on:
	FCKCommentsProcessor.parseComment = function( oNode )
	{
		var value = oNode.nodeValue ;

		// Difference between 2.4.3 and 2.5
		var prefix = ( FCKConfig.ProtectedSource._CodeTag || 'PS\\.\\.' ) ;

		var regex = new RegExp( "\\{" + prefix + "(\\d+)\\}", "g" ) ;

		if ( regex.test( value ) ) 
		{
			var index = RegExp.$1 ;
			var content = FCKTempBin.Elements[ index ] ;

			// Now call the registered parser handlers.
			var oCalls = this.ParserHandlers ;
			if ( oCalls )
			{
				for ( var i = 0 ; i < oCalls.length ; i++ )
					oCalls[ i ]( oNode, content, index ) ;

			}

		}
	}

	/**
		The users of the object will add a parser here, the callback function gets two parameters:
			oNode: it's the node in the editorDocument that holds the position of our content
			oContent: it's the node (removed from the document) that holds the original contents
			index: the reference in the FCKTempBin of our content
	*/
	FCKCommentsProcessor.AddParser = function( handlerFunction )
	{
		if ( !this.ParserHandlers )
			this.ParserHandlers = [ handlerFunction ] ;
		else
		{
			// Check that the event handler isn't already registered with the same listener
			// It doesn't detect function pointers belonging to an object (at least in Gecko)
			if ( this.ParserHandlers.IndexOf( handlerFunction ) == -1 )
				this.ParserHandlers.push( handlerFunction ) ;
		}
	}
}
/**
	END of FCKCommentsProcessor
	---------------------------
*/

FCK["eXoPlugin"] = eXoPlugin;

eXoPlugin.init();