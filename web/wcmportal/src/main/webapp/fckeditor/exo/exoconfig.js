	
var eXoPlugin = {};

FCKConfig.ToolbarSets["eXoBar"] = [
	['Urani', 'InsertImage', 'InsertLink', 'InsertPortalLink', 'InsertDocument']
];

// set eXo  plugin path;
FCKConfig.eXoPath = FCKConfig.BasePath.substr(0, FCKConfig.BasePath.length - 7) + "exo/" ;
FCKConfig.Plugins.Add( 'urani', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertImage', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertLink', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertDocument', null, FCKConfig.eXoPath + "plugins/") ;
FCKConfig.Plugins.Add( 'insertPortalLink', null, FCKConfig.eXoPath + "plugins/") ;

eXoPlugin.ExoImageBrowserURL = FCKConfig.eXoPath + 'filemanager/browser/default/browser.html?Type=Image&Connector=/portal/connector' ;
eXoPlugin.ExoFileBrowserURL = FCKConfig.eXoPath + 'filemanager/browser/default/browser.html?Type=File&Connector=/portal/connector' ;

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

eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Basic" });
eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Default" });
