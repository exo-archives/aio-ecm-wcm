	
var eXoPlugin = {};
FCKConfig.ToolbarSets["eXoBar"] = [
	['InsertImage', 'InsertLink', 'InsertPortalLink', 'InsertDocument']
];

// Change the default plugin path.
var eXoPath = FCKConfig.BasePath.substr(0, FCKConfig.BasePath.length - 7) + "exo/" ;
FCKConfig.eXoPlugins = eXoPath + "plugins/";
FCKConfig.Plugins.Add( 'urani', null, FCKConfig.eXoPlugins) ;
FCKConfig.Plugins.Add( 'insertImage', null, FCKConfig.eXoPlugins) ;
FCKConfig.Plugins.Add( 'insertLink', null, FCKConfig.eXoPlugins) ;
FCKConfig.Plugins.Add( 'insertDocument', null, FCKConfig.eXoPlugins) ;
FCKConfig.Plugins.Add( 'insertPortalLink', null, FCKConfig.eXoPlugins) ;

eXoPlugin.ExoImageBrowserURL = eXoPath + 'filemanager/browser/default/browser.html?Type=Image&Connector=/portal/connector' ;
eXoPlugin.ExoFileBrowserURL = eXoPath + 'filemanager/browser/default/browser.html?Type=File&Connector=/portal/connector' ;

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
		if (newBar && targetBar && FCKConfig.ToolbarSets[newBar] && FCKConfig.ToolbarSets[targetBar]) {
			FCKConfig.ToolbarSets[targetBar].push(FCKConfig.ToolbarSets[newBar]);
		}
	}
};
//eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Basic" });
//eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Default" });

FCKConfig.ToolbarSets["Default"] = [
	['Source','DocProps','-','Save','NewPage','Preview','-','Templates'],
	['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],
	['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
	['Form','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField'],
	'/',
	['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
	['OrderedList','UnorderedList','-','Outdent','Indent'],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
	['Link','Unlink','Anchor'],
	['Image','Flash','Table','Rule','Smiley','SpecialChar','PageBreak','UniversalKey'],
	'/',
	['Style','FontFormat','FontName','FontSize'],
	['TextColor','BGColor'],
	['FitWindow','-','About'],
	'/',
	['InsertImage', 'InsertLink', 'InsertPortalLink', 'InsertDocument']
] ;

FCKConfig.ToolbarSets["Basic"] = [
	['Bold','Italic','-','OrderedList','UnorderedList','-','Link','Unlink','-','About'],
	'/',
	['InsertImage', 'InsertLink', 'InsertPortalLink', 'InsertDocument']
] ;
	
	