//config eXo plugin bar
FCKConfig.ToolbarSets["eXoBar"] = [
	['Insert Image', 'Insert Portal Link', 'Insert DMS Document', 'Explorer']
];

// set eXo plugin path;
FCKConfig.eXoPath = FCKConfig.BasePath.substr(0, FCKConfig.BasePath.length - 7) + "exo/" ;
FCKConfig.Plugins.Add( 'urani', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'explorer', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertImage', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertDocument', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertPortalLink', null, FCKConfig.eXoPath + "plugins/") ;

//config style
FCKConfig.EditorAreaCSS = ['/eXoResources/skin/Stylesheet.css', '/wcmResources/skin/Stylesheet.css', '/portal/css/WebContent/Live/Stylesheet.css'] ;
FCKConfig.EditorAreaStyles = 'body{	background: none;	margin: 0px;}' ;

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
		this.ExoPortalLinkBrowserURL = FCKConfig.eXoPath + 'explorer/explorer.html?Type=PortalLink&Connector=/portal/rest/portalLinks/' ;
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
FCK["eXoPlugin"] = eXoPlugin;

eXoPlugin.init();
eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Basic" });
eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Default" });
