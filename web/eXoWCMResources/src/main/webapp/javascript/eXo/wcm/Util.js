
window.wcm = function() {}

wcm.insertCSSFromTextArea2FCK = function(Instance, ContentCSS) {
	var eContentCSS = document.getElementById(ContentCSS);
	var sContentCSSId = ContentCSS + "_Inline";
	eContentCSS.onblur = updateStyle;
	function updateStyle() {
		var sValue = eContentCSS.value;
		var iDoc = FCKeditorAPI.Instances[Instance].EditorWindow.document;
		var eHead = iDoc.getElementsByTagName("head")[0];
		var eStyle = iDoc.getElementById(sContentCSSId);
		if (eStyle) {
			eHead.removeChild(eStyle);
		}
		eStyle = iDoc.createElement("style");
		eStyle.setAttribute("type", "text/css");
		eStyle.setAttribute("id", sContentCSSId);
		eStyle.innerHTML = sValue;
		eHead.appendChild(eStyle);
	}
}

//kill gadget :D
eXo.gadget = {
	UIGadget: {
		createGadget: function(){}
	}
}
