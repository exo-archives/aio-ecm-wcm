	
var eXoPlugin = {};
var parentLocation = window.parent.location;
eXoPlugin.hostName = parentLocation.href.substring(0, parentLocation.href.indexOf(parentLocation.pathname));
eXoPlugin.portalName = window.parent.eXo.env.portal.portalName;
eXoPlugin.originPortalName = eXoPlugin.portalName;

eXoPlugin.availablePortals = ['classic' , 'webos'];

with (window.parent.eXo.env.portal) {
	eXoPlugin.context = context;
	eXoPlugin.accessMode = accessMode;
}

FCKConfig.ToolbarSets["eXoBar"] = [
	['Insert Image', 'Insert Portal Link', 'Insert DMS Document', 'Explorer']
];

// set eXo  plugin path;
FCKConfig.eXoPath = FCKConfig.BasePath.substr(0, FCKConfig.BasePath.length - 7) + "exo/" ;
FCKConfig.Plugins.Add( 'urani', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'explorer', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertImage', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertDocument', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertPortalLink', null, FCKConfig.eXoPath + "plugins/") ;

eXoPlugin.ExoImageBrowserURL = FCKConfig.eXoPath + 'filemanager/browser/default/browser.html?Type=Image&Connector=/portal/connector' ;
eXoPlugin.ExoFileBrowserURL = FCKConfig.eXoPath + 'filemanager/browser/default/browser.html?Type=File&Connector=/portal/connector' ;
eXoPlugin.ExoPortalLinkBrowserURL = FCKConfig.eXoPath + 'filemanager/portal/default/browser.html?Type=PortalLink&Connector=/portal/rest/fckconnectorext/pageURI' ;

eXoPlugin.switchToolBar = function(R) {
	var Setting = {
		oldBar: R.oldBar || "" ,
		newBar: R.newBar || "",
		useBar: R.useBar || []
	};
	with (Setting) {
		if (oldBar && newBar && FCKConfig.ToolbarSets[newBar] && FCKConfig.ToolbarSets[oldBar]) {
			FCKConfig.ToolbarSets[oldBar] = FCKConfig.ToolbarSets[newBar];
		}
	}
};

//eXoPlugin.switchToolBar({oldBar: "Basic", newBar: "eXoBar"});
eXoPlugin.addBar = function(R) {
	var Setting = {
		newBar: R.newBar || "",
		targetBar: R.targetBar || ""
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
};

eXoPlugin.getContent = function() {
	var content = new String();
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
};

eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Basic" });
eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Default" });

FCK["eXoPlugin"] = eXoPlugin;